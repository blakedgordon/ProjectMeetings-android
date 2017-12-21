package edu.calbaptist.android.projectmeetings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
        implements View.OnClickListener {
    private static final String TAG = "MeetingListActivity";
    private static final SharedPreferences PREFERENCES = App.context.getSharedPreferences(
            App.context.getString(R.string.app_package), Context.MODE_PRIVATE);

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
     * Executes on the back button being pressed. Takes the user back to the SignInActivity.
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("type", "SignInActivity");
        startActivity(intent);
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

