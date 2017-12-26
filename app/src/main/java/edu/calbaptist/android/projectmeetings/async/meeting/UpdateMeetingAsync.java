package edu.calbaptist.android.projectmeetings.async.meeting;

import android.os.AsyncTask;

import edu.calbaptist.android.projectmeetings.App;
import edu.calbaptist.android.projectmeetings.models.Meeting;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClient;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClientMeetingCallback;

/**
 *  Create Meeting Async
 *  Handles updating a Meeting asynchronously.
 *
 *  @author Caleb Solorio
 *  @version 1.0.0 12/18/17
 */

public class UpdateMeetingAsync extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "UpdateMeetingAsync";

    private Meeting mMeeting;
    private RestClientMeetingCallback mCallback;

    /**
     * The UpdateMeetingAsync constructor.
     * @param mMeeting The Meeting to create.
     * @param callback Executes after creating the Meeting.
     */
    public UpdateMeetingAsync(Meeting mMeeting, RestClientMeetingCallback callback) {
        this.mMeeting = mMeeting;
        this.mCallback = callback;
    }

    /**
     * Updates a Meeting in the background.
     * @param voids Will not be called.
     * @return null
     */
    @Override
    protected Void doInBackground(Void... voids) {
        RestClient.updateMeeting(mMeeting,
                App.PREFERENCES.getString("firebase_token", null), mCallback);
        return null;
    }
}