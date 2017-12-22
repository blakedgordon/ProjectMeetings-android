package edu.calbaptist.android.projectmeetings.async.user;

import android.os.AsyncTask;

import edu.calbaptist.android.projectmeetings.utils.rest.RestClientUserCallback;
import edu.calbaptist.android.projectmeetings.models.User;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClient;

/**
 *  Create User Async
 *  Handles the creation of a user asynchronously.
 *
 *  @author Caleb Solorio
 *  @version 1.0.0 12/18/17
 */

public class CreateUserAsync extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "CreateUserAsync";

    private User user;
    private RestClientUserCallback callback;

    /**
     * The CreateUserAsync constructor.
     * @param user The user to create.
     * @param callback Executes after creating the user.
     */
    public CreateUserAsync(User user, RestClientUserCallback callback) {
        this.user = user;
        this.callback = callback;
    }

    /**
     * Creates a user in the background.
     * @param voids Will not be called.
     * @return null
     */
    @Override
    protected Void doInBackground(Void... voids) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RestClient.createUser(user, callback);
        return null;
    }
}
