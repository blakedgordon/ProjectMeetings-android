package edu.calbaptist.android.projectmeetings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TimePicker;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import edu.calbaptist.android.projectmeetings.exceptions.ChooseAccountException;
import edu.calbaptist.android.projectmeetings.exceptions.GooglePlayServicesAvailabilityException;
import edu.calbaptist.android.projectmeetings.exceptions.RequestPermissionException;
import edu.calbaptist.android.projectmeetings.exceptions.RestClientException;

/**
 * Created by Austin on 12/5/2017.
 */

public class EditMeetingActivity extends AppCompatActivity {
    Calendar c;

    EditText meetingName, meetingObjective, add_invites, remove_invites;
    Button submit;

    Button dateButton;
    Button driveButton;

    private String mDriveFolderId;
    private int mLengthMinutes;

    Meeting meeting;

    private static final String TAG = "EditMeetingActivity";
    SharedPreferences prefs = App.context.getSharedPreferences(
            "edu.calbaptist.android.projectmeetings.Account_Name",
            Context.MODE_PRIVATE);

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_creation);

        getSupportActionBar().setTitle("Edit Meeting");

        meeting = (Meeting) getIntent().getExtras().getSerializable("meeting");

        meetingName = findViewById(R.id.MeetingName);
        meetingName.setText(meeting.getName());

        meetingObjective = findViewById(R.id.MeetingObjective);
        meetingObjective.setText(meeting.getObjective());

        c = Calendar.getInstance();
        c.setTimeInMillis(meeting.getTime());

        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);
        final int mHour = c.get(Calendar.HOUR_OF_DAY);
        final int mMinute = c.get(Calendar.MINUTE);

        final SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm a, MMM dd, YYYY");

        final TimePickerDialog timePickerDialog = new TimePickerDialog(EditMeetingActivity.this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hour,
                                          int minute) {
                        c.set(Calendar.HOUR_OF_DAY, hour);
                        c.set(Calendar.MINUTE, minute);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dateButton.setText(dateFormatter.format(c.getTime()));
                            }
                        });
                    }
                }, mHour, mMinute, false);

        final DatePickerDialog datePickerDialog = new DatePickerDialog(EditMeetingActivity.this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        c.set(year, monthOfYear, dayOfMonth);

                        timePickerDialog.show();

                    }
                }, mYear, mMonth, mDay);

        dateButton = findViewById(R.id.button_date_picker);
        dateButton.setText(dateFormatter.format(c.getTime()));
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });

        driveButton = findViewById(R.id.button_drive_folder);
        driveButton.setText("Valid Folder");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String fileName = DriveFiles.getInstance().getDriveService()
                            .files().get(meeting.getDriveFolderId()).execute().getName();
                    driveButton.setText(fileName);
                } catch (GooglePlayServicesAvailabilityException e) {
                    e.printStackTrace();
                } catch (ChooseAccountException e) {
                    e.printStackTrace();
                } catch (RequestPermissionException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        driveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FolderViewActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        mLengthMinutes = (int) meeting.getTimeLimit()/60000;

        NumberPicker np = (NumberPicker) findViewById(R.id.np);
        np.setMinValue(1);
        np.setMaxValue(mLengthMinutes > 60 ? mLengthMinutes : 60);
        np.setValue(mLengthMinutes);

        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                //Display the newly selected number from picker
                mLengthMinutes = newVal;
            }
        });

        add_invites = findViewById(R.id.add_invites);

        remove_invites = findViewById(R.id.remove_invites);
        remove_invites.setVisibility(View.VISIBLE);

        submit = findViewById(R.id.Submit);
        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                editMeeting();
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

    private void editMeeting(){
        final long millis = c.getTimeInMillis();
        final long length = (long) mLengthMinutes * 60 * 1000;

        final String firebaseToken = prefs.getString("firebase_token",null);
        Log.d(TAG, "editMeeting: TOKEN " +firebaseToken);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.edit_meeting_spinner);
        progressBar.setVisibility(View.VISIBLE);

        final Meeting m = new Meeting.MeetingBuilder()
                .setMid(meeting.getMid())
                .setName(meetingName.getText().toString())
                .setObjective(meetingObjective.getText().toString())
                .setTime(millis)
                .setTimeLimit(length)
                .setDriveFolderId(mDriveFolderId)
                .build();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                RestClient.updateMeeting(m, firebaseToken, new Callback.RestClientMeeting() {
                    @Override
                    void onTaskExecuted(final Meeting meeting) {
                        Log.d(TAG, "onTaskExecuted: " + meeting.getName());
                        switchActivity();
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

        final ArrayList invitationsToAdd =
                new ArrayList<String>(Arrays.asList(add_invites.getText().toString().split("\\s*,\\s*")));
        if(invitationsToAdd.size() > 0) {
            invite(m.getMid(), firebaseToken, invitationsToAdd);
        }

        final ArrayList invitationsToRemove =
                new ArrayList<String>(Arrays.asList(remove_invites.getText().toString().split("\\s*,\\s*")));
        if(invitationsToAdd.size() > 0) {
            uninvite(m.getMid(), firebaseToken, invitationsToRemove);
        }
    }

    private void switchActivity(){
        Intent intent = new Intent(getApplicationContext(), MeetingListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void invite(final String mId, final String token, final ArrayList<String> invitationsToAdd) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                RestClient.inviteToMeeting(mId, invitationsToAdd,
                        token, new Callback.RestClientJson() {

                            @Override
                            void onTaskExecuted(JSONObject json) {
                                Log.d(TAG, "inviteToMeeting onTaskExecuted: "
                                        + json.toString());
                                switchActivity();
                            }

                            @Override
                            void onTaskFailed(RestClientException e) {
                                Log.d(TAG, "inviteToMeeting onTaskFailed: "
                                        + e.getMessage());
                                e.printStackTrace();
                            }

                            @Override
                            void onExceptionRaised(Exception e) {
                                Log.d(TAG, "inviteToMeeting onExceptionRaised: "
                                        + e.getMessage());
                                e.printStackTrace();
                            }
                        });
            }
        });
    }

    private void uninvite(final String mId, final String token, final ArrayList<String> invitationsToRemove) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                RestClient.uninviteFromMeeting(mId, invitationsToRemove,
                        token, new Callback.RestClientJson() {

                            @Override
                            void onTaskExecuted(JSONObject json) {
                                Log.d(TAG, "uninviteFromMeeting onTaskExecuted: "
                                        + json.toString());
                                switchActivity();
                            }

                            @Override
                            void onTaskFailed(RestClientException e) {
                                Log.d(TAG, "uninviteFromMeeting onTaskFailed: "
                                        + e.getMessage());
                                e.printStackTrace();
                            }

                            @Override
                            void onExceptionRaised(Exception e) {
                                Log.d(TAG, "uninviteFromMeeting onExceptionRaised: "
                                        + e.getMessage());
                                e.printStackTrace();
                            }
                        });
            }
        });
    }
 }

