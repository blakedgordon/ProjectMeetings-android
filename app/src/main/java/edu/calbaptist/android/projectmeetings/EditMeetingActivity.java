package edu.calbaptist.android.projectmeetings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import edu.calbaptist.android.projectmeetings.Exceptions.RestClientException;

/**
 * Created by Austin on 12/5/2017.
 */

public class EditMeetingActivity extends AppCompatActivity {
    Meeting meeting;

    EditText MeetingName, MeetingObjective, length;
    DatePicker date;
    TimePicker time;
    Button submit;
    private static final String TAG = "EditMeetingActivity";
    SharedPreferences prefs = App.context.getSharedPreferences(
            "edu.calbaptist.android.projectmeetings.Account_Name",
            Context.MODE_PRIVATE);

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_creation);

        meeting = (Meeting) getIntent().getExtras().getSerializable("meeting");

        MeetingName = findViewById(R.id.MeetingName);
        MeetingName.setText(meeting.getName());

        MeetingObjective = findViewById(R.id.MeetingObjective);
        MeetingObjective.setText(meeting.getObjective());

        date = findViewById(R.id.Date);

        Date mDate = new Date(meeting.getTime());

        Calendar cal = Calendar.getInstance();
        cal.setTime(mDate);
        date.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        time = findViewById(R.id.Time);
        time.setHour(mDate.getHours());
        time.setMinute(mDate.getMinutes());

        length = findViewById(R.id.Length);
        length.setText(String.valueOf(meeting.getTimeLimit()/60000));

        submit = findViewById(R.id.Submit);
        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Calendar calendar = Calendar.getInstance();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    calendar.set(date.getYear(), date.getMonth(), date.getDayOfMonth(), time.getHour(), time.getMinute());
                }
                final long millis = calendar.getTimeInMillis();
                final long mLength = Long.parseLong(length.getText().toString())*60*1000;
                editMeeting(millis, mLength);
            }
        });
    }

    private void editMeeting(long millis, long mLength){
        //TODO: Add actual invitation functionality
        ArrayList<String> invites = new ArrayList<String>(Arrays.asList(new String[]{"austin@gmail.com", "blake@gmail.com"}));
        final String firebaseToken = prefs.getString("FirebaseToken",null);
        Intent intent = getIntent();

        final Meeting meeting = new Meeting.MeetingBuilder()
                .setMid(intent.getStringExtra("meetingID"))
                .setName(MeetingName.getText().toString())
                .setObjective(MeetingObjective.getText().toString())
                .setTime(millis)
                .setTimeLimit(mLength)
                .setDriveFolderId(prefs.getString("DefaultFolder", ""))
                .setInvites(invites)
                .build();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                RestClient.updateMeeting(meeting, firebaseToken, new Callback.RestClientMeeting() {
                    @Override
                    void onTaskExecuted(Meeting m) {
                        Log.d(TAG, "onTaskExecuted: " + m.getName());
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
    }

    private void switchActivity(){
        startActivity(new Intent(this, MeetingListActivity.class));
    }
 }

