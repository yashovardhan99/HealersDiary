package com.yashovardhan99.core.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import com.yashovardhan99.core.R

object NotificationHelpers {
    fun getDefaultNotification(context: Context, channel: Channel): NotificationCompat.Builder {
        buildNotificationChannel(context, channel)
        return NotificationCompat.Builder(context, channel.id)
            .setDefaultParams(context)
    }

    private fun NotificationCompat.Builder.setDefaultParams(
        context: Context
    ): NotificationCompat.Builder {
        return setSmallIcon(R.drawable.ic_launcher_foreground).setColor(
            ContextCompat.getColor(
                context,
                R.color.colorPrimary
            )
        )
    }

    fun NotificationCompat.Builder.setTypeProgress(): NotificationCompat.Builder {
        return setCategory(Notification.CATEGORY_PROGRESS)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
    }

    fun NotificationCompat.Builder.setContentDeepLink(
        context: Context,
        uri: Uri,
        requestCode: Int,
        action: String = Intent.ACTION_VIEW,
        flags: Int = PendingIntent.FLAG_UPDATE_CURRENT
    ): NotificationCompat.Builder {
        val deepLink = TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(Intent(action, uri))
            .getPendingIntent(requestCode, flags)
        return setContentIntent(deepLink)
    }

    private fun buildNotificationChannel(context: Context, channel: Channel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManagerCompat.from(context)
                .createNotificationChannelGroup(channel.group.getNotificationChannelGroup(context))
            NotificationManagerCompat.from(context)
                .createNotificationChannel(channel.getNotificationChannel(context))
        }
    }

    private fun getForegroundInfo(
        notificationId: Int,
        notification: Notification,
        foregroundServiceType: Int
    ): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, foregroundServiceType)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    suspend fun CoroutineWorker.setForegroundCompat(
        notificationId: Int,
        notification: Notification,
        foregroundServiceType: Int
    ) {
        setForeground(getForegroundInfo(notificationId, notification, foregroundServiceType))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    sealed class Channel(
        val group: Group,
        val id: String,
        @StringRes private val name: Int,
        private val importance: Int
    ) {
        object LocalExport : Channel(
            Group.BackupSync,
            "local_export",
            R.string.export,
            NotificationManager.IMPORTANCE_HIGH
        )

        object LocalImport : Channel(
            Group.BackupSync,
            "local_import",
            R.string.import_text,
            NotificationManager.IMPORTANCE_HIGH
        )

        object FirebaseImport : Channel(
            Group.BackupSync,
            "online_import",
            R.string.online_import,
            NotificationManager.IMPORTANCE_HIGH
        )

        @RequiresApi(Build.VERSION_CODES.O)
        fun getNotificationChannel(context: Context): NotificationChannel {
            return NotificationChannel(id, context.getString(name), importance).also {
                it.group = group.id
            }
        }
    }

    sealed class Group(val id: String, @StringRes val name: Int) {
        object BackupSync : Group("backup_sync", R.string.backup_sync_group_name)

        @RequiresApi(Build.VERSION_CODES.O)
        fun getNotificationChannelGroup(context: Context): NotificationChannelGroup {
            return NotificationChannelGroup(id, context.getString(name))
        }
    }
}
