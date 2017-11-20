package edu.calbaptist.android.projectmeetings;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 *  Messeging Service
 *  Handles FCM remote messages.
 *
 *  @author Caleb Solorio
 *  @version 0.4.0 11/15/17
 */
public class MessagingService extends FirebaseMessagingService {
    private static final String TAG = "MessagingService";

    /**
     * Recieves an FCM remote message and handles it accoordingly based on its notification/data payload.
     * @param remoteMessage The FCM message recieved.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            // Create a notification to display while still in the app (may or may not be useful)
            notifyUser(remoteMessage.getFrom(), remoteMessage.getNotification().getBody());
        }

        // TODO: Add response different notifications.

        String type = remoteMessage.getData().get("type");

        switch (type) {
            case "meeting_invite":
                // Do something
                break;
            case "meeting_warn":
                // Do something
                break;
            case "meeting_start":
                // Do something
                break;
        }

    }


    /**
     * Signals the FcmNotification Manager to build a custom notification.
     * @param from The id of the notification sender
     * @param notification The notification itself
     */
    private void notifyUser(String from, String notification) {
        FcmNotificationManager mNotificationManager = new FcmNotificationManager(getApplicationContext());
        mNotificationManager.showNotification(from, notification,
                new Intent(getApplicationContext(), MainActivity.class));
    }
}
