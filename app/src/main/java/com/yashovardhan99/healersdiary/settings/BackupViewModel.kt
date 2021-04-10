package com.yashovardhan99.healersdiary.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.yashovardhan99.core.backup_restore.ExportWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(@ApplicationContext context: Context) : ViewModel() {
    private val workManager = WorkManager.getInstance(context)
    fun createBackup(type: ExportWorker.Companion.DataType, uri: Uri) {
        val workRequest = OneTimeWorkRequestBuilder<ExportWorker>()
            .setInputData(
                workDataOf(
                    ExportWorker.DATA_TYPE_KEY to type.mask,
                    ExportWorker.PATIENTS_FILE_URI_KEY to uri.toString()
                )
            )
            .addTag("exportWorker")
            .build()
        workManager.enqueueUniqueWork(
            "exportWorker",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
