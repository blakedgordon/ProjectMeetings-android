package edu.calbaptist.android.projectmeetings.async.meeting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import edu.calbaptist.android.projectmeetings.App;
import edu.calbaptist.android.projectmeetings.R;
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
    private static final String TAG = "DeleteMeetingAsync";
    private static final SharedPreferences PREFERENCES = App.context.getSharedPreferences(
            App.context.getString(R.string.app_package), Context.MODE_PRIVATE);

    private String mId;
    private RestClientJsonCallback callback;

    /**
     * The DeleteMeetingAsync constructor.
     * @param mId The id of the meeting to delete.
     * @param callback Executes after deleting the meeting.
     */
    public DeleteMeetingAsync(String mId, RestClientJsonCallback callback) {
        this.mId = mId;
        this.callback = callback;
    }

    /**
     * Deletes a meeting in the background.
     * @param voids Will not be called.
     * @return null
     */
    @Override
    protected Void doInBackground(Void... voids) {
        RestClient.deleteMeeting(mId,
                PREFERENCES.getString("firebase_token", null), callback);
        return null;
    }
}
