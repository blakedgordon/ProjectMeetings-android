package edu.calbaptist.android.projectmeetings.async.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import edu.calbaptist.android.projectmeetings.App;
import edu.calbaptist.android.projectmeetings.R;
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
    private static final SharedPreferences PREFERENCES = App.context.getSharedPreferences(
            App.context.getString(R.string.app_package), Context.MODE_PRIVATE);

    private String mId;
    private RestClientJsonCallback callback;

    /**
     * The DeleteUserAsync constructor.
     * @param uId The id of the user to delete.
     * @param callback Executes after deleting the user.
     */
    public DeleteUserInviteAsync(String uId, RestClientJsonCallback callback) {
        this.mId = uId;
        this.callback = callback;
    }

    /**
     * Deletes a user in the background.
     * @param voids Will not be called.
     * @return null
     */
    @Override
    protected Void doInBackground(Void... voids) {
        RestClient.deleteUserInvite(mId,
                PREFERENCES.getString("firebase_token", null), callback);
        return null;
    }
}

