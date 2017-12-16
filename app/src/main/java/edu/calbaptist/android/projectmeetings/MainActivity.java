package edu.calbaptist.android.projectmeetings;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import edu.calbaptist.android.projectmeetings.exceptions.RestClientException;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    GoogleAccountCredential mCredential;
    private TextView mOutputText;

    private LinearLayout buttonContainer;
    private SignInButton signInButton;
    private Button signOutButton;
    private LinearLayout connectingContainer;
    private TextView welcomeText;
    private TextView statusTextView;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;

    private static final String TAG = "SignInActivity";
    private static final int GOOGLE_SIGN_IN = 9001;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { DriveScopes.DRIVE_METADATA_READONLY };

    final static SharedPreferences prefs = App.context.getSharedPreferences(
            "edu.calbaptist.android.projectmeetings.Account_Name",
            Context.MODE_PRIVATE);

//    private Button meetingActivityButton;

    /**
     * Create the main activity.
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //auto sign in
        if(prefs.getBoolean("signed_in",false)) {
            Log.d(TAG, "onCreate: cHEcKING");
            Intent intent = getIntent();

            if (intent != null && intent.getExtras() != null) {
                Bundle extras = intent.getExtras();
                Log.d(TAG, "onCreate: " + extras.toString());

                String type = extras.getString("type");

                switch (type) {
                    case "meeting_invite":
                        Intent transfer = new Intent(this, MeetingListActivity.class);
                        startActivity(transfer);
                        break;
                    case "meeting_warn":
                        toMeetingActivity(extras.getString("m_id"));
                        break;
                    case "meeting_start":
                        toMeetingActivity(extras.getString("m_id"));
                        break;
                }
//                String someData= extras.getString("someData");
//                String someData2 = extras.getString("someData2");
            } else if(intent.getExtras() == null){
                Intent transfer = new Intent(this, MeetingListActivity.class);
                startActivity(transfer);
            }
        }

        Log.d(TAG, "onCreate: DONE CHECKING");

        setContentView(R.layout.activity_main);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();

//        statusTextView = (TextView) findViewById(R.id.status_text_view);
        buttonContainer = (LinearLayout) findViewById(R.id.layout_main_button_container);

        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(this);

        signOutButton = (Button) findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(this);

        if(prefs.getString("firebase_token", null) == null) {
            signOutButton.setEnabled(false);
        }

        connectingContainer = (LinearLayout) findViewById(R.id.layout_main_connecting_container);
        welcomeText = (TextView) findViewById(R.id.text_main_welcome);


//        meetingActivityButton = (Button) findViewById(R.id.meeting_activity_button);
//        meetingActivityButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, MeetingActivity.class);
//
//                Meeting meeting = new Meeting.MeetingBuilder()
//                        .setMid("357f2278-6aa3-48a0-b870-8cb938d51194")
//                        .setName("My First Meeting")
//                        .setObjective("Let's figure this app out!")
//                        .setTime(1512187834821L)
//                        .setTimeLimit(60000L)
//                        .setUid("dXwfd6uiyZR5xiiBo1xfPWYAF1C2")
//                        .setDriveFolderId("0B0SCJBL1Pu8eaWwxU1hnMFZrTVU")
//                        .build();
//
//                User user = new User.UserBuilder()
//                        .setUid("dXwfd6uiyZR5xiiBo1xfPWYAF1C2")
//                        .setEmail("jerryvonjingles@gmail.com")
//                        .setDisplayName("Caleb")
//                        .setFirebaseToken(prefs.getString("FirebaseToken",null))
//                        .build();
//
//                intent.putExtra("meeting", meeting);
//                intent.putExtra("user", user);
//
//                startActivity(intent);
//            }
//        });


//        mOutputText = (TextView) findViewById(R.id.output_text);
//        mOutputText.setVerticalScrollBarEnabled(true);
//        mOutputText.setMovementMethod(new ScrollingMovementMethod());

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        // Get instance id token for FCM.
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "FirebaseInstanceId token: " + token);

        // Configure FCM notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId  = getString(R.string.notification_channel_id);
            String channelName = getString(R.string.notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }

    }

    private static List<String> keysFromJsonString(String json) throws JSONException {
        List<String> keys = new ArrayList<>();
        Iterator<String> iterator = new JSONObject(json).keys();

        while(iterator.hasNext()) {
            keys.add(iterator.next());
        }

        return keys;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {
//            statusTextView.setText(currentUser.getDisplayName());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
        }

    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putBoolean("isSignedIn", true);
//        editor.apply();
//        if (prefs.getString(PREF_ACCOUNT_NAME, null) == null) {
//            startActivityForResult(
//                    mCredential.newChooseAccountIntent(),
//                    REQUEST_ACCOUNT_PICKER);
//        } else {
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
//        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case GOOGLE_SIGN_IN:
                // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...)
                GoogleSignInResult acct = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                try {
                    handleSignInResult(acct);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    showToast("This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");

//                    mOutputText.setText(
//                            "This app requires Google Play Services. Please install " +
//                                    "Google Play Services on your device and relaunch this app.");
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                    data.getExtras() != null) {
                String accountName =
                        data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

                    Log.d(TAG, "onActivityResult: ACCOUNT " + accountName);

                if (accountName != null) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(PREF_ACCOUNT_NAME, accountName);
                    editor.putBoolean("signed_in", true);
                    editor.apply();
                    mCredential.setSelectedAccountName(accountName);
                }
            }
                break;
        }
    }

    private void handleSignInResult(GoogleSignInResult result) throws IOException, GoogleAuthException {
        Log.d(TAG, "handleSignInResult: " + result.isSuccess());
        if(result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount user = result.getSignInAccount();
            firebaseAuthWithGoogle(user);

            assert user != null;
            prefs.edit().putString("google_token", user.getIdToken()).apply();
        } else {
//            statusTextView.setText("Sign in w/ Google failed :(");
            showToast("Sign in with Google failed :(");
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "Google Token:" + acct.getId() + " " + acct.getIdToken());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential: success");
                            final FirebaseUser user = mAuth.getCurrentUser();
//                            statusTextView.setText("Hello, " + user.getDisplayName());

                            FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                            assert mUser != null;
                            mUser.getToken(true)
                                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                                            if (task.isSuccessful()) {
                                                try {
                                                    createUser(acct.getDisplayName(), acct.getEmail(), acct.getIdToken());
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

//                            statusTextView.setText("Authentication with Firebase failed :(");
                            showToast("Authentication with Firebase failed :(");
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
//                statusTextView.setText("Signed out");
                prefs.edit().clear().commit();
                signOutButton.setEnabled(false);
            }
        });
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private void createUser(final String displayName, final String email, final String gToken) throws IOException, GoogleAuthException {
        buttonContainer.setVisibility(View.GONE);
        connectingContainer.setVisibility(View.VISIBLE);
        welcomeText.setText("Welcome, " + displayName + "!");


        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        assert mUser != null;
        mUser.getToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            final String idToken = task.getResult().getToken();
                            Log.d(TAG, "Firebase Token 427: " + idToken);

                            final User user = new User.UserBuilder()
                                    .setDisplayName(displayName)
                                    .setEmail(email)
                                    .setFirebaseToken(idToken)
                                    .setGoogleToken(gToken)
                                    .setInstanceId(FirebaseInstanceId.getInstance().getToken())
                                    .build();

                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    RestClient.createUser(user, new Callback.RestClientUser() {
                                        @Override
                                        void onTaskExecuted(User user) {
                                            Log.d(TAG, "onTaskExecuted: " + user.getDisplayName());
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString(PREF_ACCOUNT_NAME, user.getEmail());
                                            editor.putBoolean("signed_in", true);
                                            editor.putString("u_id",user.getUid());
                                            editor.putString("display_name",user.getDisplayName());
                                            editor.putString("email",user.getEmail());
                                            editor.putString("firebase_token", user.getFirebaseToken());
                                            editor.putString("google_token", user.getGoogleToken());
                                            editor.putString("instance_id", user.getInstanceId());
                                            editor.apply();

                                            showSignInButtons();

                                            startActivity(new Intent(getApplicationContext(), MeetingListActivity.class));
                                        }

                                        @Override
                                        void onTaskFailed(RestClientException e) {
                                            Log.d(TAG, "onTaskFailed with " + e.getResponseCode()
                                                    + ": " + e.getJson().toString());

                                            showToast("Uh oh, an unknown error occured :(");
                                            showSignInButtons();
                                        }

                                        @Override
                                        void onExceptionRaised(Exception e) {
                                            Log.d(TAG, "onExceptionRaised: " + e.getMessage());

                                            showToast("An unknown error occured :(");
                                            showSignInButtons();
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
    }

    private void toMeetingActivity(final String mId) {
        final String token = prefs.getString("firebase_token", null);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                RestClient.getMeeting(mId, token, new Callback.RestClientMeeting() {
                    @Override
                    void onTaskExecuted(final Meeting meeting) {
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                RestClient.updateUser(new User.UserBuilder().build(),
                                        token, new Callback.RestClientUser() {
                                            @Override
                                            void onTaskExecuted(User user) {
                                                FcmNotificationManager mNotificationManager =
                                                        new FcmNotificationManager(getApplicationContext());

                                                Intent transfer = new Intent(getApplicationContext(),
                                                        MeetingActivity.class);
                                                transfer.putExtra("meeting", meeting);
                                                transfer.putExtra("user", user);

                                                startActivity(transfer);
                                            }

                                            @Override
                                            void onTaskFailed(RestClientException e) {
                                                Log.d(TAG, "onTaskFailed: " + e.getMessage());
                                                e.printStackTrace();
                                            }

                                            @Override
                                            void onExceptionRaised(Exception e) {
                                                Log.d(TAG, "onExceptionRaise: " + e.getMessage());
                                                e.printStackTrace();
                                            }
                                        });
                            }
                        });
                    }

                    @Override
                    void onTaskFailed(RestClientException e) {
                        Log.d(TAG, "onTaskFailed: " + e.getMessage());
                        e.printStackTrace();
                    }

                    @Override
                    void onExceptionRaised(Exception e) {
                        Log.d(TAG, "onExceptionRaise: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSignInButtons() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buttonContainer.setVisibility(View.VISIBLE);
                connectingContainer.setVisibility(View.GONE);

                if(prefs.getString("firebase_token", null) != null) {
                    signOutButton.setEnabled(true);
                }
            }
        });
    }
}