package com.example.ridesharerider.Notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.firebase.messaging.RemoteMessage;

public class OreoNotification extends ContextWrapper {

    private static final String CHANNEL_ID = "com.example.tryfit";
    private static final String CHANNEL_NAME = "tryfit";

    private NotificationManager notificationManager;


    public OreoNotification(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.enableLights(false);
        notificationChannel.enableVibration(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(notificationChannel);
    }
    public NotificationManager getManager(){
        if (notificationManager == null){
            notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }
    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getOreoNotifications(String title,
                                                     String body,
                                                     PendingIntent pIntent,
                                                     Uri soundUri,
                                                     int icon){

        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentIntent(pIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(icon)
                .setSound(soundUri)
                .setAutoCancel(true);

    }
}
