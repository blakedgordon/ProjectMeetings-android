package edu.calbaptist.android.projectmeetings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.iid.FirebaseInstanceId;

import edu.calbaptist.android.projectmeetings.exceptions.RestClientException;

/**
 * Created by Austin on 11/30/2017.
 */

public class MeetingListActivity extends AppCompatActivity{

    private static final String TAG = "MeetingListActivity";
    private FloatingActionButton newMeeting;
    final static SharedPreferences prefs = App.context.getSharedPreferences(
            "edu.calbaptist.android.projectmeetings.Account_Name",
            Context.MODE_PRIVATE);

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        stashToken();
        setContentView(R.layout.activity_meetinglist);
        newMeeting = findViewById(R.id.CreateMeeting);
        newMeeting.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                switchActivity(MeetingCreationActivity.class);
            }
        });
    }

    private void switchActivity(Class activity){
        startActivity(new Intent(this, activity));
    }

    private void stashToken(){
        final FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            String idToken = task.getResult().getToken();

                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("firebase_token", idToken);
                            editor.apply();

                            final User newUser = new User.UserBuilder()
                                    .setFirebaseToken(idToken)
                                    .build();

                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    String uID = prefs.getString("u_id",null);
                                    Log.d(TAG, "run: " + uID);
                                    final String firebaseToken = prefs.getString("firebase_token",null);
                                    RestClient.updateUser(newUser, firebaseToken, new Callback.RestClientUser() {
                                        @Override
                                        void onTaskExecuted(final User user) {
                                            Log.d(TAG, "onTaskExecuted: adfa " + user.getMeetings());

                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString("firebase_token", user.getFirebaseToken());
                                            editor.apply();

                                            CurrentMeetingsFragment meetingListFragment =
                                                    (CurrentMeetingsFragment) getFragmentManager()
                                                            .findFragmentById(R.id.fragment_meeting_list);
                                            meetingListFragment.updateList(user);

//                                            final User updatedUser = new User.UserBuilder()
//                                                    .setFirebaseToken(firebaseToken)
//                                                    .setGoogleToken(prefs.getString("google_token",null))
//                                                    .setInstanceId(FirebaseInstanceId.getInstance().getToken())
//                                                    .build();
//
//                                            AsyncTask.execute(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    String firebaseToken = prefs.getString("firebase_token",null);
//                                                    RestClient.updateUser(updatedUser, firebaseToken, new Callback.RestClientUser() {
//                                                        @Override
//                                                        void onTaskExecuted(User user) {
//                                                            Log.d(TAG, "onTaskExecuted: " + user.getDisplayName());
//                                                        }
//
//                                                        @Override
//                                                        void onTaskFailed(RestClientException e) {
//                                                            Log.d(TAG, "onTaskFailed with " + e.getResponseCode()
//                                                                    + ": " + e.getJson().toString());
//                                                        }
//
//                                                        @Override
//                                                        void onExceptionRaised(Exception e) {
//                                                            Log.d(TAG, "onExceptionRaised: " + e.getMessage());
//                                                        }
//                                                    });
//                                                }
//                                            });
                                        }

                                        @Override
                                        void onTaskFailed(RestClientException e) {
                                            Log.d(TAG, "onTaskFailed with " + e.getResponseCode()
                                                    + ": " + e.getJson().toString());
                                        }

                                        @Override
                                        void onExceptionRaised(Exception e) {
                                            Log.d(TAG, "onExceptionRaised: " + e.getMessage());
                                        }
                                    });
                                }
                            });
                        }
                    }
                });

    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: ");
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("type", "SignInActivity");
        startActivity(intent);
    }
}

