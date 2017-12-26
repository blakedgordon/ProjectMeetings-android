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
 *  Assists the user with creating a mMeeting.
 *
 *  @author Austin Brinegar, Caleb Solorio
 *  @version 1.0.0 12/20/17
 */

public class CreateMeetingActivity extends AppCompatActivity
        implements View.OnClickListener, DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener, NumberPicker.OnValueChangeListener {
    public static final String TAG = "CreateMeetingActivity";

    private EditText mMeetingName, mMeetingObjective, mInvitesToAdd;
    private Button mDateButton, mDriveButton, mSubmitButton;
    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;
    private ProgressBar mProgressBar;

    private Calendar mCalendar;
    private SimpleDateFormat mDateFormatter;

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

        mMeetingName = findViewById(R.id.edit_text_meeting_name);
        mMeetingObjective = findViewById(R.id.edit_text_meeting_objective);

        mCalendar = Calendar.getInstance();
        mDateFormatter = new SimpleDateFormat("h:mm a, MMM dd, YYYY");

        mTimePickerDialog = new TimePickerDialog(CreateMeetingActivity.this, this,
                mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), false);

        mDatePickerDialog = new DatePickerDialog(CreateMeetingActivity.this, this,
                mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH));

        mDateButton = findViewById(R.id.button_date_picker);
        mDateButton.setText(mDateFormatter.format(mCalendar.getTime()));
        mDateButton.setOnClickListener(this);

        mDriveButton = findViewById(R.id.button_drive_folder);
        mDriveButton.setOnClickListener(this);

        mLengthMinutes = 1;

        NumberPicker np = (NumberPicker) findViewById(R.id.number_picker);
        np.setMinValue(1);
        np.setMaxValue(60);
        np.setOnValueChangedListener(this);

        mInvitesToAdd = findViewById(R.id.edit_text_add_invites);

        mSubmitButton = findViewById(R.id.button_submit);
        mSubmitButton.setOnClickListener(this);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    /**
     * Handles OnClick events.
     * @param view the view in which the event occurred.
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_date_picker:
                mDatePickerDialog.show();
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
        mCalendar.set(year, monthOfYear, dayOfMonth);
        mTimePickerDialog.show();
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
        mCalendar.set(Calendar.HOUR_OF_DAY, hour);
        mCalendar.set(Calendar.MINUTE, minute);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDateButton.setText(mDateFormatter.format(mCalendar.getTime()));
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
                mDriveButton.setText(data.getStringExtra("folder_name"));
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
        final long millis = mCalendar.getTimeInMillis();
        final long length = (long) mLengthMinutes * 60 * 1000;

        mProgressBar.setVisibility(View.VISIBLE);

        final ArrayList invitationsToAdd =
                new ArrayList<String>(Arrays.asList(mInvitesToAdd
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
                .setName(mMeetingName.getText().toString())
                .setObjective(mMeetingObjective.getText().toString())
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
     * implementation after creating a mMeeting.
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
                    mProgressBar.setVisibility(View.GONE);
                }
            });
            showToast("Whoops, make sure all the fields are filled out. "
                    + "You must invite at least one user to the mMeeting.");
        }

        @Override
        public void onExceptionRaised(Exception e) {
            mProgressBar.setVisibility(View.GONE);
            showToast("Oh no, an unknown error occured :(");
        }
    }
}


