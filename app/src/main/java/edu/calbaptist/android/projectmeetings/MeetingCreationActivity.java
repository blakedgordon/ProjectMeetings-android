package edu.calbaptist.android.projectmeetings;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

import edu.calbaptist.android.projectmeetings.Exceptions.RestClientException;

import static edu.calbaptist.android.projectmeetings.MainActivity.REQUEST_AUTHORIZATION;
import static edu.calbaptist.android.projectmeetings.MainActivity.prefs;

/**
 * Created by Austin on 12/1/2017.
 */

public class MeetingCreationActivity extends AppCompatActivity{

    Calendar c;

    EditText meetingName, meetingObjective, add_invites;
    Button submit;

    Button dateButton;
    Button timeButton;
    Button driveButton;

    String mDriveFolderId;

    private int mLengthMinutes;

    private static final String TAG = "MeetingCreationActivity";

    private static final String[] SCOPES = { DriveScopes.DRIVE_METADATA, DriveScopes.DRIVE_FILE };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_creation);

        getSupportActionBar().setTitle("Create Meeting");

        meetingName = findViewById(R.id.MeetingName);
        meetingObjective = findViewById(R.id.MeetingObjective);

        c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);
        final int mHour = c.get(Calendar.HOUR_OF_DAY);
        final int mMinute = c.get(Calendar.MINUTE);

        final SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, YYYY");
        final SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mm a");

        dateButton = findViewById(R.id.button_date_picker);
        dateButton.setText(dateFormatter.format(c.getTime()));
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(MeetingCreationActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                c.set(year, monthOfYear, dayOfMonth);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dateButton.setText(dateFormatter.format(c.getTime()));
                                    }
                                });

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        timeButton = findViewById(R.id.button_time_picker);
        timeButton.setText(timeFormatter.format(c.getTime()));
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(MeetingCreationActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hour,
                                                  int minute) {
                                c.set(Calendar.HOUR_OF_DAY, hour);
                                c.set(Calendar.MINUTE, minute);

                                timeButton.setText(timeFormatter.format(c.getTime()));
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();
            }
        });

        driveButton = findViewById(R.id.button_drive_folder);
        driveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FolderViewActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        mLengthMinutes = 1;

        NumberPicker np = (NumberPicker) findViewById(R.id.np);
        np.setMinValue(1);
        np.setMaxValue(60);

        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                //Display the newly selected number from picker
                mLengthMinutes = newVal;
            }
        });

        add_invites = findViewById(R.id.add_invites);

        submit = findViewById(R.id.Submit);
        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                createMeeting();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                Log.d(TAG, "onActivityResult: " + data.getStringExtra("folder_name"));
                driveButton.setText(data.getStringExtra("folder_name"));
                mDriveFolderId = data.getStringExtra("folder_id");
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private void createMeeting(){
        final long millis = c.getTimeInMillis();
        final long length = (long) mLengthMinutes * 60 * 1000;

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser.getToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            final String idToken = task.getResult().getToken();

                            SharedPreferences prefs = App.context.getSharedPreferences(
                                    "edu.calbaptist.android.projectmeetings.Account_Name",
                                    Context.MODE_PRIVATE);

                            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.edit_meeting_spinner);
                            progressBar.setVisibility(View.VISIBLE);

                            final ArrayList invitationsToAdd =
                                    new ArrayList<String>(Arrays.asList(add_invites.getText().toString().split("\\s*,\\s*")));

                            final Meeting meeting = new Meeting.MeetingBuilder()
                                    .setName(meetingName.getText().toString())
                                    .setObjective(meetingObjective.getText().toString())
                                    .setTime(millis)
                                    .setTimeLimit(length)
                                    .setDriveFolderId(mDriveFolderId)
                                    .setInvites(invitationsToAdd)
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
                                            progressBar.setVisibility(View.GONE);
                                            showToast("Oh no, an unknown error occured :(");
                                        }

                                        @Override
                                        void onExceptionRaised(Exception e) {
                                            Log.d(TAG, "onExceptionRaised: " + e.getMessage());
                                            progressBar.setVisibility(View.GONE);
                                            showToast("Whoops, make sure all the fields are filled out. "
                                                    + "You must invite at least one user to the meeting.");
                                        }
                                    });
                                }
                            });

                            Log.d(TAG, "Firebase Token: " + idToken);
                        } else {
                            Log.d(TAG, "Couldn't connect to Firebase :(");
                            showToast("Error getting user credentials :(");
                        }
                    }
                });
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

                Intent intent = new Intent(getApplicationContext(), MeetingListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

//                startActivity(new Intent(this, activity));
//                switchActivity(MeetingListActivity.class);
            } catch (UserRecoverableAuthIOException e){
                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            } catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}


