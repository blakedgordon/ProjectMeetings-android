package edu.calbaptist.android.projectmeetings.async.user;

import android.os.AsyncTask;

import edu.calbaptist.android.projectmeetings.App;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClientUserCallback;
import edu.calbaptist.android.projectmeetings.models.User;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClient;

/**
 *  Update User Async
 *  Handles updating a User asynchronously.
 *
 *  @author Caleb Solorio
 *  @version 1.0.0 12/18/17
 */

public class UpdateUserAsync extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "UpdateUserAsync";

    private User mUser;
    private RestClientUserCallback mCallback;

    /**
     * The UpdateUserAsync constructor.
     * @param user The User to create.
     * @param callback Executes after updating the User.
     */
    public UpdateUserAsync(User user, RestClientUserCallback callback) {
        this.mUser = user;
        this.mCallback = callback;
    }

    /**
     * Updates a User in the background.
     * @param voids Will not be called.
     * @return null
     */
    @Override
    protected Void doInBackground(Void... voids) {
        RestClient.updateUser(mUser,
                App.PREFERENCES.getString("firebase_token", null), mCallback);
        return null;
    }
}
