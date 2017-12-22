package edu.calbaptist.android.projectmeetings;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import edu.calbaptist.android.projectmeetings.async.meeting.CreateMeetingAsync;
import edu.calbaptist.android.projectmeetings.exceptions.ChooseAccountException;
import edu.calbaptist.android.projectmeetings.exceptions.GooglePlayServicesAvailabilityException;
import edu.calbaptist.android.projectmeetings.exceptions.RequestPermissionException;
import edu.calbaptist.android.projectmeetings.exceptions.RestClientException;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClientMeetingCallback;
import edu.calbaptist.android.projectmeetings.models.Meeting;
import edu.calbaptist.android.projectmeetings.utils.DriveFiles;

/**
 *  Create Meeting Activity
 *  Assists the user with creating a meeting.
 *
 *  @author Austin Brinegar, Caleb Solorio
 *  @version 1.0.0 12/20/17
 */

public class CreateMeetingActivity extends AppCompatActivity
        implements View.OnClickListener, DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener, NumberPicker.OnValueChangeListener {
    private static final String TAG = "CreateMeetingActivity";

    private EditText meetingName, meetingObjective, add_invites;
    private Button dateButton, driveButton, submit;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private ProgressBar progressBar;

    private Calendar c;
    SimpleDateFormat dateFormatter;

    private String mDriveFolderId;
    private int mLengthMinutes;

    /**
     * Initializes CreateMeetingActivity
     * @param savedInstanceState Contains any data sent from the previous activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_meeting);

        getSupportActionBar().setTitle(getString(R.string.create_meeting));

        meetingName = findViewById(R.id.edit_text_meeting_name);
        meetingObjective = findViewById(R.id.edit_text_meeting_objective);

        c = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("h:mm a, MMM dd, YYYY");

        timePickerDialog = new TimePickerDialog(CreateMeetingActivity.this, this,
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);

        datePickerDialog = new DatePickerDialog(CreateMeetingActivity.this, this,
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        dateButton = findViewById(R.id.button_date_picker);
        dateButton.setText(dateFormatter.format(c.getTime()));
        dateButton.setOnClickListener(this);

        driveButton = findViewById(R.id.button_drive_folder);
        driveButton.setOnClickListener(this);

        mLengthMinutes = 1;

        NumberPicker np = (NumberPicker) findViewById(R.id.number_picker);
        np.setMinValue(1);
        np.setMaxValue(60);
        np.setOnValueChangedListener(this);

        add_invites = findViewById(R.id.edit_text_add_invites);

        submit = findViewById(R.id.button_submit);
        submit.setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    /**
     * Handles OnClick events.
     * @param view the view in which the event occurred.
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_date_picker:
                datePickerDialog.show();
                break;
            case R.id.button_drive_folder:
                Intent intent = new Intent(getApplicationContext(), FolderListActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.button_submit:
                createMeeting();
                break;
        }

    }

    /**
     * Handles Date changes in a DatePicker
     * @param view the DatePicker object in question.
     * @param year The year selected.
     * @param monthOfYear The month seleted.
     * @param dayOfMonth The day selected.
     */
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        c.set(year, monthOfYear, dayOfMonth);
        timePickerDialog.show();
    }

    /**
     * Handles value changes in the NumberPicker object.
     * @param picker The NumberPicker in question.
     * @param oldVal The old value of the NumberPicker.
     * @param newVal The new value of the NumberPicker.
     */
    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal){
        //Display the newly selected number from picker
        mLengthMinutes = newVal;
    }

    /**
     * Handles value changes in the TimePicker.
     * @param view The TimePicker in question.
     * @param hour The hour selected.
     * @param minute The minute selected.
     */
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

    /**
     * Handles the return data from an activity.
     * @param requestCode The code associated with the request.
     * @param resultCode The code specifying the outcome of the request.
     * @param data The data returned from the request.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                driveButton.setText(data.getStringExtra("folder_name"));
                mDriveFolderId = data.getStringExtra("folder_id");
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                showToast("Unable to find Google Drive folder.");
            }
        }
    }

    /**
     * Initializes the creation of a new Meeting.
     */
    private void createMeeting(){
        final long millis = c.getTimeInMillis();
        final long length = (long) mLengthMinutes * 60 * 1000;

        progressBar.setVisibility(View.VISIBLE);

        final ArrayList invitationsToAdd =
                new ArrayList<String>(Arrays.asList(add_invites
                        .getText().toString().split("\\s*,\\s*")));

        try {
            DriveFiles.getInstance()
                    .shareFolder(mDriveFolderId, invitationsToAdd);
        } catch (GooglePlayServicesAvailabilityException e) {
            e.printStackTrace();
        } catch (ChooseAccountException e) {
            e.printStackTrace();
        } catch (RequestPermissionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Meeting meeting = new Meeting.MeetingBuilder()
                .setName(meetingName.getText().toString())
                .setObjective(meetingObjective.getText().toString())
                .setTime(millis)
                .setTimeLimit(length)
                .setDriveFolderId(mDriveFolderId)
                .setInvites(invitationsToAdd)
                .build();

        new CreateMeetingAsync(meeting, new AsyncCallback()).execute();
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
     * Specifies the RestClientMeetingCallback and RestClientUserCallback
     * implementation after creating a meeting.
     */
    private class AsyncCallback implements RestClientMeetingCallback {
        public void onTaskExecuted(Meeting m) {
            Intent intent = new Intent(getApplicationContext(), MeetingListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        @Override
        public void onTaskFailed(RestClientException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                }
            });
            showToast("Whoops, make sure all the fields are filled out. "
                    + "You must invite at least one user to the meeting.");
        }

        @Override
        public void onExceptionRaised(Exception e) {
            progressBar.setVisibility(View.GONE);
            showToast("Oh no, an unknown error occured :(");
        }
    }
}


