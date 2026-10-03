package com.yomy.counting;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Central notification layer.
 * Remote push providers should call this layer after receiving a payload.
 * This class deliberately contains no provider secret.
 */
public final class NotificationCenter {
    public static final String CHANNEL_MESSAGES = "messages";
    public static final String CHANNEL_CALLS = "calls";
    public static final String CHANNEL_FEDO = "fedo";
    public static final String CHANNEL_SYSTEM = "system";

    private final Context context;

    public NotificationCenter(Context context) {
        this.context = context.getApplicationContext();
        createChannels();
    }

    public void createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;

        manager.createNotificationChannel(new NotificationChannel(
                CHANNEL_MESSAGES, "Messages", NotificationManager.IMPORTANCE_HIGH));
        manager.createNotificationChannel(new NotificationChannel(
                CHANNEL_CALLS, "Calls", NotificationManager.IMPORTANCE_HIGH));
        manager.createNotificationChannel(new NotificationChannel(
                CHANNEL_FEDO, "Fedo", NotificationManager.IMPORTANCE_DEFAULT));
        manager.createNotificationChannel(new NotificationChannel(
                CHANNEL_SYSTEM, "YOMY", NotificationManager.IMPORTANCE_DEFAULT));
    }

    public boolean canPost() {
        return Build.VERSION.SDK_INT < 33 ||
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED;
    }

    public void show(String channelId, int notificationId, String title, String body) {
        if (!canPost()) return;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(com.yomy.counting.R.drawable.ic_yomy)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true);
        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }
}
