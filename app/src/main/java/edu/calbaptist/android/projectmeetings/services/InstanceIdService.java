package edu.calbaptist.android.projectmeetings.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import edu.calbaptist.android.projectmeetings.App;
import edu.calbaptist.android.projectmeetings.R;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClientUserCallback;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClient;
import edu.calbaptist.android.projectmeetings.models.User;
import edu.calbaptist.android.projectmeetings.exceptions.RestClientException;

/**
 *  Instance Id Service
 *  Manages instance id changes
 *
 *  @author Caleb Solorio
 *  @version 0.4.0 11/15/17
 */

public class InstanceIdService extends FirebaseInstanceIdService {
    private static final String TAG = "InstanceIdService";
    private final static SharedPreferences PREFERENCES = App.context.getSharedPreferences(
            App.context.getString(R.string.app_package), Context.MODE_PRIVATE);

    /**
     * Called when the user's instance id updates. Sends the new token server-side.
     */
    @Override
    public void onTokenRefresh() {
        String refreshedId = FirebaseInstanceId.getInstance().getToken();
        sendRegistrationToServer(refreshedId);
    }

    // Send the new instance id to the backend.
    private void sendRegistrationToServer(String refreshedId) {
        SharedPreferences.Editor editor = PREFERENCES.edit();
        editor.putString("instance_id", refreshedId);
        editor.apply();

        User user = new User.UserBuilder()
                .setInstanceId(refreshedId)
                .build();

        RestClient.updateUser(user, PREFERENCES.getString("firebase_token", null), new AsyncCallback());
    }

    /**
     * Specifies the RestClientUserCallback implementation after updating the instance id.
     */
    private class AsyncCallback implements RestClientUserCallback {
        @Override
        public void onTaskExecuted(User user) {
            Log.d(TAG, "onTaskExecuted: Updated instance id.");
        }

        @Override
        public void onTaskFailed(RestClientException e) {
            Log.e(TAG, "onTaskFailed: ", e);
        }

        @Override
        public void onExceptionRaised(Exception e) {
            Log.e(TAG, "onExceptionRaised: ", e);
        }
    }
}
