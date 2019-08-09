package com.yashovardhan99.healersdiary.helpers

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Created by Yashovardhan99 on 4/11/18 as a part of HealersDiary.
 */
class MyNotificationPublisher : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NOTIFPUBLISHER", "Received")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Log.d("NOTIFPUBLISHER", notificationManager.toString())
        val notification = intent.getParcelableExtra<Notification>(NOTIFICATION)
        val notificationId = intent.getIntExtra(NOTIFICATION_ID, 0)
        Log.d("NOTIFPUBLISHER: NOTIFID", notificationId.toString())
        notificationManager.notify(notificationId, notification)
    }

    companion object {
        val NOTIFICATION_ID = "notifID"
        val NOTIFICATION = "notif"
    }
}
