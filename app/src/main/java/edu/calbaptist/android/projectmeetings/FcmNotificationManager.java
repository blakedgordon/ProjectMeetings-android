package edu.calbaptist.android.projectmeetings;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 *  FCM Notification Manager
 *  Assists with constructing new notifications.
 *
 *  @author Caleb Solorio
 *  @version 0.4.0 11/15/17
 */
public class FcmNotificationManager {
    public static final String TAG = "FcmNotificationManager";

    public static final int NOTIFICATION_ID = 0;

    private Context context;

    /**
     * Constructs a new FcmNotificationManager object.
     * @param context The context the generated notification should reference.
     */
    public FcmNotificationManager(Context context) {
        this.context = context;
    }

    /**
     * Builds and issues a notification.
     * @param from The FCM sender id
     * @param notification The notification text
     * @param intent The intent which the user will be directed to upon tapping the notification.
     */
    public void showNotification(String from, String notification, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Configure the notification builder
        String channelId = context.getString(R.string.notification_channel_id);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(notification)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Get an instance of the NotificationManager service
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification and issues it.
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
