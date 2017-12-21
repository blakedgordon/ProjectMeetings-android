package edu.calbaptist.android.projectmeetings.async.meeting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import edu.calbaptist.android.projectmeetings.App;
import edu.calbaptist.android.projectmeetings.R;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClientMeetingCallback;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClient;

/**
 *  Get Meeting Async
 *  Handles the retrieval of a meeting asynchronously.
 *
 *  @author Caleb Solorio
 *  @version 1.0.0 12/18/17
 */

public class GetMeetingAsync extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "DeleteUserInviteAsync";
    private static final SharedPreferences PREFERENCES = App.context.getSharedPreferences(
            App.context.getString(R.string.app_package), Context.MODE_PRIVATE);

    private String mId;
    private RestClientMeetingCallback callback;

    /**
     * The GetMeetingAsync constructor.
     * @param mId The id of the meeting to get.
     * @param callback Executes after retrieval.
     */
    public GetMeetingAsync(String mId, RestClientMeetingCallback callback) {
        this.mId = mId;
        this.callback = callback;
    }

    /**
     * Gets a meeting in the background.
     * @param voids Will not be called.
     * @return null
     */
    @Override
    protected Void doInBackground(Void... voids) {
        RestClient.getMeeting(mId, PREFERENCES.getString("firebase_token", null), callback);
        return null;
    }
}
