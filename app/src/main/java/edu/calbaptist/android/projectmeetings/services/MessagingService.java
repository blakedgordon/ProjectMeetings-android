package edu.calbaptist.android.projectmeetings.services;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import edu.calbaptist.android.projectmeetings.async.meeting.GetMeetingAsync;
import edu.calbaptist.android.projectmeetings.async.user.UpdateUserAsync;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClientMeetingCallback;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClientUserCallback;
import edu.calbaptist.android.projectmeetings.models.Meeting;
import edu.calbaptist.android.projectmeetings.MeetingActivity;
import edu.calbaptist.android.projectmeetings.MeetingListActivity;
import edu.calbaptist.android.projectmeetings.models.User;
import edu.calbaptist.android.projectmeetings.exceptions.RestClientException;
import edu.calbaptist.android.projectmeetings.utils.FcmNotificationManager;

/**
 *  Messeging Service
 *  Handles FCM remote messages.
 *
 *  @author Caleb Solorio
 *  @version 0.4.0 11/15/17
 */
public class MessagingService extends FirebaseMessagingService {
    public static final String TAG = "MessagingService";
    public static final String MEETING_INVITE = "meeting_invite";
    public static final String MEETING_WARN = "meeting_warn";
    public static final String MEETING_START = "meeting_start";
    public static final String TYPE = "type";
    public static final String TITLE = "title";
    public static final String BODY = "body";
    public static final String M_ID = "m_id";

    private Meeting mMeeting;
    private String mTitle;
    private String mBody;

    /**
     * Called on recieving a new message, handle the data and notify the user.
     * @param intent Contains the data payload.
     */
    @Override
    public void handleIntent(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            Bundle extras = intent.getExtras();
            String type = extras.getString(TYPE);

            switch (type) {
                case MEETING_INVITE:
                    notifyUserToMeetingListActvity(extras.getString(TITLE),
                            extras.getString(BODY));
                    break;
                case MEETING_WARN:
                    notifyUserToMeetingActivity(extras.getString(TITLE),
                            extras.getString(BODY),
                            extras.getString(M_ID));
                    break;
                case MEETING_START:
                    notifyUserToMeetingActivity(extras.getString(TITLE),
                            extras.getString(BODY),
                            extras.getString(M_ID));
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
        this.mTitle = title;
        this.mBody = body;

        new GetMeetingAsync(mId, new AsyncCallback()).execute();
    }

    /**
     * Specifies the RestClientMeetingCallback and RestClientUserCallback
     * implementation after getting a meeting.
     */
    private class AsyncCallback implements RestClientMeetingCallback, RestClientUserCallback {
        @Override
        public void onTaskExecuted(Meeting m) {
            mMeeting = m;
            new UpdateUserAsync(new User.UserBuilder().build(), this);
        }

        @Override
        public void onTaskExecuted(User user) {
            FcmNotificationManager mNotificationManager =
                    new FcmNotificationManager(getApplicationContext());

            Intent transfer = new Intent(getApplicationContext(),
                    MeetingActivity.class);
            transfer.putExtra(MeetingActivity.MEETING_KEY, mMeeting);
            transfer.putExtra(MeetingActivity.USER_KEY, user);

            mNotificationManager.showNotification(mTitle, mBody, transfer);
        }

        @Override
        public void onTaskFailed(RestClientException e) {
            Log.d(TAG, "onTaskFailed: " + e.getMessage());
            e.printStackTrace();
        }

        @Override
        public void onExceptionRaised(Exception e) {
            Log.d(TAG, "onExceptionRaise: " + e.getMessage());
            e.printStackTrace();
        }
    }
}