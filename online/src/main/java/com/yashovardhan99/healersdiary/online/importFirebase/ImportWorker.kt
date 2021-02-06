package com.yashovardhan99.healersdiary.online.importFirebase

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.yashovardhan99.healersdiary.R
import kotlinx.coroutines.delay

class ImportWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        buildNotificationChannels()
        setForeground(getForegroundInfo(0))
        setProgress(workDataOf("progress" to 0))
        delay(1000)
        val user = Firebase.auth.currentUser
        setForeground(getForegroundInfo(50))
        setProgress(workDataOf("progress" to 50))
        delay(1000)
        setForeground(getForegroundInfo(100))
        setProgress(workDataOf("progress" to 100))
        return Result.success()
    }

    private fun getForegroundInfo(progress: Int): ForegroundInfo {
        val notification = getNotificationBuilder()
                .setProgress(MAX_PROGRESS, progress, false)
                .build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(1, notification)
        }
    }

    private fun getNotificationBuilder() = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Import from v1.0")

    private fun buildNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelGroup = NotificationChannelGroup(GROUP_ID, GROUP_NAME)
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.group = channelGroup.id
            NotificationManagerCompat.from(applicationContext).createNotificationChannelGroup(channelGroup)
            NotificationManagerCompat.from(applicationContext).createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        const val MAX_PROGRESS = 100
        const val GROUP_ID = "online"
        const val GROUP_NAME = "Backup & Sync"
        const val CHANNEL_ID = "online_import"
        const val CHANNEL_NAME = "Import"
    }
}