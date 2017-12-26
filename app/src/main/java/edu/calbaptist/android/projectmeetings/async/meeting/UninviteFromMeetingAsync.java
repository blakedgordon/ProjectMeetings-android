package edu.calbaptist.android.projectmeetings.async.meeting;

import android.os.AsyncTask;

import java.util.ArrayList;

import edu.calbaptist.android.projectmeetings.App;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClient;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClientJsonCallback;

/**
 *  Uninvite From Meeting Async
 *  Handles uninviting users to a meeting asynchronously.
 *
 *  @author Caleb Solorio
 *  @version 1.0.0 12/18/17
 */

public class UninviteFromMeetingAsync extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "DeleteMeetingAsync";

    private String mId;
    private ArrayList<String> mEmails;
    private RestClientJsonCallback mCallback;

    /**
     * The UninviteToMeetingAsync constructor.
     * @param mId The id of the meeting.
     * @param emails The emails of the users to invite.
     * @param callback Executes after inviting users to a meeting.
     */
    public UninviteFromMeetingAsync(String mId, ArrayList<String> emails, RestClientJsonCallback callback) {
        this.mId = mId;
        this.mEmails = emails;
        this.mCallback = callback;
    }

    /**
     * Invites users to a meeting in the background
     * @param voids Will not be called.
     * @return null
     */
    @Override
    protected Void doInBackground(Void... voids) {
        RestClient.uninviteFromMeeting(mId, mEmails,
                App.PREFERENCES.getString("firebase_token", null), mCallback);
        return null;
    }
}
