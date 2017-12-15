package edu.calbaptist.android.projectmeetings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import edu.calbaptist.android.projectmeetings.exceptions.RestClientException;

/**
 *  Messeging Service
 *  Handles FCM remote messages.
 *
 *  @author Caleb Solorio
 *  @version 0.4.0 11/15/17
 */
public class MessagingService extends FirebaseMessagingService {
    private static final String TAG = "MessagingService";

    final static SharedPreferences prefs = App.context.getSharedPreferences(
            "edu.calbaptist.android.projectmeetings.Account_Name",
            Context.MODE_PRIVATE);

    @Override
    public void handleIntent(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            Bundle extras = intent.getExtras();
            Log.d(TAG, "onCreate: " +  extras.toString());

            String type = extras.getString("type");

            switch (type) {
                case "meeting_invite":
                    notifyUserToMeetingListActvity(extras.getString("title"),
                            extras.getString("body"));
                    break;
                case "meeting_warn":
                    notifyUserToMeetingActivity(extras.getString("title"),
                            extras.getString("body"),
                            extras.getString("m_id"));
                    break;
                case "meeting_start":
                    notifyUserToMeetingActivity(extras.getString("title"),
                            extras.getString("body"),
                            extras.getString("m_id"));
                    break;
            }
        }
    }

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

            String type = remoteMessage.getData().get("type");

            switch (type) {
                case "meeting_invite":
                    notifyUserToMeetingListActvity(remoteMessage.getNotification().getTitle(),
                            remoteMessage.getNotification().getBody());
                    break;
                case "meeting_warn":
                    notifyUserToMeetingActivity(remoteMessage.getNotification().getTitle(),
                            remoteMessage.getNotification().getBody(),
                            remoteMessage.getData().get("m_id"));
                    break;
                case "meeting_start":
                    notifyUserToMeetingActivity(remoteMessage.getNotification().getTitle(),
                            remoteMessage.getNotification().getBody(),
                            remoteMessage.getData().get("m_id"));
                    break;
            }
        }
    }


    /**
     * Signals the FcmNotification Manager to build a custom notification leading to MeetingListActivity.
     * @param title The id of the notification sender
     * @param body The notification itself
     */
    private void notifyUserToMeetingListActvity(String title, String body) {
        FcmNotificationManager mNotificationManager = new FcmNotificationManager(getApplicationContext());
        mNotificationManager.showNotification(title, body,
                new Intent(getApplicationContext(), MeetingListActivity.class));
    }

    /**
     * Signals the FcmNotification Manager to build a custom notification leading to MeetingActivity.
     * @param title The id of the notification sender
     * @param body The notification itself
     * @param mId The id of the meeting
     */
    private void notifyUserToMeetingActivity(final String title, final String body, final String mId) {
        final String token = prefs.getString("firebase_token", null);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                RestClient.getMeeting(mId, token, new Callback.RestClientMeeting() {
                    @Override
                    void onTaskExecuted(final Meeting meeting) {
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                RestClient.updateUser(new User.UserBuilder().build(),
                                        token, new Callback.RestClientUser() {
                                    @Override
                                    void onTaskExecuted(User user) {
                                        FcmNotificationManager mNotificationManager =
                                                new FcmNotificationManager(getApplicationContext());

                                        Intent transfer = new Intent(getApplicationContext(),
                                                MeetingActivity.class);
                                        transfer.putExtra("meeting", meeting);
                                        transfer.putExtra("user", user);

                                        mNotificationManager.showNotification(title, body, transfer);
                                    }

                                    @Override
                                    void onTaskFailed(RestClientException e) {
                                        Log.d(TAG, "onTaskFailed: " + e.getMessage());
                                        e.printStackTrace();
                                    }

                                    @Override
                                    void onExceptionRaised(Exception e) {
                                        Log.d(TAG, "onExceptionRaise: " + e.getMessage());
                                        e.printStackTrace();
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    void onTaskFailed(RestClientException e) {
                        Log.d(TAG, "onTaskFailed: " + e.getMessage());
                        e.printStackTrace();
                    }

                    @Override
                    void onExceptionRaised(Exception e) {
                        Log.d(TAG, "onExceptionRaise: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            }
        });
    }
}