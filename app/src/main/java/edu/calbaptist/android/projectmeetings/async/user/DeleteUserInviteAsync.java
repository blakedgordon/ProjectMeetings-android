package edu.calbaptist.android.projectmeetings.async.user;

import android.os.AsyncTask;

import edu.calbaptist.android.projectmeetings.App;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClientJsonCallback;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClient;

/**
 *  Delete User Async
 *  Handles the deletion of a user asynchronously.
 *
 *  @author Caleb Solorio
 *  @version 1.0.0 12/18/17
 */

public class DeleteUserInviteAsync extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "DeleteUserInviteAsync";

    private String mId;
    private RestClientJsonCallback mCallback;

    /**
     * The DeleteUserAsync constructor.
     * @param uId The id of the user to delete.
     * @param callback Executes after deleting the user.
     */
    public DeleteUserInviteAsync(String uId, RestClientJsonCallback callback) {
        this.mId = uId;
        this.mCallback = callback;
    }

    /**
     * Deletes a user in the background.
     * @param voids Will not be called.
     * @return null
     */
    @Override
    protected Void doInBackground(Void... voids) {
        RestClient.deleteUserInvite(mId,
                App.PREFERENCES.getString("firebase_token", null), mCallback);
        return null;
    }
}

