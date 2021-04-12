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
import androidx.annotation.StringRes
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
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class ImportWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(context.getString(R.string.importing_data))
        .setCategory(Notification.CATEGORY_PROGRESS)
        .setColor(
            ContextCompat.getColor(
                applicationContext,
                R.color.colorPrimary
            )
        )
        .setOngoing(true)
        .setOnlyAlertOnce(true)

    private val patientMaps = mutableMapOf<Long, Long>()
    override suspend fun doWork(): Result {
        val dataType =
            inputData.getInt(DATA_TYPE_KEY, ExportWorker.Companion.DataType.Patients.mask)
        val contentResolver = applicationContext.contentResolver
        val healersDatabase = DatabaseModule.provideHealersDatabase(applicationContext)
        val healersDao = DatabaseModule.provideHealersDao(healersDatabase)
        patientMaps.clear()
        return withContext(Dispatchers.IO) {
            try {
                if (dataType and ExportWorker.Companion.DataType.Patients.mask > 0) {
                    val result = importPatients(contentResolver, healersDao)
                    Timber.d("Patients import result = $result")
                }
                if (dataType and ExportWorker.Companion.DataType.Healings.mask > 0) {
                    val result = importHealings(contentResolver, healersDao)
                    Timber.d("Healings import result = $result")
                }
                if (dataType and ExportWorker.Companion.DataType.Payments.mask > 0) {
                    val result = importPayments(contentResolver, healersDao)
                    Timber.d("Payments import result = $result")
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
    private suspend fun importPatients(
        contentResolver: ContentResolver,
        dao: HealersDao
    ): ImportResult {
        updateProgress("Importing Patients")
        val fileUri =
            Uri.parse(inputData.getString(PATIENTS_FILE_URI_KEY))
                ?: throw FileNotFoundException()
        var success = 0
        var failed = 0
        contentResolver.openInputStream(fileUri)?.use { inputStream ->
            val csvReader = CsvReader(inputStream)
            val headings = csvReader.parseRow()
            if (!verifyHeader(headings, ExportWorker.Companion.DataType.Patients)) {
                return ImportResult.InvalidFormat
            }
            do {
                val row = csvReader.parseRow()
                if (row.isNullOrEmpty()) break
                if (row.size != ExportUtils.getExpectedSize(
                        ExportWorker.Companion.DataType.Patients
                    )
                ) {
                    failed += 1
                } else {
                    try {
                        val patient = Patient(
                            row[0].toLong(),
                            row[1], row[2].toLong(), row[3].toLong(),
                            row[4], Date(row[5].toLong()), Date(row[6].toLong())
                        )
                        val id = dao.insertPatient(patient)
                        patientMaps[patient.id] = id
                        success += 1
                    } catch (e: NumberFormatException) {
                        failed += 1
                    } finally {
                        updateProgress("Importing Patients")
                    }
                }
            } while (row.isNotEmpty())
            inputStream.close()
        }
        if (success == 0) return if (failed > 0) ImportResult.InvalidFormat else ImportResult.Empty
        if (failed > 0) return ImportResult.PartialSuccess(success, failed)
        return ImportResult.Success(success)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun importHealings(
        contentResolver: ContentResolver,
        dao: HealersDao
    ): ImportResult {
        updateProgress("Importing Healings")
        val fileUri =
            Uri.parse(inputData.getString(HEALINGS_FILE_URI_KEY))
                ?: throw FileNotFoundException()
        var success = 0
        var failed = 0
        contentResolver.openInputStream(fileUri)?.use { inputStream ->
            val csvReader = CsvReader(inputStream)
            val headings = csvReader.parseRow()
            if (!verifyHeader(headings, ExportWorker.Companion.DataType.Healings)) {
                return ImportResult.InvalidFormat
            }
            do {
                val row = csvReader.parseRow()
                if (row.isNullOrEmpty()) break
                if (row.size != ExportUtils.getExpectedSize(
                        ExportWorker.Companion.DataType.Healings
                    )
                ) {
                    failed += 1
                } else {
                    try {
                        val patientId = patientMaps.getOrDefault(row[4].toLong(), row[4].toLong())
                        if (
                            dao.getPatient(patientId) == null
                        ) {
                            failed += 1
                            continue
                        }
                        val healing = Healing(
                            row[0].toLong(), Date(row[1].toLong()),
                            row[2].toLong(), row[3], patientId
                        )
                        dao.insertHealing(healing)
                        success += 1
                    } catch (e: NumberFormatException) {
                        failed += 1
                    } finally {
                        updateProgress("Importing Healings")
                    }
                }
            } while (row.isNotEmpty())
            inputStream.close()
        }
        if (success == 0) return if (failed > 0) ImportResult.InvalidFormat else ImportResult.Empty
        if (failed > 0) return ImportResult.PartialSuccess(success, failed)
        return ImportResult.Success(success)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun importPayments(
        contentResolver: ContentResolver,
        dao: HealersDao
    ): ImportResult {
        updateProgress("Importing Payments")
        val fileUri =
            Uri.parse(inputData.getString(PAYMENTS_FILE_URI_KEY))
                ?: throw FileNotFoundException()
        var success = 0
        var failed = 0
        contentResolver.openInputStream(fileUri)?.use { inputStream ->
            val csvReader = CsvReader(inputStream)
            val headings = csvReader.parseRow()
            if (!verifyHeader(headings, ExportWorker.Companion.DataType.Payments)) {
                return ImportResult.InvalidFormat
            }
            do {
                val row = csvReader.parseRow()
                if (row.isNullOrEmpty()) break
                if (row.size != ExportUtils.getExpectedSize(
                        ExportWorker.Companion.DataType.Payments
                    )
                ) {
                    failed += 1
                } else {
                    try {
                        val patientId = patientMaps.getOrDefault(row[4].toLong(), row[4].toLong())
                        if (
                            dao.getPatient(patientId) == null
                        ) {
                            failed += 1
                            continue
                        }
                        val payment = Payment(
                            row[0].toLong(), Date(row[1].toLong()),
                            row[2].toLong(), row[3], patientId
                        )
                        val id = dao.insertPayment(payment)
                        Timber.d("Payment inserted id = $id")
                        success += 1
                    } catch (e: NumberFormatException) {
                        failed += 1
                    } finally {
                        updateProgress("Importing Payments")
                    }
                }
            } while (row.isNotEmpty())
            inputStream.close()
        }
        if (success == 0) return if (failed > 0) ImportResult.InvalidFormat else ImportResult.Empty
        if (failed > 0) return ImportResult.PartialSuccess(success, failed)
        return ImportResult.Success(success)
    }

    private fun verifyHeader(
        headings: List<String>,
        type: ExportWorker.Companion.DataType
    ): Boolean {
        if (headings.size != ExportUtils.getExpectedSize(type)) return false
        val expectedHeaders = ExportUtils.getHeaders(type)
        headings.forEachIndexed { index: Int, heading: String ->
            if (heading != expectedHeaders[index]) return false
        }
        return true
    }

    private suspend fun updateProgress(
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
        val notification = notificationBuilder.setProgress(100, 30, true)
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
                PROGRESS_TEXT_KEY to message
            )
        )
    }

    private fun buildNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelGroup =
                NotificationChannelGroup(GROUP_ID, applicationContext.getString(GROUP_NAME))
            val notificationChannel = NotificationChannel(
                CHANNEL_ID, applicationContext.getString(CHANNEL_NAME),
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
        const val CHANNEL_ID = "import_export_import"
        const val GROUP_ID = "backup_sync"
        const val PROGRESS_NOTIF_ID = 300
        val CHANNEL_NAME = R.string.import_text
        val GROUP_NAME = R.string.backup_sync_group_name
        const val PROGRESS_TEXT_KEY = "progress_text"
        const val PendingIntentReqCode = 30

        sealed class ImportResult(@StringRes val message: Int) {
            object InvalidFormat : ImportResult(R.string.invalid_format)
            object Empty : ImportResult(R.string.empty_file)
            data class PartialSuccess(val successful: Int, val failed: Int) :
                ImportResult(R.string.partially_successful)

            data class Success(val rowsAdded: Int) : ImportResult(R.string.successful)
        }
    }
}
