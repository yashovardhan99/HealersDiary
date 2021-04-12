package com.yashovardhan99.core.backup_restore

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import androidx.annotation.IntRange
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.yashovardhan99.core.R
import com.yashovardhan99.core.database.DatabaseModule
import com.yashovardhan99.core.database.HealersDao
import com.yashovardhan99.core.database.Healing
import com.yashovardhan99.core.database.Patient
import com.yashovardhan99.core.database.Payment
import java.io.FileNotFoundException
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ExportWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(context.getString(R.string.exporting_data))
        .setCategory(Notification.CATEGORY_PROGRESS)
        .setColor(
            ContextCompat.getColor(
                applicationContext,
                R.color.colorPrimary
            )
        )
        .setOngoing(true)
        .setOnlyAlertOnce(true)

    override suspend fun doWork(): Result {
        val dataType = inputData.getInt(DATA_TYPE_KEY, DataType.Patients.mask)
        val contentResolver = applicationContext.contentResolver
        val healersDatabase = DatabaseModule.provideHealersDatabase(applicationContext)
        val healersDao = DatabaseModule.provideHealersDao(healersDatabase)
        return withContext(Dispatchers.IO) {
            try {
                if (dataType and DataType.Patients.mask > 0) {
                    exportPatients(contentResolver, healersDao)
                }
                if (dataType and DataType.Healings.mask > 0) {
                    exportHealings(contentResolver, healersDao)
                }
                if (dataType and DataType.Payments.mask > 0) {
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
            ExportUtils.getCsvRow(
                Patient::id.name, Patient::name.name, Patient::charge.name, Patient::due.name,
                Patient::notes.name, Patient::lastModified.name, Patient::created.name
            ),
            dao.getAllPatients().first()
        ) { patient ->
            ExportUtils.getCsvRow(
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
            ExportUtils.getCsvRow(
                Healing::id.name, Healing::time.name, Healing::charge.name,
                Healing::notes.name, Healing::patientId.name
            ),
            dao.getAllHealings()
        ) { healing ->
            ExportUtils.getCsvRow(
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
            ExportUtils.getCsvRow(
                Payment::id.name, Payment::time.name, Payment::amount.name,
                Payment::notes.name, Payment::patientId.name
            ),
            dao.getAllPayments()
        ) { payment ->
            ExportUtils.getCsvRow(
                payment.id, payment.time.time, payment.amount,
                payment.notes, payment.patientId
            )
        }
    }

    private suspend fun updateProgress(
        @IntRange(from = 0, to = 100) progress: Int,
        message: String
    ) {
        buildNotificationChannel()
        val deepLink = TaskStackBuilder.create(applicationContext)
            .addNextIntentWithParentStack(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("healersdiary://com.yashovardhan99.healersdiary/backup/progress"),
                )
            ).getPendingIntent(PendingIntentReqCode, PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = notificationBuilder.setProgress(100, progress, false)
            .setContentText(message)
            .setContentIntent(deepLink)
            .build()
        val foregroundInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                PROGRESS_NOTIF_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(PROGRESS_NOTIF_ID, notification)
        }
        setForeground(foregroundInfo)
        setProgress(
            workDataOf(
                PROGRESS_PERCENT_KEY to progress,
                PROGRESS_TEXT_KEY to message
            )
        )
    }

    private fun buildNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelGroup =
                NotificationChannelGroup(GROUP_ID, applicationContext.getString(GROUP_NAME))
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                applicationContext.getString(CHANNEL_NAME),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.group = channelGroup.id
            NotificationManagerCompat.from(applicationContext)
                .createNotificationChannelGroup(channelGroup)
            NotificationManagerCompat.from(applicationContext)
                .createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        const val PATIENTS_FILE_URI_KEY = "patient_file_uri"
        const val HEALINGS_FILE_URI_KEY = "healings_file_uri"
        const val PAYMENTS_FILE_URI_KEY = "payments_file_uri"
        const val DATA_TYPE_KEY = "data_type"
        const val CHANNEL_ID = "import_export_export"
        const val GROUP_ID = "backup_sync"
        const val PROGRESS_NOTIF_ID = 300
        val CHANNEL_NAME = R.string.export
        val GROUP_NAME = R.string.backup_sync_group_name
        const val PROGRESS_PERCENT_KEY = "progress_percent"
        const val PROGRESS_TEXT_KEY = "progress_text"
        const val PendingIntentReqCode = 30

        sealed class DataType(val mask: Int) {
            object Patients : DataType(1 shl 0)
            object Healings : DataType(1 shl 1)
            object Payments : DataType(1 shl 2)
        }
    }
}
