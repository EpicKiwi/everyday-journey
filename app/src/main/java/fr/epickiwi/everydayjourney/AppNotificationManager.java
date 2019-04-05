package fr.epickiwi.everydayjourney;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class AppNotificationManager {

    public static String TRACKING_CHANNEL_ID = "TrackingService";

    static public void createTrackingChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = ctx.getString(R.string.trackingNotificationChannelTitle);
            String description = ctx.getString(R.string.trackingNotificationChannelDescription);
            int importance = NotificationManager.IMPORTANCE_MIN;

            NotificationChannel channel = new NotificationChannel(TRACKING_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}
