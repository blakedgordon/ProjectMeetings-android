package edu.calbaptist.android.projectmeetings.async.meeting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import edu.calbaptist.android.projectmeetings.App;
import edu.calbaptist.android.projectmeetings.R;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClientMeetingCallback;
import edu.calbaptist.android.projectmeetings.models.Meeting;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClient;

/**
 *  Create Meeting Async
 *  Handles the creation of a meeting asynchronously.
 *
 *  @author Caleb Solorio
 *  @version 1.0.0 12/18/17
 */

public class CreateMeetingAsync extends AsyncTask<Void, Void, Void> {
    public static final String TAG = "CreateMeetingAsync";

    private Meeting mMeeting;
    private RestClientMeetingCallback mCallback;

    /**
     * The CreateMeetingAsync constructor.
     * @param meeting The meeting to create.
     * @param callback Executes after creating the meeting.
     */
    public CreateMeetingAsync(Meeting meeting, RestClientMeetingCallback callback) {
        this.mMeeting = meeting;
        this.mCallback = callback;
    }

    /**
     * Creates a meeting in the background.
     * @param voids Will not be called.
     * @return null
     */
    @Override
    protected Void doInBackground(Void... voids) {
        RestClient.createMeeting(mMeeting,
                App.PREFERENCES.getString("firebase_token", null), mCallback);
        return null;
    }
}
