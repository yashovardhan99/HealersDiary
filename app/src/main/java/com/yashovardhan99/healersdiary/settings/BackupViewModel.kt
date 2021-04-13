package com.yashovardhan99.healersdiary.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.core.database.getStringOrNull
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.yashovardhan99.core.backup_restore.BackupUtils
import com.yashovardhan99.core.backup_restore.ExportWorker
import com.yashovardhan99.core.backup_restore.ImportWorker
import com.yashovardhan99.core.database.HealersDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltViewModel
class BackupViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val dataStore: HealersDataStore
) : ViewModel() {
    private val workManager = WorkManager.getInstance(context)
    private val contentResolver = context.contentResolver
    private var selectedTypes = 0
    var checkedTypes = 0
        private set
    private var patientsUri = Uri.EMPTY
    private var healingsUri = Uri.EMPTY
    private var paymentsUri = Uri.EMPTY
    var isExporting = false
        private set
    var exportUriCopy: Uri? = null
        private set
    private val exportUri = dataStore.getExportLocation().onEach { exportUriCopy = it }
    val exportUriFlow = exportUri
    private val allWorkObserver: LiveData<List<WorkInfo>> =
        workManager.getWorkInfosForUniqueWorkLiveData("backupWorker").map { list ->
            list.filter { it.state in setOf(WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED) }
        }
    private val showProgressInternal = MutableStateFlow(false)
    val showProgress: StateFlow<Boolean> = showProgressInternal.asStateFlow()

    val workObserver: LiveData<WorkInfo?> = allWorkObserver.map { list ->
        list.firstOrNull { it.state == WorkInfo.State.RUNNING } ?: list.firstOrNull()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    val exportLocation: Flow<String?> = exportUri.map { uri ->
        uri?.let {
            getFileName(it)
        }
    }
    private val documentFile = exportUri.map { uri ->
        uri?.let {
            Timber.d("Mapping export location for uri = $uri")
            DocumentFile.fromTreeUri(context, it)?.getChildDocumentFile()
        }
    }

    fun setExport(isExport: Boolean) {
        isExporting = isExport
    }

    fun selectType(type: BackupUtils.DataType) {
        checkedTypes = checkedTypes or type.mask
    }

    fun checkUri(uri: Uri): Boolean {
        contentResolver.query(
            uri, null, null, null, null, null
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return false
            Timber.d("Column names = ${cursor.columnNames.toList()}")

            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            val size = if (!cursor.isNull(sizeIndex)) cursor.getString(sizeIndex)
            else "Unknown"
            Timber.d("Size = $size")
            cursor.close()
            return true
        } ?: return false
    }

    fun isReadyForImport(): Boolean {
        return (checkedTypes == selectedTypes)
    }

    fun getFileName(uri: Uri): String? {
        contentResolver.query(
            uri, null, null, null, null, null
        )?.use { cursor ->
            Timber.d("Again Querying = $cursor")
            if (!cursor.moveToFirst()) return null
            val fileName =
                cursor.getStringOrNull(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            Timber.d("File Name = $fileName")
            cursor.close()
            return fileName
        } ?: return null
    }

    fun selectType(type: BackupUtils.DataType, uri: Uri) {
        selectedTypes = selectedTypes or type.mask
        when (type) {
            BackupUtils.DataType.Healings -> healingsUri = uri
            BackupUtils.DataType.Patients -> patientsUri = uri
            BackupUtils.DataType.Payments -> paymentsUri = uri
        }
    }

    fun deselectType(type: BackupUtils.DataType) {
        selectedTypes = selectedTypes xor type.mask
        checkedTypes = checkedTypes xor type.mask
        when (type) {
            BackupUtils.DataType.Healings -> healingsUri = Uri.EMPTY
            BackupUtils.DataType.Patients -> patientsUri = Uri.EMPTY
            BackupUtils.DataType.Payments -> paymentsUri = Uri.EMPTY
        }
    }

    fun setExportLocation(context: Context, uri: Uri) {
        contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        DocumentFile.fromTreeUri(context, uri)?.getChildDocumentFile()?.uri?.let {
            viewModelScope.launch(Dispatchers.IO) {
                dataStore.updateExportLocation(it)
                createBackup()
            }
        }
    }

    private fun DocumentFile.getChildDocumentFile(): DocumentFile? {
        return if (name?.startsWith(ExportFolderName) == false)
            findFile(ExportFolderName) ?: createDirectory(ExportFolderName)
        else this
    }

    private suspend fun buildFileUri(type: BackupUtils.DataType): Uri? {
        return withContext(Dispatchers.IO) {
            documentFile.firstOrNull()?.run {
                Timber.d("build file docfile = $name")
                @Suppress("BlockingMethodInNonBlockingContext")
                findFile(getBackupFileName(type))?.uri
                    ?: DocumentsContract.createDocument(
                        contentResolver,
                        uri,
                        "text/csv",
                        getBackupFileName(type)
                    )
            }
        }
    }

    private fun getBackupFileName(type: BackupUtils.DataType): String {
        return when (type) {
            BackupUtils.DataType.Healings -> "healings.csv"
            BackupUtils.DataType.Patients -> "patients.csv"
            BackupUtils.DataType.Payments -> "payments.csv"
        }
    }

    private suspend fun createBackup(): Boolean {
        if (checkedTypes == 0) return false
        val workData = Data.Builder().putInt(BackupUtils.Input.DATA_TYPE_KEY, checkedTypes)
        if (checkedTypes and BackupUtils.DataType.Patients.mask > 0) workData
            .putString(
                BackupUtils.Input.PATIENTS_FILE_URI_KEY,
                buildFileUri(BackupUtils.DataType.Patients).toString()
            )
        if (checkedTypes and BackupUtils.DataType.Healings.mask > 0) workData
            .putString(
                BackupUtils.Input.HEALINGS_FILE_URI_KEY,
                buildFileUri(BackupUtils.DataType.Healings).toString()
            )
        if (checkedTypes and BackupUtils.DataType.Payments.mask > 0) workData
            .putString(
                BackupUtils.Input.PAYMENTS_FILE_URI_KEY,
                buildFileUri(BackupUtils.DataType.Payments).toString()
            )
        val workRequest = OneTimeWorkRequestBuilder<ExportWorker>()
            .setInputData(workData.build())
            .addTag("exportWorker")
            .build()
        workManager.enqueueUniqueWork(
            "backupWorker",
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            workRequest
        )
        checkedTypes = 0
        showProgressInternal.value = true
        return true
    }

    fun importBackup(): Boolean {
        if (selectedTypes == 0) return false
        val workData = Data.Builder().putInt(ImportWorker.DATA_TYPE_KEY, selectedTypes)
        if (selectedTypes and BackupUtils.DataType.Patients.mask > 0) workData
            .putString(
                ImportWorker.PATIENTS_FILE_URI_KEY,
                patientsUri.toString()
            )
        if (selectedTypes and BackupUtils.DataType.Healings.mask > 0) workData
            .putString(
                ImportWorker.HEALINGS_FILE_URI_KEY,
                healingsUri.toString()
            )
        if (selectedTypes and BackupUtils.DataType.Payments.mask > 0) workData
            .putString(
                ImportWorker.PAYMENTS_FILE_URI_KEY,
                paymentsUri.toString()
            )
        val workRequest = OneTimeWorkRequestBuilder<ImportWorker>()
            .setInputData(workData.build())
            .addTag("importWorker")
            .build()
        workManager.enqueueUniqueWork(
            "backupWorker",
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            workRequest
        )
        showProgressInternal.value = true
        return true
    }

    fun resetShowProgress() {
        showProgressInternal.value = false
    }

    companion object {
        private const val ExportFolderName = "HealersDiary"
    }
}
