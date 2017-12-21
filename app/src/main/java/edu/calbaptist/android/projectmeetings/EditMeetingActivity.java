package edu.calbaptist.android.projectmeetings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import edu.calbaptist.android.projectmeetings.async.meeting.InviteToMeetingAsync;
import edu.calbaptist.android.projectmeetings.async.meeting.UninviteFromMeetingAsync;
import edu.calbaptist.android.projectmeetings.async.meeting.UpdateMeetingAsync;
import edu.calbaptist.android.projectmeetings.exceptions.ChooseAccountException;
import edu.calbaptist.android.projectmeetings.exceptions.GooglePlayServicesAvailabilityException;
import edu.calbaptist.android.projectmeetings.exceptions.RequestPermissionException;
import edu.calbaptist.android.projectmeetings.exceptions.RestClientException;
import edu.calbaptist.android.projectmeetings.models.Meeting;
import edu.calbaptist.android.projectmeetings.utils.DriveFiles;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClientJsonCallback;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClientMeetingCallback;

/**
 *  Edit Meeting Activity
 *  Assists the user with editing a meeting.
 *
 *  @author Austin Brinegar, Caleb Solorio
 *  @version 1.0.0 12/20/17
 */

public class EditMeetingActivity extends AppCompatActivity
        implements View.OnClickListener, DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener, NumberPicker.OnValueChangeListener, TextWatcher{
    private static final String TAG = "EditMeetingActivity";

    private EditText meetingName, meetingObjective, add_invites, remove_invites;
    private Button dateButton, driveButton, submit;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private ProgressBar progressBar;

    private Calendar c;
    SimpleDateFormat dateFormatter;

    private String mDriveFolderId;
    private int mLengthMinutes;

    private boolean updateMeeting = false;
    private boolean updatePending = true;
    private boolean invitesPending = true;
    private boolean uninvitesPending = true;

    Meeting meeting;

    /**
     * Initializes EditMeetingActivity
     * @param savedInstanceState Contains any data sent from the previous activity.
     */
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_meeting);

        getSupportActionBar().setTitle("Edit Meeting");

        meeting = (Meeting) getIntent().getExtras().getSerializable("meeting");

        mDriveFolderId = meeting.getDriveFolderId();

        meetingName = findViewById(R.id.edit_text_meeting_name);
        meetingName.setText(meeting.getName());
        meetingName.addTextChangedListener(this);

        meetingObjective = findViewById(R.id.edit_text_meeting_objective);
        meetingObjective.setText(meeting.getObjective());
        meetingObjective.addTextChangedListener(this);

        c = Calendar.getInstance();
        c.setTimeInMillis(meeting.getTime());

        dateFormatter = new SimpleDateFormat("h:mm a, MMM dd, YYYY");

        timePickerDialog = new TimePickerDialog(EditMeetingActivity.this, this,
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);

        datePickerDialog = new DatePickerDialog(EditMeetingActivity.this, this,
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        dateButton = findViewById(R.id.button_date_picker);
        dateButton.setText(dateFormatter.format(c.getTime()));
        dateButton.setOnClickListener(this);

        driveButton = findViewById(R.id.button_drive_folder);
        driveButton.setText("Valid Folder");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String fileName = DriveFiles.getInstance().getDriveService()
                            .files().get(mDriveFolderId).execute().getName();
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
        driveButton.setOnClickListener(this);

        mLengthMinutes = (int) meeting.getTimeLimit()/60000;

        NumberPicker np = (NumberPicker) findViewById(R.id.number_picker);
        np.setMinValue(1);
        np.setMaxValue(mLengthMinutes > 60 ? mLengthMinutes : 60);
        np.setValue(mLengthMinutes);
        np.setOnValueChangedListener(this);

        add_invites = findViewById(R.id.edit_text_add_invites);

        remove_invites = findViewById(R.id.edit_text_remove_invites);
        remove_invites.setVisibility(View.VISIBLE);

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
                updateMeeting = true;
                datePickerDialog.show();
                break;
            case R.id.button_drive_folder:
                updateMeeting = true;
                Intent intent = new Intent(getApplicationContext(), FolderListActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.button_submit:
                editMeeting();
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
        updateMeeting = true;
    }

    /**
     * Handles value changes in the NumberPicker object.
     * @param picker The NumberPicker in question.
     * @param oldVal The old value of the NumberPicker.
     * @param newVal The new value of the NumberPicker.
     */
    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal){
        mLengthMinutes = newVal;
        updateMeeting = true;
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
        updateMeeting = true;
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
     * Executes before an EditText's value is changed.
     */
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    /**
     * Executes when an EditText's value is changed.
     */
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    /**
     * Executes after an EditText's value is changed.
     * @param editable the EditText in question.
     */
    @Override
    public void afterTextChanged(Editable editable) {
        updateMeeting = true;
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
     * Initializes the update of a Meeting.
     */
    private void editMeeting(){
        final long millis = c.getTimeInMillis();
        final long length = (long) mLengthMinutes * 60 * 1000;

        progressBar.setVisibility(View.VISIBLE);

        if(updateMeeting) {
            final Meeting m = new Meeting.MeetingBuilder()
                    .setMid(meeting.getMid())
                    .setName(meetingName.getText().toString())
                    .setObjective(meetingObjective.getText().toString())
                    .setTime(millis)
                    .setTimeLimit(length)
                    .setDriveFolderId(mDriveFolderId)
                    .build();

            updateMeetingData(m);
        } else {
            updatePending = false;
        }

        if(!add_invites.getText().toString().isEmpty()) {
            final ArrayList invitationsToAdd =
                    new ArrayList<String>(Arrays.asList(add_invites.getText()
                            .toString().split("\\s*,\\s*")));
            invite(meeting.getMid(), invitationsToAdd);
        } else {
            invitesPending = false;
        }

        if(!remove_invites.getText().toString().isEmpty()) {
            final ArrayList invitationsToRemove =
                    new ArrayList<String>(Arrays.asList(remove_invites.getText()
                            .toString().split("\\s*,\\s*")));
            uninvite(meeting.getMid(), invitationsToRemove);
        } else {
            uninvitesPending = false;
        }
    }

    /**
     * Handles the switching of activities to ensure smooth navigation.
     */
    private void switchActivity(){
        if(updatePending || invitesPending || uninvitesPending) {
            return;
        }

        Intent intent = new Intent(getApplicationContext(), MeetingListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Initializes the update of a meeting.
     * @param meeting The meeting to update.
     */
    private void updateMeetingData(Meeting meeting) {
        new UpdateMeetingAsync(meeting, new AsyncMeetingCallback()).execute();
    }

    /**
     * Initializes new invitations to a meeting.
     * @param mId Specifies the meeting.
     * @param invitationsToAdd Contains the emails of the users to invite.
     */
    private void invite(final String mId, final ArrayList<String> invitationsToAdd) {
        try {
            DriveFiles.getInstance().shareFolder(mDriveFolderId, invitationsToAdd);
        } catch (GooglePlayServicesAvailabilityException e) {
            e.printStackTrace();
        } catch (ChooseAccountException e) {
            e.printStackTrace();
        } catch (RequestPermissionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new InviteToMeetingAsync(mId, invitationsToAdd,
                new AsyncInviteCallback()).execute();
    }

    /**
     * Initializes the removal of users from a meeting.
     * @param mId Specifies the meeting.
     * @param invitationsToRemove Contains the emails of the users to remove.
     */
    private void uninvite(final String mId, final ArrayList<String> invitationsToRemove) {
        try {
            DriveFiles.getInstance().unshareFolder(mDriveFolderId, invitationsToRemove);
        } catch (GooglePlayServicesAvailabilityException e) {
            e.printStackTrace();
        } catch (ChooseAccountException e) {
            e.printStackTrace();
        } catch (RequestPermissionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new UninviteFromMeetingAsync(mId, invitationsToRemove,
                new AsyncUninviteCallback()).execute();
    }

    /**
     * Shows the given error string in a short Toast.
     */
    private void showError(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
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
     * Specifies the RestClientMeetingCallback implementation after updating a meeting.
     */
    private class AsyncMeetingCallback implements RestClientMeetingCallback {
        @Override
        public void onTaskExecuted(final Meeting meeting) {
            Log.d(TAG, "onTaskExecuted: " + meeting.getName());
            updatePending = false;
            switchActivity();
        }

        @Override
        public void onTaskFailed(RestClientException e) {
            Log.d(TAG, "onTaskFailed with " + e.getResponseCode()
                    + ": " + e.getJson().toString());
            showError("Unable to update meeting");
        }

        @Override
        public void onExceptionRaised(Exception e) {
            Log.d(TAG, "onExceptionRaised: " + e.getMessage());
            showError("Unable to update meeting");
        }
    }

    /**
     * Specifies the RestClientJsonCallback implementation after inviting users to a meeting.
     */
    private class AsyncInviteCallback implements RestClientJsonCallback {
        @Override
        public void onTaskExecuted(JSONObject json) {
            Log.d(TAG, "inviteToMeeting onTaskExecuted: "
                    + json.toString());
            invitesPending = false;
            switchActivity();
        }

        @Override
        public void onTaskFailed(RestClientException e) {
            Log.d(TAG, "inviteToMeeting onTaskFailed: "
                    + e.getMessage());
            e.printStackTrace();
            showError("Unable to invite user(s)");
        }

        @Override
        public void onExceptionRaised(Exception e) {
            Log.d(TAG, "inviteToMeeting onExceptionRaised: "
                    + e.getMessage());
            e.printStackTrace();
            showError("Unable to invite user(s)");
        }
    }

    /**
     * Specifies the RestClientJsonCallback implementation after uninviting users from a meeting.
     */
    private class AsyncUninviteCallback implements RestClientJsonCallback {
        @Override
        public void onTaskExecuted(JSONObject json) {
            uninvitesPending = false;
            switchActivity();
        }

        @Override
        public void onTaskFailed(RestClientException e) {
            showError("Unable to uninvite user(s)");
        }

        @Override
        public void onExceptionRaised(Exception e) {
            showError("Unable to uninvite user(s)");
        }
    }
 }

