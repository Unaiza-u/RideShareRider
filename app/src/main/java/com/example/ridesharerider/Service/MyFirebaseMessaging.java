package com.example.ridesharerider.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.ridesharerider.Common.Common;
import com.example.ridesharerider.MessageActivity;
import com.example.ridesharerider.Notification.OreoNotification;
import com.example.ridesharerider.R;
import com.example.ridesharerider.RateActivity;
import com.example.ridesharerider.Welcome;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getData().get("title");
        final String message = remoteMessage.getData().get("message");


        if (title != null && title.equals("Cancel")) {

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyFirebaseMessaging.this, message, Toast.LENGTH_SHORT).show();

                    FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl)
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //Toast.makeText(MyFirebaseMessaging.this, "pick up deleted", Toast.LENGTH_SHORT).show();
                        }
                    });

                    Common.rejectedDriver.add(Common.driverId);
                    Welcome.btnFind.setEnabled(true);

                    Common.driverId =  "";
                    Common.isDriverFound = false;
                    Common.againLoad = true;
                    Common.isAccepted = false;


                }
            });
        } else if (title != null && title.equals("Arrived")) {

            showArrivedNotification(remoteMessage,message, title);
        } else if (title != null && title.equals("DropOff")) {
            Common.driverId =  "";
            Common.isDriverFound = false;
            Common.againLoad = true;
            Common.isAccepted = false;
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                Welcome.btnFind.setVisibility(View.GONE);
                Welcome.btnShareRide.setVisibility(View.GONE);
                }
            });
            FirebaseDatabase.getInstance().getReference(Common.notification_ride_share)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(MyFirebaseMessaging.this, "notification has been deleted", Toast.LENGTH_SHORT).show();
                }
            });

            openRateActivity(message);

        } else if (title != null && title.equals("Accepted")) {

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyFirebaseMessaging.this, message, Toast.LENGTH_SHORT).show();

                    Common.isAccepted = true;

                    FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl)
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //Toast.makeText(MyFirebaseMessaging.this, "pick up deleted", Toast.LENGTH_SHORT).show();
                        }
                    });

                    Welcome.btnFind.setVisibility(View.GONE);
                    Welcome.btnFind.setEnabled(true);
                    Welcome.btnShareRide.setVisibility(View.VISIBLE);

                }
            });
        } else if (title != null && title.equals("New Message")) {

            String sented = remoteMessage.getData().get("sented");
            String user = remoteMessage.getData().get("user");

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            SharedPreferences preferences = getSharedPreferences("userlogindetail", MODE_PRIVATE);
            String currentUser = preferences.getString("currentuser", "none");

            if(firebaseUser != null && sented.equals(firebaseUser.getUid())){
                if (!currentUser.equals(user)) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        RemoteMessage.Notification notification = remoteMessage.getNotification();
                        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
                        Intent intent = new Intent(this, MessageActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("userid",user);
                        intent.putExtras(bundle);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, new Intent(), PendingIntent.FLAG_ONE_SHOT);
                        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                        OreoNotification oreoNotification = new OreoNotification(this);
                        Notification.Builder builder = oreoNotification.getOreoNotifications(title, message, pendingIntent,
                                defSoundUri, R.mipmap.ic_launcher_round);

                        int i = 0;
                        if (j > 0){
                            i = j;
                        }
                        oreoNotification.getManager().notify(i, builder.build());
                    } else {
                        RemoteMessage.Notification notification = remoteMessage.getNotification();
                        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
                        Intent intent = new Intent(this, MessageActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("userid",user);
                        intent.putExtras(bundle);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

                        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                                .setDefaults(Notification.DEFAULT_LIGHTS)
                                .setWhen(System.currentTimeMillis())
                                .setSmallIcon(R.mipmap.ic_launcher_round)
                                .setContentTitle(title)
                                .setContentText(message)
                                .setAutoCancel(true)
                                .setSound(defSoundUri)
                                .setContentIntent(pendingIntent);

                        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

                        int i = 0;
                        if (j > 0){
                            i = j;
                        }
                        notificationManager.notify(i, notificationBuilder.build());
                    }
                }
            }
        }

    }

    private void openRateActivity(String body) {
        Intent intent = new Intent(this, RateActivity.class);
        intent.putExtra("customerId", body);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showArrivedNotification(RemoteMessage remoteMessage, String body, String title) {

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            OreoNotification oreoNotification = new OreoNotification(this);
            Notification.Builder builder = oreoNotification.getOreoNotifications(title, body, pendingIntent,
                    defSoundUri, R.mipmap.ic_launcher_round);

            oreoNotification.getManager().notify(1, builder.build());
        } else {

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setDefaults(Notification.DEFAULT_LIGHTS)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setSound(defSoundUri)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(1, notificationBuilder.build());
        }
    }
}