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
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdInternalReceiver;

import edu.calbaptist.android.projectmeetings.Exceptions.RestClientException;

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
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            String idToken = task.getResult().getToken();
                            SharedPreferences settings = App.context.getSharedPreferences(
                                    "edu.calbaptist.android.projectmeetings.Account_Name",
                                    Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("FirebaseToken", idToken);
                            editor.apply();
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    String uID = prefs.getString("uID",null);
                                    final String firebaseToken = prefs.getString("FirebaseToken",null);
                                    RestClient.getUserByUid(uID, firebaseToken, new Callback.RestClientUser() {
                                        @Override
                                        void onTaskExecuted(final User user) {
                                            Log.d(TAG, "onTaskExecuted: " + user.getDisplayName());

                                            final User updatedUser = new User.UserBuilder()
                                                    .setFirebaseToken(firebaseToken)
                                                    .setGoogleToken(prefs.getString("gToken",null))
                                                    .setInstanceId(FirebaseInstanceId.getInstance().getToken())
                                                    .build();

                                            AsyncTask.execute(new Runnable() {
                                                @Override
                                                public void run() {
                                                    String firebaseToken = prefs.getString("FirebaseToken",null);
                                                    RestClient.updateUser(updatedUser, firebaseToken, new Callback.RestClientUser() {
                                                        @Override
                                                        void onTaskExecuted(User user) {
                                                            Log.d(TAG, "onTaskExecuted: " + user.getDisplayName());
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
}

