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
 *  Assists the user with editing a mMeeting.
 *
 *  @author Austin Brinegar, Caleb Solorio
 *  @version 1.0.0 12/20/17
 */

public class EditMeetingActivity extends AppCompatActivity
        implements View.OnClickListener, DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener, NumberPicker.OnValueChangeListener, TextWatcher{
    public static final String TAG = "EditMeetingActivity";
    public static final String MEETING_KEY = "meeting";

    private EditText mMeetingName, mMeetingObjective, mInvitesToAdd, mInvitesToRemove;
    private Button mDateButton, mDriveButton, mSubmitButton;
    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;
    private ProgressBar mProgressBar;

    private Calendar mCalendar;
    SimpleDateFormat mDateFormatter;

    private String mDriveFolderId;
    private int mLengthMinutes;

    private boolean mUpdateMeeting = false;
    private boolean mUpdatePending = true;
    private boolean mInvitesPending = true;
    private boolean mUninvitesPending = true;

    private Meeting mMeeting;

    /**
     * Initializes EditMeetingActivity
     * @param savedInstanceState Contains any data sent from the previous activity.
     */
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_meeting);

        getSupportActionBar().setTitle(getString(R.string.edit_meeting));

        mMeeting = (Meeting) getIntent().getExtras().getSerializable(MEETING_KEY);

        mDriveFolderId = mMeeting.getDriveFolderId();

        mMeetingName = findViewById(R.id.edit_text_meeting_name);
        mMeetingName.setText(mMeeting.getName());
        mMeetingName.addTextChangedListener(this);

        mMeetingObjective = findViewById(R.id.edit_text_meeting_objective);
        mMeetingObjective.setText(mMeeting.getObjective());
        mMeetingObjective.addTextChangedListener(this);

        mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(mMeeting.getTime());

        mDateFormatter = new SimpleDateFormat("h:mm a, MMM dd, YYYY");

        mTimePickerDialog = new TimePickerDialog(EditMeetingActivity.this, this,
                mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), false);

        mDatePickerDialog = new DatePickerDialog(EditMeetingActivity.this, this,
                mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));

        mDateButton = findViewById(R.id.button_date_picker);
        mDateButton.setText(mDateFormatter.format(mCalendar.getTime()));
        mDateButton.setOnClickListener(this);

        mDriveButton = findViewById(R.id.button_drive_folder);
        mDriveButton.setText(App.CONTEXT.getString(R.string.valid_folder));
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String fileName = DriveFiles.getInstance().getDriveService()
                            .files().get(mDriveFolderId).execute().getName();
                    mDriveButton.setText(fileName);
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
        mDriveButton.setOnClickListener(this);

        mLengthMinutes = (int) mMeeting.getTimeLimit()/60000;

        NumberPicker np = (NumberPicker) findViewById(R.id.number_picker);
        np.setMinValue(1);
        np.setMaxValue(mLengthMinutes > 60 ? mLengthMinutes : 60);
        np.setValue(mLengthMinutes);
        np.setOnValueChangedListener(this);

        mInvitesToAdd = findViewById(R.id.edit_text_add_invites);

        mInvitesToRemove = findViewById(R.id.edit_text_remove_invites);
        mInvitesToRemove.setVisibility(View.VISIBLE);

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
                mUpdateMeeting = true;
                mDatePickerDialog.show();
                break;
            case R.id.button_drive_folder:
                mUpdateMeeting = true;
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
        mCalendar.set(year, monthOfYear, dayOfMonth);
        mTimePickerDialog.show();
        mUpdateMeeting = true;
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
        mUpdateMeeting = true;
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
        mUpdateMeeting = true;
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
        mUpdateMeeting = true;
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
     * Initializes the update of a Meeting.
     */
    private void editMeeting(){
        final long millis = mCalendar.getTimeInMillis();
        final long length = (long) mLengthMinutes * 60 * 1000;

        mProgressBar.setVisibility(View.VISIBLE);

        if(mUpdateMeeting) {
            final Meeting m = new Meeting.MeetingBuilder()
                    .setMId(mMeeting.getMId())
                    .setName(mMeetingName.getText().toString())
                    .setObjective(mMeetingObjective.getText().toString())
                    .setTime(millis)
                    .setTimeLimit(length)
                    .setDriveFolderId(mDriveFolderId)
                    .build();

            updateMeetingData(m);
        } else {
            mUpdatePending = false;
        }

        if(!mInvitesToAdd.getText().toString().isEmpty()) {
            final ArrayList invitationsToAdd =
                    new ArrayList<String>(Arrays.asList(mInvitesToAdd.getText()
                            .toString().split("\\s*,\\s*")));
            invite(mMeeting.getMId(), invitationsToAdd);
        } else {
            mInvitesPending = false;
        }

        if(!mInvitesToRemove.getText().toString().isEmpty()) {
            final ArrayList invitationsToRemove =
                    new ArrayList<String>(Arrays.asList(mInvitesToRemove.getText()
                            .toString().split("\\s*,\\s*")));
            uninvite(mMeeting.getMId(), invitationsToRemove);
        } else {
            mUninvitesPending = false;
        }
    }

    /**
     * Handles the switching of activities to ensure smooth navigation.
     */
    private void switchActivity(){
        if(mUpdatePending || mInvitesPending || mUninvitesPending) {
            return;
        }

        Intent intent = new Intent(getApplicationContext(), MeetingListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Initializes the update of a Meeting.
     * @param meeting The Meeting to update.
     */
    private void updateMeetingData(Meeting meeting) {
        new UpdateMeetingAsync(meeting, new AsyncMeetingCallback()).execute();
    }

    /**
     * Initializes new invitations to a Meeting.
     * @param mId Specifies the Meeting.
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
     * Initializes the removal of users from a Meeting.
     * @param mId Specifies the Meeting.
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
                mProgressBar.setVisibility(View.GONE);
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
     * Specifies the RestClientMeetingCallback implementation after updating a Meeting.
     */
    private class AsyncMeetingCallback implements RestClientMeetingCallback {
        @Override
        public void onTaskExecuted(final Meeting meeting) {
            mUpdatePending = false;
            switchActivity();
        }

        @Override
        public void onTaskFailed(RestClientException e) {
            showError("Unable to update mMeeting");
        }

        @Override
        public void onExceptionRaised(Exception e) {
            showError("Unable to update mMeeting");
        }
    }

    /**
     * Specifies the RestClientJsonCallback implementation after inviting users to a Meeting.
     */
    private class AsyncInviteCallback implements RestClientJsonCallback {
        @Override
        public void onTaskExecuted(JSONObject json) {
            mInvitesPending = false;
            switchActivity();
        }

        @Override
        public void onTaskFailed(RestClientException e) {
            e.printStackTrace();
            showError("Unable to invite user(s)");
        }

        @Override
        public void onExceptionRaised(Exception e) {
            e.printStackTrace();
            showError("Unable to invite user(s)");
        }
    }

    /**
     * Specifies the RestClientJsonCallback implementation after uninviting users from a Meeting.
     */
    private class AsyncUninviteCallback implements RestClientJsonCallback {
        @Override
        public void onTaskExecuted(JSONObject json) {
            mUninvitesPending = false;
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

