package edu.calbaptist.android.projectmeetings.async.meeting;

import android.os.AsyncTask;

import edu.calbaptist.android.projectmeetings.App;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClient;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClientJsonCallback;

/**
 *  Delete Meeting Async
 *  Handles the deletion of a meeting asynchronously.
 *
 *  @author Caleb Solorio
 *  @version 1.0.0 12/18/17
 */

public class DeleteMeetingAsync extends AsyncTask<Void, Void, Void> {
    public static final String TAG = "DeleteMeetingAsync";

    private String mId;
    private RestClientJsonCallback mCallback;

    /**
     * The DeleteMeetingAsync constructor.
     * @param mId The id of the meeting to delete.
     * @param callback Executes after deleting the meeting.
     */
    public DeleteMeetingAsync(String mId, RestClientJsonCallback callback) {
        this.mId = mId;
        this.mCallback = callback;
    }

    /**
     * Deletes a meeting in the background.
     * @param voids Will not be called.
     * @return null
     */
    @Override
    protected Void doInBackground(Void... voids) {
        RestClient.deleteMeeting(mId,
                App.PREFERENCES.getString("firebase_token", null), mCallback);
        return null;
    }
}
