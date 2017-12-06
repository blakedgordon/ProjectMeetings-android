package edu.calbaptist.android.projectmeetings;

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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import edu.calbaptist.android.projectmeetings.Exceptions.RestClientException;

import static edu.calbaptist.android.projectmeetings.MainActivity.REQUEST_AUTHORIZATION;
import static edu.calbaptist.android.projectmeetings.MainActivity.prefs;

/**
 * Created by Austin on 12/1/2017.
 */

public class MeetingCreationActivity extends AppCompatActivity{

    EditText MeetingName, MeetingObjective, length, invites;
    DatePicker date;
    TimePicker time;
    Button submit;
    private static final String TAG = "MeetingCreationActivity";

    private static final String[] SCOPES = { DriveScopes.DRIVE_METADATA, DriveScopes.DRIVE_FILE };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_creation);

        MeetingName = findViewById(R.id.MeetingName);
        MeetingObjective = findViewById(R.id.MeetingObjective);
        date = findViewById(R.id.Date);
        time = findViewById(R.id.Time);
        submit = findViewById(R.id.Submit);
        length = findViewById(R.id.Length);
        invites = findViewById(R.id.Invites);

        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Calendar calendar = Calendar.getInstance();
                calendar.set(date.getYear(), date.getMonth(), date.getDayOfMonth(), time.getHour(), time.getMinute());
                final long millis = calendar.getTimeInMillis();
                final long mLength = Long.parseLong(length.getText().toString())*60*1000;
                createMeeting(millis, mLength);
            }
        });
    }

    private void createMeeting(final long millis, final long mLength){
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            final String idToken = task.getResult().getToken();

                            SharedPreferences prefs = App.context.getSharedPreferences(
                                    "edu.calbaptist.android.projectmeetings.Account_Name",
                                    Context.MODE_PRIVATE);

                            ArrayList items = new ArrayList<String>(Arrays.asList(invites.getText().toString().split("\\s*,\\s*")));

                            final Meeting meeting = new Meeting.MeetingBuilder()
                                    .setName(MeetingName.getText().toString())
                                    .setObjective(MeetingObjective.getText().toString())
                                    .setTime(millis)
                                    .setTimeLimit(mLength)
                                    .setDriveFolderId(prefs.getString("DefaultFolder", ""))
                                    .setInvites(items)
                                    .build();

                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    RestClient.createMeeting(meeting, idToken, new Callback.RestClientMeeting() {
                                        @Override
                                        void onTaskExecuted(Meeting m) {
                                            Log.d(TAG, "onTaskExecuted: " + m.getName());
                                            new MeetingCreationActivity.requestCreateFolder(m.getName()).execute();
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

                            Log.d(TAG, "Firebase Token: " + idToken);
                            // Send token to your backend via HTTPS
                            // ...
                        }
                    }
                });
    }
    private void switchActivity(Class activity){
        startActivity(new Intent(this, activity));
    }

    private class requestCreateFolder extends  AsyncTask<Void, Void, Void> {

        String meetingName;
        Drive driveService;

        requestCreateFolder(String meetingName){
            this.meetingName = meetingName;

            try {
                driveService = DriveFiles.getInstance().getDriveService();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            String folderId = prefs.getString("DefaultFolder",null);
            System.out.println(folderId + "-------------------------------------------------------------");
            File fileMetadata = new File();
            fileMetadata.setName(meetingName);
            fileMetadata.setPermissionIds(Collections.singletonList(prefs.getString("email",null)));
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            fileMetadata.setParents(Collections.singletonList(folderId));
            try {
                File file = driveService.files().create(fileMetadata)
                        .setFields("id")
                        .execute();
                System.out.println("Folder ID: " + file.getId());
                finish();
                switchActivity(MeetingListActivity.class);
            } catch (UserRecoverableAuthIOException e){
                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            } catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }
    }
}


