package edu.calbaptist.android.projectmeetings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import edu.calbaptist.android.projectmeetings.async.user.UpdateUserAsync;
import edu.calbaptist.android.projectmeetings.exceptions.RestClientException;
import edu.calbaptist.android.projectmeetings.models.User;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClientUserCallback;

/**
 *  Meeting List Activity
 *  Shows the meetings the user is a part of.
 *
 *  @author Austin Brinegar, Caleb Solorio
 *  @version 1.0.0 12/20/17
 */

public class MeetingListActivity extends AppCompatActivity
        implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MeetingListActivity";
    private static final SharedPreferences PREFERENCES = App.context.getSharedPreferences(
            App.context.getString(R.string.app_package), Context.MODE_PRIVATE);

    private GoogleApiClient mGoogleApiClient;

    private MenuItem mSettingsButton;
    private FloatingActionButton newMeeting;

    /**
     * Initializes MeetingListActivity
     * @param savedInstanceState Contains any data sent from the previous activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        stashToken();
        setContentView(R.layout.activity_meeting_list);
        newMeeting = findViewById(R.id.fab_create_meeting);
        newMeeting.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    /**
     * Handles OnClick events.
     * @param view the view in which the event occurred.
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_create_meeting:
                switchActivity(CreateMeetingActivity.class);
                break;
        }
    }

    /**
     * Initializes the activity's menu.
     * @param menu the menu to initialize.
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_meeting_list, menu);
        mSettingsButton = menu.findItem(R.id.item_settings);
        mSettingsButton.setVisible(true);
        return true;
    }

    /**
     * Called when a menu item is selected.
     * @param item The menu item in question.
     * @return true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_privacy_policy:
                switchActivity(PrivacyPolicyActivity.class);
                return true;
            case R.id.item_sign_out:
                signOut(mGoogleApiClient);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Executes on failure to connect with Google.
     * @param connectionResult Contains data on the connection attempt.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        showToast("Failed to Sign Out :(");
    }

    /**
     * Signs the user out of the app.
     */
    private void signOut(GoogleApiClient client) {
        Auth.GoogleSignInApi.signOut(client).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                PREFERENCES.edit().clear().commit();

                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent);
            }
        });
    }

    /**
     * Shows the given string in a short Toast.
     */
    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Switches to another activity.
     */
    private void switchActivity(Class activity){
        startActivity(new Intent(this, activity));
    }

    /**
     * Updates the user's Firebase token and sends it to the back end.
     */
    private void stashToken(){
        final FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            String idToken = task.getResult().getToken();

                            SharedPreferences.Editor editor = PREFERENCES.edit();
                            editor.putString("firebase_token", idToken);
                            editor.apply();

                            final User newUser = new User.UserBuilder()
                                    .setFirebaseToken(idToken)
                                    .build();

                            new UpdateUserAsync(newUser, new AsyncUserCallback()).execute();
                        }
                    }
                });

    }

    /**
     * Specifies the RestClientUserCallback implementation after updating a user's token.
     */
    private class AsyncUserCallback implements RestClientUserCallback {
        @Override
        public void onTaskExecuted(final User user) {
            SharedPreferences.Editor editor = PREFERENCES.edit();
            editor.putString("firebase_token", user.getFirebaseToken());
            editor.apply();

            MeetingListFragment meetingListFragment =
                    (MeetingListFragment) getFragmentManager()
                            .findFragmentById(R.id.fragment_meeting_list);
            meetingListFragment.updateList(user);
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

