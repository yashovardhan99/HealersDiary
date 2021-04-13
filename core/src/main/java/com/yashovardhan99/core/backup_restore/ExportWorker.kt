package com.yashovardhan99.core.backup_restore

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import androidx.annotation.IntRange
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.yashovardhan99.core.R
import com.yashovardhan99.core.database.DatabaseModule
import com.yashovardhan99.core.database.HealersDao
import com.yashovardhan99.core.database.Healing
import com.yashovardhan99.core.database.Patient
import com.yashovardhan99.core.database.Payment
import com.yashovardhan99.core.utils.NotificationHelpers
import com.yashovardhan99.core.utils.NotificationHelpers.setContentDeepLink
import com.yashovardhan99.core.utils.NotificationHelpers.setForegroundCompat
import com.yashovardhan99.core.utils.NotificationHelpers.setTypeProgress
import java.io.FileNotFoundException
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ExportWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val dataType = inputData.getInt(DATA_TYPE_KEY, BackupUtils.DataType.Patients.mask)
        val contentResolver = applicationContext.contentResolver
        val healersDatabase = DatabaseModule.provideHealersDatabase(applicationContext)
        val healersDao = DatabaseModule.provideHealersDao(healersDatabase)
        return withContext(Dispatchers.IO) {
            try {
                if (dataType and BackupUtils.DataType.Patients.mask > 0) {
                    exportPatients(contentResolver, healersDao)
                }
                if (dataType and BackupUtils.DataType.Healings.mask > 0) {
                    exportHealings(contentResolver, healersDao)
                }
                if (dataType and BackupUtils.DataType.Payments.mask > 0) {
                    exportPayments(contentResolver, healersDao)
                }
                Result.success()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Result.failure()
            } catch (e: IOException) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend inline fun <T> export(
        contentResolver: ContentResolver,
        uriKey: String,
        notificationMessage: String,
        header: String,
        items: List<T>,
        crossinline getCsvRow: (T) -> String
    ) {
        updateProgress(0, notificationMessage)
        val fileUri = Uri.parse(inputData.getString(uriKey)) ?: throw FileNotFoundException()
        withContext(Dispatchers.IO) {
            contentResolver.openAssetFileDescriptor(fileUri, "w")?.use {
                it.createOutputStream().channel.truncate(0).close()
            }
            contentResolver.openOutputStream(fileUri)?.bufferedWriter()?.apply {
                appendLine(header)
                items.forEachIndexed { index, item ->
                    appendLine(getCsvRow(item))
                    updateProgress((index + 1) * 100 / items.size, notificationMessage)
                }
                close()
            }?.close()
        }
    }

    private suspend fun exportPatients(contentResolver: ContentResolver, dao: HealersDao) {
        export(
            contentResolver,
            PATIENTS_FILE_URI_KEY,
            "Exporting Patients",
            BackupUtils.getCsvRow(
                Patient::id.name, Patient::name.name, Patient::charge.name, Patient::due.name,
                Patient::notes.name, Patient::lastModified.name, Patient::created.name
            ),
            dao.getAllPatients().first()
        ) { patient ->
            BackupUtils.getCsvRow(
                patient.id, patient.name, patient.charge,
                patient.due, patient.notes, patient.lastModified.time, patient.created.time
            )
        }
    }

    private suspend fun exportHealings(contentResolver: ContentResolver, dao: HealersDao) {
        export(
            contentResolver,
            HEALINGS_FILE_URI_KEY,
            "Exporting Healings",
            BackupUtils.getCsvRow(
                Healing::id.name, Healing::time.name, Healing::charge.name,
                Healing::notes.name, Healing::patientId.name
            ),
            dao.getAllHealings()
        ) { healing ->
            BackupUtils.getCsvRow(
                healing.id, healing.time.time, healing.charge,
                healing.notes, healing.patientId
            )
        }
    }

    private suspend fun exportPayments(contentResolver: ContentResolver, dao: HealersDao) {
        export(
            contentResolver,
            PAYMENTS_FILE_URI_KEY,
            "Exporting Payments",
            BackupUtils.getCsvRow(
                Payment::id.name, Payment::time.name, Payment::amount.name,
                Payment::notes.name, Payment::patientId.name
            ),
            dao.getAllPayments()
        ) { payment ->
            BackupUtils.getCsvRow(
                payment.id, payment.time.time, payment.amount,
                payment.notes, payment.patientId
            )
        }
    }

    private suspend fun updateProgress(
        @IntRange(from = 0, to = 100) progress: Int,
        message: String
    ) {
        val notification = NotificationHelpers.getDefaultNotification(
            applicationContext,
            NotificationHelpers.Channel.LocalExport
        ).setContentTitle(applicationContext.getString(R.string.exporting_data))
            .setTypeProgress()
            .setProgress(100, progress, false)
            .setContentText(message)
            .setContentDeepLink(
                applicationContext,
                Uri.parse("healersdiary://com.yashovardhan99.healersdiary/backup/progress"),
                PendingIntentReqCode
            )
            .build()

        @SuppressLint("InlinedApi")
        val foregroundServiceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        setForegroundCompat(
            NotificationHelpers.NotificationIds.LocalBackupProgress,
            notification,
            foregroundServiceType
        )
        setProgress(
            workDataOf(
                PROGRESS_PERCENT_KEY to progress,
                PROGRESS_TEXT_KEY to message
            )
        )
    }

    companion object {
        const val PATIENTS_FILE_URI_KEY = "patient_file_uri"
        const val HEALINGS_FILE_URI_KEY = "healings_file_uri"
        const val PAYMENTS_FILE_URI_KEY = "payments_file_uri"
        const val DATA_TYPE_KEY = "data_type"
        const val PROGRESS_PERCENT_KEY = "progress_percent"
        const val PROGRESS_TEXT_KEY = "progress_text"
        const val PendingIntentReqCode = 30
    }
}
