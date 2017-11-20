package edu.calbaptist.android.projectmeetings;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 *  Instance Id Service
 *  Manages instance id changes
 *
 *  @author Caleb Solorio
 *  @version 0.4.0 11/15/17
 */

public class InstanceIdService extends FirebaseInstanceIdService {
    static final String TAG = "InstanceIdService";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
//        sendRegistrationToServer(refreshedToken);
    }
}
