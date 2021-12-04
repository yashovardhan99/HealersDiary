package com.yashovardhan99.core.backup_restore

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
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
import com.yashovardhan99.core.database.BackupState
import com.yashovardhan99.core.database.DatabaseModule
import com.yashovardhan99.core.database.HealersDao
import com.yashovardhan99.core.database.HealersDataStore
import com.yashovardhan99.core.database.Healing
import com.yashovardhan99.core.database.Patient
import com.yashovardhan99.core.database.Payment
import com.yashovardhan99.core.toEpochMilli
import com.yashovardhan99.core.utils.NotificationHelpers
import com.yashovardhan99.core.utils.NotificationHelpers.setContentDeepLink
import com.yashovardhan99.core.utils.NotificationHelpers.setForegroundCompat
import com.yashovardhan99.core.utils.NotificationHelpers.setTypeProgress
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.FileNotFoundException
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@HiltWorker
class ExportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    val dataStore: HealersDataStore
) : CoroutineWorker(context, params) {

    private val done = IntArray(3)
    private val total = IntArray(3)
    private var errorBit = 0
    override suspend fun doWork(): Result {
        done.indices.forEach { done[it] = 0 }
        total.indices.forEach { total[it] = 0 }
        errorBit = 0
        val dataType = inputData.getInt(DATA_TYPE_KEY, BackupUtils.DataType.Patients.mask)
        val contentResolver = applicationContext.contentResolver
        val healersDatabase = DatabaseModule.provideHealersDatabase(applicationContext)
        val healersDao = DatabaseModule.provideHealersDao(healersDatabase)
        return withContext(Dispatchers.IO) {
            try {
                dataStore.updateBackupState(BackupState.Running)
                if (BackupUtils.DataType.Patients in dataType) {
                    exportPatients(contentResolver, healersDao)
                }
                if (BackupUtils.DataType.Healings in dataType) {
                    exportHealings(contentResolver, healersDao)
                }
                if (BackupUtils.DataType.Payments in dataType) {
                    exportPayments(contentResolver, healersDao)
                }
                showDoneNotification(total.sum(), done.sum(), errorBit == dataType)
                if (errorBit == dataType) Result.failure(
                    getProgressData(R.string.export_failed, null)
                )
                else Result.success(getProgressData(R.string.export_completed, null))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                showDoneNotification(total.sum(), done.sum(), true)
                Result.failure(getProgressData(R.string.export_failed, null))
            } catch (e: IOException) {
                e.printStackTrace()
                showDoneNotification(total.sum(), done.sum(), true)
                Result.failure(getProgressData(R.string.export_failed, null))
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend inline fun <T> export(
        contentResolver: ContentResolver,
        uriKey: String,
        @StringRes notificationMessage: Int,
        header: String,
        items: List<T>,
        crossinline getCsvRow: (T) -> String
    ) {
        if (items.isNullOrEmpty()) return
        val type = when (items.firstOrNull()) {
            is Patient -> BackupUtils.DataType.Patients
            is Healing -> BackupUtils.DataType.Healings
            is Payment -> BackupUtils.DataType.Payments
            else -> throw IllegalArgumentException("Items received $items are not recognized")
        }
        done[type.idx] = 0
        total[type.idx] = items.size
        updateProgress(0, items.size, notificationMessage, type)
        val fileUri = Uri.parse(inputData.getString(uriKey)) ?: throw FileNotFoundException()
        contentResolver.openAssetFileDescriptor(fileUri, "w")?.use {
            it.createOutputStream().channel.truncate(0).close()
        }
        contentResolver.openOutputStream(fileUri)?.bufferedWriter()?.apply {
            appendLine(header)
            items.forEachIndexed { index, item ->
                done[type.idx] += 1
                appendLine(getCsvRow(item))
                updateProgress(index + 1, items.size, notificationMessage, type)
            }
            close()
        }?.close()
    }

    private suspend fun exportPatients(contentResolver: ContentResolver, dao: HealersDao) {
        try {
            export(
                contentResolver,
                PATIENTS_FILE_URI_KEY,
                R.string.exporting_patients,
                BackupUtils.getCsvRow(
                    Patient::id.name, Patient::name.name, Patient::charge.name, Patient::due.name,
                    Patient::notes.name, Patient::lastModified.name, Patient::created.name
                ),
                dao.getAllPatients().first(),
            ) { patient ->
                BackupUtils.getCsvRow(
                    patient.id,
                    patient.name,
                    patient.charge,
                    patient.due,
                    patient.notes,
                    patient.lastModified.toEpochMilli(),
                    patient.created.toEpochMilli()
                )
            }
        } catch (e: FileNotFoundException) {
            errorBit += BackupUtils.DataType.Patients
            updateProgress(
                0,
                0,
                R.string.exporting_patients_failed,
                BackupUtils.DataType.Patients
            )
        }
    }

    private suspend fun exportHealings(contentResolver: ContentResolver, dao: HealersDao) {
        try {
            export(
                contentResolver,
                HEALINGS_FILE_URI_KEY,
                R.string.exporting_healings,
                BackupUtils.getCsvRow(
                    Healing::id.name, Healing::time.name, Healing::charge.name,
                    Healing::notes.name, Healing::patientId.name
                ),
                dao.getAllHealings(),
            ) { healing ->
                BackupUtils.getCsvRow(
                    healing.id, healing.time.toEpochMilli(), healing.charge,
                    healing.notes, healing.patientId
                )
            }
        } catch (e: FileNotFoundException) {
            errorBit += BackupUtils.DataType.Healings
            updateProgress(
                0,
                0,
                R.string.exporting_healings_failed,
                BackupUtils.DataType.Healings
            )
        }
    }

    private suspend fun exportPayments(contentResolver: ContentResolver, dao: HealersDao) {
        try {
            export(
                contentResolver,
                PAYMENTS_FILE_URI_KEY,
                R.string.exporting_payments,
                BackupUtils.getCsvRow(
                    Payment::id.name, Payment::time.name, Payment::amount.name,
                    Payment::notes.name, Payment::patientId.name
                ),
                dao.getAllPayments(),
            ) { payment ->
                BackupUtils.getCsvRow(
                    payment.id, payment.time.time, payment.amount,
                    payment.notes, payment.patientId
                )
            }
        } catch (e: FileNotFoundException) {
            errorBit += BackupUtils.DataType.Payments
            updateProgress(
                0,
                0,
                R.string.exporting_payments_failed,
                BackupUtils.DataType.Payments
            )
        }
    }

    private suspend fun updateProgress(
        progress: Int,
        max: Int,
        @StringRes message: Int,
        currentType: BackupUtils.DataType
    ) {
        check(progress <= max)
        val notification = NotificationHelpers.getDefaultNotification(
            applicationContext,
            NotificationHelpers.Channel.LocalExport
        ).setContentTitle(applicationContext.getString(R.string.exporting_data))
            .setTypeProgress()
            .setProgress(max, progress, false)
            .setContentText(applicationContext.getString(message))
            .setContentDeepLink(
                applicationContext,
                Uri.parse(
                    "healersdiary://com.yashovardhan99.healersdiary/backup/progress?uuid=$id"
                ),
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
        setProgress(getProgressData(message, currentType))
    }

    private suspend fun showDoneNotification(max: Int, count: Int, failed: Boolean) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(inputData.getString(BackupUtils.Input.ExportFolderUriKey))
        )
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            PendingIntentReqCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationHelpers.getDefaultNotification(
            applicationContext,
            NotificationHelpers.Channel.LocalExport
        ).setCategory(if (failed) Notification.CATEGORY_ERROR else Notification.CATEGORY_PROGRESS)
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    if (failed)
                        applicationContext.getString(R.string.something_went_wrong_exporting)
                    else applicationContext.getString(R.string.exported_successfully, count, max)
                )
            )
            .setContentTitle(
                applicationContext
                    .getString(if (failed) R.string.export_failed else R.string.export_completed)
            )
            .run {
                if (failed) setContentDeepLink(
                    applicationContext,
                    Uri.parse("healersdiary://com.yashovardhan99.healersdiary/backup"),
                    PendingIntentReqCode
                ) else setContentIntent(pendingIntent)
            }.build()

        NotificationManagerCompat.from(applicationContext)
            .notify(NotificationHelpers.NotificationIds.LocalBackupCompleted, notification)

        // Save backup state in dataStore
        if (failed) dataStore.updateBackupState(BackupState.LastRunFailed(Instant.now()))
        else dataStore.updateBackupState(
            BackupState.LastRunSuccess(
                Instant.now(),
                done,
                Uri.parse(inputData.getString(BackupUtils.Input.ExportFolderUriKey))
            )
        )
    }

    private fun getProgressData(
        @StringRes message: Int,
        currentType: BackupUtils.DataType?
    ) = workDataOf(
        BackupUtils.Progress.ProgressMessage to applicationContext.getString(message),
        BackupUtils.Progress.RequiredBit to inputData.getInt(DATA_TYPE_KEY, 0),
        BackupUtils.Progress.CurrentBit to (currentType?.mask ?: BackupUtils.DataType.DoneMask),
        BackupUtils.Progress.ExportCounts to done,
        BackupUtils.Progress.ExportTotal to total,
        BackupUtils.Progress.FileErrorBit to errorBit,
        BackupUtils.Progress.Timestamp to System.currentTimeMillis()
    )

    companion object {
        const val PendingIntentReqCode = 30
    }
}
