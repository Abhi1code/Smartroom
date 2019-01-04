package com.smartroom.smartroom;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.IntentFilter;
import android.os.Build;

public class App extends Application {
    public static final String CHANNEL_ID = "notification_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        create_channel();
    }

    private void create_channel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Foreground service channel", NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
