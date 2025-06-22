package io.diendanx.labbooking.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import io.diendanx.labbooking.activities.Cart.CartActivity;

public class CartNotificationService {
    public static final String CHANNEL_ID = "cart";
    public static final String CHANNEL_NAME = "Cart Notifications";
    public static final String CHANNEL_DESCRIPTION = "Notifications for cart updates, such as item additions or removals.";

    public static final int NOTIFICATION_ID = 1001;

    private final Context context;
    private final NotificationManager notificationManager;

    public CartNotificationService(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void showNotification(String title, String message) {
        var activityPendingIntent = PendingIntent.getActivity(
                context,
                1,
                new Intent(context, CartActivity.class),
                PendingIntent.FLAG_IMMUTABLE
        );
        var notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(io.diendanx.labbooking.R.drawable.ic_cart)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(activityPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        notificationManager.notify((int) System.currentTimeMillis(), notification);
    }
}
