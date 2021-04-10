package com.yashovardhan99.core.backup_restore

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import androidx.annotation.IntRange
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.yashovardhan99.core.R
import com.yashovardhan99.core.database.DatabaseModule
import com.yashovardhan99.core.database.HealersDao
import com.yashovardhan99.core.database.Patient
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
    private suspend fun exportPatients(contentResolver: ContentResolver, dao: HealersDao) {
        updateProgress(0, "Exporting Patients")
        val fileUri =
            Uri.parse(inputData.getString(PATIENTS_FILE_URI_KEY)) ?: throw FileNotFoundException()
        contentResolver.openOutputStream(fileUri)?.use { outputStream ->
            outputStream.bufferedWriter().apply {
                appendLine(
                    "${Patient::id.name},${Patient::charge.name},${Patient::charge.name}," +
                        "${Patient::due.name},${Patient::notes.name}," +
                        "${Patient::lastModified.name},${Patient::created.name}"
                )
                val patients = dao.getAllPatients().first()
                patients.forEachIndexed { index, patient ->
                    appendLine(
                        "${patient.id},${patient.name},${patient.charge}," +
                            "${patient.due},${patient.notes}," +
                            "${patient.lastModified},${patient.created}"
                    )
                    updateProgress((index + 1) * 100 / patients.size, "Exporting Patients")
                }
                close()
            }
            outputStream.close()
        }
    }

    private suspend fun updateProgress(
        @IntRange(from = 0, to = 100) progress: Int,
        message: String
    ) {
        val notification = notificationBuilder.setProgress(100, progress, false)
            .setContentText(message)
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

        sealed class DataType(val mask: Int) {
            object Patients : DataType(1 shl 0)
            object Healings : DataType(1 shl 1)
            object Payments : DataType(1 shl 2)
        }
    }
}
