package edu.calbaptist.android.projectmeetings.async.meeting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.util.ArrayList;

import edu.calbaptist.android.projectmeetings.App;
import edu.calbaptist.android.projectmeetings.R;
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
    private static final SharedPreferences PREFERENCES = App.context.getSharedPreferences(
            App.context.getString(R.string.app_package), Context.MODE_PRIVATE);

    private String mId;
    private ArrayList<String> emails;
    private String token;
    private RestClientJsonCallback callback;

    /**
     * The UninviteToMeetingAsync constructor.
     * @param mId The id of the meeting.
     * @param emails The emails of the users to invite.
     * @param callback Executes after inviting users to a meeting.
     */
    public UninviteFromMeetingAsync(String mId, ArrayList<String> emails, RestClientJsonCallback callback) {
        this.mId = mId;
        this.emails = emails;
        this.callback = callback;
    }

    /**
     * Invites users to a meeting in the background
     * @param voids Will not be called.
     * @return null
     */
    @Override
    protected Void doInBackground(Void... voids) {
        RestClient.uninviteFromMeeting(mId, emails,
                PREFERENCES.getString("firebase_token", null), callback);
        return null;
    }
}
