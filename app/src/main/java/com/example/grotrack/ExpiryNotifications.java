package com.example.grotrack;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import java.util.List;

final class ExpiryNotifications {
    static final String CHANNEL_ID="grotrack_expiry_alerts";
    private ExpiryNotifications() { }

    static void createChannel(Context context) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            NotificationChannel channel=new NotificationChannel(CHANNEL_ID,"Expiry alerts",NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Alerts for groceries that are expiring soon or have expired with stock remaining.");
            NotificationManager manager=context.getSystemService(NotificationManager.class);
            if(manager!=null)manager.createNotificationChannel(channel);
        }
    }

    static boolean canNotify(Context context) {
        if(Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return false;
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    static void show(Context context,String title,String message) {
        if(!canNotify(context))return;
        createChannel(context);
        NotificationCompat.Builder notification=new NotificationCompat.Builder(context,CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title).setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(PendingIntent.getActivity(context,0,new Intent(context,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true);
        try { NotificationManagerCompat.from(context).notify(4101,notification.build()); }
        catch(SecurityException ignored) { /* Android notification access may be revoked between the check and post. */ }
    }
}
