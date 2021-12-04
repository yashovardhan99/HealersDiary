package com.yashovardhan99.core.backup_restore

import android.annotation.SuppressLint
import android.app.Notification
import android.content.ContentResolver
import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.yashovardhan99.core.R
import com.yashovardhan99.core.backup_restore.BackupUtils.Input.DATA_TYPE_KEY
import com.yashovardhan99.core.backup_restore.BackupUtils.Input.HEALINGS_FILE_URI_KEY
import com.yashovardhan99.core.backup_restore.BackupUtils.Input.PATIENTS_FILE_URI_KEY
import com.yashovardhan99.core.backup_restore.BackupUtils.Input.PAYMENTS_FILE_URI_KEY
import com.yashovardhan99.core.backup_restore.BackupUtils.contains
import com.yashovardhan99.core.backup_restore.BackupUtils.plus
import com.yashovardhan99.core.database.DatabaseModule
import com.yashovardhan99.core.database.HealersDao
import com.yashovardhan99.core.database.HealersDataStore
import com.yashovardhan99.core.database.Healing
import com.yashovardhan99.core.database.ImportState
import com.yashovardhan99.core.database.Patient
import com.yashovardhan99.core.database.Payment
import com.yashovardhan99.core.toLocalDateTime
import com.yashovardhan99.core.utils.NotificationHelpers
import com.yashovardhan99.core.utils.NotificationHelpers.setContentDeepLink
import com.yashovardhan99.core.utils.NotificationHelpers.setForegroundCompat
import com.yashovardhan99.core.utils.NotificationHelpers.setTypeProgress
import com.yashovardhan99.core.utils.Request
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.FileNotFoundException
import java.io.IOException
import java.time.Instant
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class ImportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    val dataStore: HealersDataStore
) : CoroutineWorker(context, params) {
    private val patientMaps = mutableMapOf<Long, Long>()
    private val success = IntArray(3)
    private val failures = IntArray(3)
    private var errorBit = 0

    override suspend fun doWork(): Result {
        success.indices.forEach { success[it] = 0 }
        failures.indices.forEach { failures[it] = 0 }
        dataStore.updateImportState(ImportState.Running)
        errorBit = 0
        val dataType =
            inputData.getInt(DATA_TYPE_KEY, BackupUtils.DataType.Patients.mask)
        val contentResolver = applicationContext.contentResolver
        val healersDatabase = DatabaseModule.provideHealersDatabase(applicationContext)
        val healersDao = DatabaseModule.provideHealersDao(healersDatabase)
        patientMaps.clear()
        return withContext(Dispatchers.IO) {
            try {
                if (BackupUtils.DataType.Patients in dataType) {
                    importPatients(contentResolver, healersDao)
                }
                if (BackupUtils.DataType.Healings in dataType) {
                    importHealings(contentResolver, healersDao)
                }
                if (BackupUtils.DataType.Payments in dataType) {
                    importPayments(contentResolver, healersDao)
                }
                showDoneNotification(success.sum(), failures.sum(), errorBit == dataType)
                if (errorBit == dataType) Result.failure(
                    getProgressData(R.string.import_failed, null)
                )
                Result.success(getProgressData(R.string.import_completed, null))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                showDoneNotification(success.sum(), failures.sum(), true)
                Result.failure(getProgressData(R.string.import_failed, null))
            } catch (e: IOException) {
                e.printStackTrace()
                showDoneNotification(success.sum(), failures.sum(), true)
                Result.failure(getProgressData(R.string.import_failed, null))
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend inline fun import(
        contentResolver: ContentResolver,
        uriKey: String,
        @StringRes notificationMessage: Int,
        type: BackupUtils.DataType,
        insertRow: (List<String>) -> Boolean,
    ) {
        success[type.idx] = 0
        failures[type.idx] = 0
        updateProgress(notificationMessage, type)
        val fileUri = Uri.parse(inputData.getString(uriKey)) ?: throw FileNotFoundException()
        contentResolver.openInputStream(fileUri)?.use { inputStream ->
            val csvReader = CsvReader(inputStream)
            val headings = csvReader.parseRow()
            if (!verifyHeader(headings, type)) {
                errorBit += type
                inputStream.close()
                return
            }
            do {
                val row = csvReader.parseRow()
                if (row.isNullOrEmpty()) break // Reached EOF
                if (row.size != BackupUtils.getExpectedSize(type)) failures[type.idx] += 1
                else {
                    val result = insertRow(row)
                    if (result) success[type.idx] += 1
                    else failures[type.idx] += 1
                }
                updateProgress(notificationMessage, type)
            } while (row.isNotEmpty())
            inputStream.close()
        }
    }

    private suspend fun importPatients(contentResolver: ContentResolver, dao: HealersDao) {
        import(
            contentResolver,
            PATIENTS_FILE_URI_KEY,
            R.string.importing_patients,
            BackupUtils.DataType.Patients
        ) { row ->
            try {
                val patient = Patient(
                    row[0].toLong(), row[1],
                    row[2].toLong(), row[3].toLong(), row[4],
                    Date(row[5].toLong()), Date(row[6].toLong())
                )
                val id = dao.insertPatient(patient)
                patientMaps[patient.id] = id
                true
            } catch (e: NumberFormatException) {
                false
            }
        }
    }

    private suspend fun importHealings(contentResolver: ContentResolver, dao: HealersDao) {
        import(
            contentResolver,
            HEALINGS_FILE_URI_KEY,
            R.string.importing_healings,
            BackupUtils.DataType.Healings
        ) { row ->
            try {
                val patientId = patientMaps.getOrDefault(row[4].toLong(), row[4].toLong())
                if (dao.getPatient(patientId) == null) {
                    return@import false
                }
                val healing = Healing(
                    row[0].toLong(), Instant.ofEpochMilli(row[1].toLong()).toLocalDateTime(),
                    row[2].toLong(), row[3], patientId
                )
                dao.insertHealing(healing)
                true
            } catch (e: NumberFormatException) {
                false
            }
        }
    }

    private suspend fun importPayments(contentResolver: ContentResolver, dao: HealersDao) {
        import(
            contentResolver,
            PAYMENTS_FILE_URI_KEY,
            R.string.importing_payments,
            BackupUtils.DataType.Payments
        ) { row ->
            try {
                val patientId = patientMaps.getOrDefault(row[4].toLong(), row[4].toLong())
                if (dao.getPatient(patientId) == null) {
                    return@import false
                }
                val payment = Payment(
                    row[0].toLong(), Date(row[1].toLong()),
                    row[2].toLong(), row[3], patientId
                )
                dao.insertPayment(payment)
                true
            } catch (e: NumberFormatException) {
                false
            }
        }
    }

    private fun verifyHeader(
        headings: List<String>,
        type: BackupUtils.DataType
    ): Boolean {
        if (headings.size != BackupUtils.getExpectedSize(type)) return false
        val expectedHeaders = BackupUtils.getHeaders(type)
        headings.forEachIndexed { index: Int, heading: String ->
            if (heading != expectedHeaders[index]) return false
        }
        return true
    }

    private suspend fun updateProgress(
        @StringRes message: Int,
        currentType: BackupUtils.DataType?
    ) {
        val notification = NotificationHelpers.getDefaultNotification(
            applicationContext,
            NotificationHelpers.Channel.LocalImport
        ).setContentTitle(applicationContext.getString(R.string.importing_data))
            .setTypeProgress()
            .setProgress(0, 0, true)
            .setContentText(applicationContext.getString(message))
            .setContentDeepLink(
                applicationContext,
                Uri.parse(
                    "healersdiary://com.yashovardhan99.healersdiary/backup/progress?uuid=$id"
                ),
                PendingIntentReqCode
            ).build()

        @SuppressLint("InlinedApi")
        val foregroundServiceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        setForegroundCompat(
            NotificationHelpers.NotificationIds.LocalBackupProgress,
            notification,
            foregroundServiceType
        )
        setProgress(getProgressData(message, currentType))
    }

    private suspend fun showDoneNotification(success: Int, failed: Int, error: Boolean) {
        val notification = NotificationHelpers.getDefaultNotification(
            applicationContext,
            NotificationHelpers.Channel.LocalImport
        ).setCategory(if (error) Notification.CATEGORY_ERROR else Notification.CATEGORY_PROGRESS)
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    if (error) applicationContext.getString(R.string.something_went_wrong_importing)
                    else applicationContext.getString(
                        R.string.imported_successfully,
                        success,
                        success + failed
                    )
                )
            )
            .setContentTitle(
                applicationContext.getString(
                    if (error) R.string.import_failed else R.string.import_completed
                )
            )
            .setContentDeepLink(
                applicationContext,
                if (error) Uri.parse("healersdiary://com.yashovardhan99.healersdiary/backup")
                else Request.ViewDashboard.getUri(),
                PendingIntentReqCode
            )
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(NotificationHelpers.NotificationIds.LocalBackupCompleted, notification)

        // Save ImportState in dataStore
        if (error) dataStore.updateImportState(ImportState.LastRunFailed(errorBit))
        else dataStore.updateImportState(ImportState.LastRunSuccess)
    }

    private fun getProgressData(
        @StringRes message: Int,
        currentType: BackupUtils.DataType?
    ) = workDataOf(
        BackupUtils.Progress.ProgressMessage to applicationContext.getString(message),
        BackupUtils.Progress.RequiredBit to inputData.getInt(DATA_TYPE_KEY, 0),
        BackupUtils.Progress.CurrentBit to
                (currentType?.mask ?: BackupUtils.DataType.DoneMask),
        BackupUtils.Progress.ImportSuccess to success,
        BackupUtils.Progress.ImportFailure to failures,
        BackupUtils.Progress.InvalidFormatBit to errorBit,
        BackupUtils.Progress.Timestamp to System.currentTimeMillis()
    )

    companion object {
        const val PendingIntentReqCode = 30
    }
}
