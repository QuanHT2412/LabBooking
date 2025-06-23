package io.diendanx.labbooking;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import io.diendanx.labbooking.services.CartNotificationService;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize any libraries or services here if needed
        // For example, Firebase, Crashlytics, etc.
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        // create the notification channel for cart
        var cartChannel = new NotificationChannel(
                CartNotificationService.CHANNEL_ID,
                CartNotificationService.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );

        cartChannel.setDescription(CartNotificationService.CHANNEL_DESCRIPTION);

        // Register the channel with the system
        var notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(cartChannel);
    }


}
