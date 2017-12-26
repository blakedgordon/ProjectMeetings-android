package edu.calbaptist.android.projectmeetings.async.user;

import android.os.AsyncTask;

import edu.calbaptist.android.projectmeetings.App;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClientUserCallback;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClient;

/**
 *  Get Meeting Async
 *  Handles the retrieval of a meeting asynchronously.
 *
 *  @author Caleb Solorio
 *  @version 1.0.0 12/18/17
 */

public class GetUserAsync extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "GetUserAsync";
    public static final int GET_BY_U_ID = 0;
    public static final int GET_BY_EMAIL = 1;

    private int mMethod;
    private String mKey;
    private RestClientUserCallback mCallback;

    /**
     * The GetMeetingAsync constructor.
     * @param method Specifies what kind of key to retrieve the user by.
     * @param key The key that specifies the user to retrieve.
     * @param callback Executes after retrieval.
     */
    public GetUserAsync(int method, String key, RestClientUserCallback callback) {
        if(method == GET_BY_U_ID || method == GET_BY_EMAIL) {
            this.mMethod = method;
            this.mKey = key;
            this.mCallback = callback;
        } else {
            throw new IllegalArgumentException("Invalid mMethod");
        }
    }

    /**
     * Gets a meeting in the background.
     * @param voids Will not be called.
     * @return null
     */
    @Override
    protected Void doInBackground(Void... voids) {
        final String token = App.PREFERENCES.getString("firebase_token", null);

        switch (mMethod) {
            case GET_BY_U_ID:
                RestClient.getUserByUid(mKey, token, mCallback);
                break;
            case GET_BY_EMAIL:
                RestClient.getUserByEmail(mKey, token, mCallback);
                break;
        }

        return null;
    }
}
