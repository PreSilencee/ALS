package com.example.als.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.als.object.Variable;

public class OreoNotification extends ContextWrapper {

    private NotificationManagerCompat notificationManagerCompat;

    public OreoNotification(Context base){
        super(base);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {

        NotificationChannel channel = new NotificationChannel(Variable.MESSAGE_CHANNEL_ID,
                Variable.MESSAGE_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(Variable.MESSAGE_CHANNEL_DESC);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
//        channel.enableLights(false);
//        channel.enableVibration(true);
//        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
    }

    public NotificationManagerCompat getNotificationManagerCompat(){
        if(notificationManagerCompat == null){
            notificationManagerCompat = NotificationManagerCompat.from(this);
        }

        return notificationManagerCompat;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public NotificationCompat.Builder getOreoNotification(String title, String body,
                                                    PendingIntent pendingIntent, Uri soundUri, String icon){
        return new NotificationCompat.Builder(getApplicationContext(), Variable.MESSAGE_CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(Integer.parseInt(icon))
                .setSound(soundUri)
                .setAutoCancel(true);
    }

}
