package edu.calbaptist.android.projectmeetings.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import edu.calbaptist.android.projectmeetings.R;

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

    private static Context sContext;

    /**
     * Constructs a new FcmNotificationManager object.
     * @param context The context the generated notification should reference.
     */
    public FcmNotificationManager(Context context) {
        this.sContext = context;
    }

    /**
     * Builds and issues a notification.
     * @param title The FCM sender id
     * @param body The notification text
     * @param intent The intent which the user will be directed to upon tapping the notification.
     */
    public void showNotification(String title, String body, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                sContext,
                NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Configure the notification builder
        String channelId = sContext.getString(R.string.notification_channel_id);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(sContext, channelId)
                .setSmallIcon(R.drawable.ic_clapping_hands)
                .setColor(ContextCompat.getColor(sContext, R.color.color_accent))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Get an instance of the NotificationManager service
        NotificationManager mNotificationManager =
                (NotificationManager) sContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification and issues it.
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}