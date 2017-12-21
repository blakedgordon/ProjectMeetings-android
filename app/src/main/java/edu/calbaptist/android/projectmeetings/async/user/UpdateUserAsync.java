package edu.calbaptist.android.projectmeetings.async.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import edu.calbaptist.android.projectmeetings.App;
import edu.calbaptist.android.projectmeetings.R;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClientUserCallback;
import edu.calbaptist.android.projectmeetings.models.User;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClient;

/**
 *  Create User Async
 *  Handles updating a user asynchronously.
 *
 *  @author Caleb Solorio
 *  @version 1.0.0 12/18/17
 */

public class UpdateUserAsync extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "UpdateUserAsync";

    private static final SharedPreferences PREFERENCES = App.context.getSharedPreferences(
            App.context.getString(R.string.app_package), Context.MODE_PRIVATE);

    private User user;
    private RestClientUserCallback callback;

    /**
     * The UpdateUserAsync constructor.
     * @param user The user to create.
     * @param callback Executes after creating the meeting.
     */
    public UpdateUserAsync(User user, RestClientUserCallback callback) {
        this.user = user;
        this.callback = callback;
    }

    /**
     * Updates a user in the background.
     * @param voids Will not be called.
     * @return null
     */
    @Override
    protected Void doInBackground(Void... voids) {
        RestClient.updateUser(user, PREFERENCES.getString("firebase_token", null), callback);
        return null;
    }
}
