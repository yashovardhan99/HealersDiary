package com.yashovardhan99.healersdiary.settings

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.database.getStringOrNull
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.yashovardhan99.core.backup_restore.ExportWorker
import com.yashovardhan99.core.backup_restore.ImportWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import timber.log.Timber

@HiltViewModel
class BackupViewModel @Inject constructor(@ApplicationContext context: Context) : ViewModel() {
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

    fun setExport(isExport: Boolean) {
        isExporting = isExport
    }

    fun selectType(type: ExportWorker.Companion.DataType) {
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

    fun selectType(type: ExportWorker.Companion.DataType, uri: Uri) {
        selectedTypes = selectedTypes or type.mask
        when (type) {
            ExportWorker.Companion.DataType.Healings -> healingsUri = uri
            ExportWorker.Companion.DataType.Patients -> patientsUri = uri
            ExportWorker.Companion.DataType.Payments -> paymentsUri = uri
        }
        if (selectedTypes == checkedTypes) {
            if (isExporting) createBackup()
        }
    }

    fun deselectType(type: ExportWorker.Companion.DataType) {
        selectedTypes = selectedTypes xor type.mask
        checkedTypes = checkedTypes xor type.mask
        when (type) {
            ExportWorker.Companion.DataType.Healings -> healingsUri = Uri.EMPTY
            ExportWorker.Companion.DataType.Patients -> patientsUri = Uri.EMPTY
            ExportWorker.Companion.DataType.Payments -> paymentsUri = Uri.EMPTY
        }
    }

    private fun createBackup(): Boolean {
        if (selectedTypes == 0) return false
        val workData = Data.Builder().putInt(ExportWorker.DATA_TYPE_KEY, selectedTypes)
        if (selectedTypes and ExportWorker.Companion.DataType.Patients.mask > 0) workData
            .putString(
                ExportWorker.PATIENTS_FILE_URI_KEY,
                patientsUri.toString()
            )
        if (selectedTypes and ExportWorker.Companion.DataType.Healings.mask > 0) workData
            .putString(
                ExportWorker.HEALINGS_FILE_URI_KEY,
                healingsUri.toString()
            )
        if (selectedTypes and ExportWorker.Companion.DataType.Payments.mask > 0) workData
            .putString(
                ExportWorker.PAYMENTS_FILE_URI_KEY,
                paymentsUri.toString()
            )
        val workRequest = OneTimeWorkRequestBuilder<ExportWorker>()
            .setInputData(workData.build())
            .addTag("exportWorker")
            .build()
        workManager.enqueueUniqueWork(
            "exportWorker",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        return true
    }

    fun importBackup(): Boolean {
        if (selectedTypes == 0) return false
        val workData = Data.Builder().putInt(ImportWorker.DATA_TYPE_KEY, selectedTypes)
        if (selectedTypes and ExportWorker.Companion.DataType.Patients.mask > 0) workData
            .putString(
                ImportWorker.PATIENTS_FILE_URI_KEY,
                patientsUri.toString()
            )
        if (selectedTypes and ExportWorker.Companion.DataType.Healings.mask > 0) workData
            .putString(
                ImportWorker.HEALINGS_FILE_URI_KEY,
                healingsUri.toString()
            )
        if (selectedTypes and ExportWorker.Companion.DataType.Payments.mask > 0) workData
            .putString(
                ImportWorker.PAYMENTS_FILE_URI_KEY,
                paymentsUri.toString()
            )
        val workRequest = OneTimeWorkRequestBuilder<ImportWorker>()
            .setInputData(workData.build())
            .addTag("importWorker")
            .build()
        workManager.enqueueUniqueWork(
            "importWorker",
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            workRequest
        )
        return true
    }
}
