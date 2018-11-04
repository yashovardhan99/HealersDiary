package com.yashovardhan99.healersdiary.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Yashovardhan99 on 4/11/18 as a part of HealersDiary.
 */
public class MyNotificationPublisher extends BroadcastReceiver {

    public final static String NOTIFICATION_ID = "notifID";
    public final static String NOTIFICATION = "notif";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NOTIFPUBLISHER", "Received");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d("NOTIFPUBLISHER", notificationManager.toString());
        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);
        Log.d("NOTIFPUBLISHER: NOTIFID", String.valueOf(notificationId));
        notificationManager.notify(notificationId, notification);
    }
}
