package edu.calbaptist.android.projectmeetings;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.http.FileContent;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import edu.calbaptist.android.projectmeetings.async.user.GetUserAsync;
import edu.calbaptist.android.projectmeetings.exceptions.RestClientException;
import edu.calbaptist.android.projectmeetings.java_phoenix_channels.Channel;
import edu.calbaptist.android.projectmeetings.java_phoenix_channels.Envelope;
import edu.calbaptist.android.projectmeetings.java_phoenix_channels.IErrorCallback;
import edu.calbaptist.android.projectmeetings.java_phoenix_channels.IMessageCallback;
import edu.calbaptist.android.projectmeetings.java_phoenix_channels.ISocketCloseCallback;
import edu.calbaptist.android.projectmeetings.java_phoenix_channels.ISocketOpenCallback;
import edu.calbaptist.android.projectmeetings.java_phoenix_channels.Socket;
import edu.calbaptist.android.projectmeetings.models.Meeting;
import edu.calbaptist.android.projectmeetings.models.User;
import edu.calbaptist.android.projectmeetings.utils.DriveFiles;
import edu.calbaptist.android.projectmeetings.utils.rest.RestClientUserCallback;

/**
 *  Meeting Activity
 *  Handles actions taken during a meeting.
 *
 *  @author Caleb Solorio
 *  @version 0.7.0 12/3/17
 */
public class MeetingActivity extends AppCompatActivity
        implements View.OnClickListener, ViewTreeObserver.OnGlobalLayoutListener {
    private static final String TAG = "MeetingActivity";

    public static final String MEETING_KEY = "meeting";
    public static final String USER_KEY = "user";

    private Socket socket;
    private Channel channel;

    private Meeting meeting;
    private User user;
    private HashMap<String, User> users = new HashMap<>();

    TextView textToolbarTitle;
    TextView textObjective;

    private long timeLimit;
    private long initialTimePassed =0;

    ProgressBar progressBar;
    TextView textClockTime;
    TextView textClockHint;
    TextView textConnecting;

    MeetingMessagePagerAdapter mPagerAdapter;
    ViewPager mViewPager;

    private boolean connecting = true;
    private boolean connectingAnimationFinished = false;
    private boolean started = false;
    CountDownTimer timer;

    private StringBuilder stringBuilder;
    private Iterator iterator;

    private int[] applauseIcons = new int[10];
    private ImageButton buttonApplause;
    private ImageButton buttonSendMessage;
    private int animIndex = 0;

    private MenuItem mRecordButton;
    private MediaRecorder mMediaRecorder;
    private boolean mStartRecording = false;
    private static String mFilePath;

    RelativeLayout rootView;

    // Requesting permission to RECORD_AUDIO and CAMERA
    private boolean permissionToRecordAccepted = false;
    private boolean permissionToTakePhotos = false;
    private String [] permissions = {android.Manifest.permission.RECORD_AUDIO,
                                        Manifest.permission.CAMERA};
    private static final int REQUEST_MULTIPLE_PERMISSION = 200;
    private static final String LOG_TAG = "AudioRecord";

    private static final int CAMERA_REQUEST = 1888;
    private String mCurrentPhotoPath;
    private String mCurrentPhotoName;

    /**
     * Creates the view and assigns all initial properties.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);

        meeting = (Meeting) getIntent().getSerializableExtra(MEETING_KEY);
        user = (User) getIntent().getSerializableExtra(USER_KEY);

        connectToWebsocket();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_meeting);
        toolbar.setNavigationIcon(R.drawable.ic_close_white);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(new NavigationView.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        textToolbarTitle = (TextView) findViewById(R.id.text_toolbar_title);
        textToolbarTitle.setText(meeting.getName());

        textObjective = (TextView) findViewById(R.id.text_objective);
        textObjective.setText(meeting.getObjective());

        textConnecting = findViewById(R.id.text_connecting);

        timeLimit = meeting.getTimeLimit();

        textClockTime = (TextView) findViewById(R.id.text_clock_time);
        textClockHint = (TextView) findViewById(R.id.text_clock_hint);

        progressBar = findViewById(R.id.progress_bar_meeting);
        progressBar.setOnClickListener(this);
        progressBar.setProgress(100);
        startConnectionAnimation();

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mPagerAdapter = new MeetingMessagePagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.view_pager_meeting);
        mViewPager.setAdapter(mPagerAdapter);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_MULTIPLE_PERMISSION);

        buttonApplause = (ImageButton) findViewById(R.id.button_applause);
        applauseIcons[0] = R.id.ic_applause_1;
        applauseIcons[1] = R.id.ic_applause_2;
        applauseIcons[2] = R.id.ic_applause_3;
        applauseIcons[3] = R.id.ic_applause_4;
        applauseIcons[4] = R.id.ic_applause_5;
        applauseIcons[5] = R.id.ic_applause_6;
        applauseIcons[6] = R.id.ic_applause_7;
        applauseIcons[7] = R.id.ic_applause_8;
        applauseIcons[8] = R.id.ic_applause_9;
        applauseIcons[9] = R.id.ic_applause_10;

        buttonApplause.setOnClickListener(this);

        buttonSendMessage = (ImageButton) findViewById(R.id.button_send_message);
        buttonSendMessage.setOnClickListener(this);

        rootView = (RelativeLayout) findViewById(R.id.layout_meeting_root);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    /**
     * Handles OnClick events.
     * @param view the view in which the event occurred.
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.progress_bar_meeting:
                if(!started && user.getUid().equals(meeting.getUid())) {
                    try {
                        channel.push("start_meeting", null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.button_applause:
                ScaleAnimation clapAnimation = new ScaleAnimation(1f, .8f, 1f, .8f,
                        Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, 0.5f);
                clapAnimation.setDuration(80);
                buttonApplause.startAnimation(clapAnimation);

                try {
                    channel.push("applause");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button_send_message:
                EditText editText = findViewById(R.id.edit_text_message);
                String text = editText.getText().toString();

                if(!text.isEmpty()) {
                    editText.setText("");

                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getApplicationWindowToken(), 0);

                    //Sending a message. This library uses Jackson for JSON serialization
                    ObjectNode node = new ObjectNode(JsonNodeFactory.instance)
                            .put("msg", text);

                    try {
                        channel.push("msg", node);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                ScaleAnimation tapAnimation = new ScaleAnimation(1f, .8f, 1f, .8f,
                        Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, 0.5f);
                tapAnimation.setDuration(80);
                buttonSendMessage.startAnimation(tapAnimation);
                break;
        }
    }

    /**
     * Handles the back button being pressed.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            socket.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the keyboard coming in and out of focus.
     */
    @Override
    public void onGlobalLayout() {
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        int screenHeight = rootView.getRootView().getHeight();
        int keypadHeight = screenHeight - r.bottom;

        if (keypadHeight > screenHeight * 0.15) {
            findViewById(R.id.button_applause).setVisibility(View.GONE);
            findViewById(R.id.button_send_message).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.button_send_message).setVisibility(View.GONE);
            findViewById(R.id.button_applause).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Executes on completion of a permission request.
     * @param requestCode The code associated with the outcome of the request.
     * @param permissions The permissions requested.
     * @param grantResults The results of the permissions requested.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_MULTIPLE_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                permissionToTakePhotos = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted && !permissionToTakePhotos ) finish();
    }

    /**
     * Executes after a recording concludes.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (mStartRecording) {
            recordPressed(mStartRecording);
        }
    }

    /**
     * Starts a timer and updates UI components accoordingly.
     * @param timeTotal
     */
    private void startTimer(final long timeTotal) {
        if(initialTimePassed < meeting.getTimeLimit()) {
            final int countdownInterval = 25;

            textClockHint.setText("In Progress");

            timer = new CountDownTimer(timeTotal - initialTimePassed, countdownInterval) {

                public void onTick(long millisUntilFinished) {
                    long minutes = millisUntilFinished / 60000;
                    long seconds = (millisUntilFinished % 60000) / 1000 + 1;
                    if (seconds == 60) {
                        seconds = 0;
                        minutes++;
                    }

                    textClockTime.setText(String.format("%02d", minutes) +
                            ":" + String.format("%02d", seconds));

                    if(connectingAnimationFinished) {
                        long progress = 1000 * millisUntilFinished / timeTotal;
                        animateProgressBar((int) progress, countdownInterval, false);
                    } else {
                    }
                }

                public void onFinish() {
                    progressBarAnimateFinished();
                }

            }.start();
        } else {
            progressBarAnimateFinished();
        }
    }

    /**
     * Creates a UI component for a new message.
     * @param message
     */
    private void newMessage(String message) {
        int pos = mPagerAdapter.getCurrentPos();
        int count = mPagerAdapter.getCount();

        mPagerAdapter.addMessage(message);

        if(pos + 1 == count) {
            mViewPager.setCurrentItem(count);
        }
    }

    /**
     * Initializes the connection animations.
     */
    private void startConnectionAnimation() {
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar_meeting);

        final ObjectAnimator animationRtl = ObjectAnimator
                .ofInt(progressBar, "progress", 0);
        animationRtl.setDuration(500); // 0.5 second
        animationRtl.setInterpolator(new DecelerateInterpolator());

        final ObjectAnimator animationLtr = ObjectAnimator
                .ofInt(progressBar, "progress", 1000);
        animationLtr.setDuration(500); // 0.5 second
        animationLtr.setInterpolator(new DecelerateInterpolator());

        animationRtl.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                progressBar.setRotation(90);
                progressBar.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(connecting) {
                    animationLtr.start();
                } else {
                    int duration = 500;
                    float ratio = initialTimePassed > 0 ?
                            (1 - (float)(initialTimePassed + duration)/meeting.getTimeLimit()) : 1;
                    int progress = ratio > 0 ? (int) (1000 * ratio) : 1000;

                    animateProgressBar(progress, duration, true);

                    if(textConnecting.getVisibility() == View.VISIBLE) {
                        stopConnectionAnimation();
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });

        animationLtr.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                progressBar.setRotation(-90);
                progressBar.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                animationRtl.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });

        animationLtr.start();

        if(connecting) {
            final RelativeLayout clockInfo = findViewById(R.id.meeting_clock_info);
            Animation shrinkAnimation = AnimationUtils
                    .loadAnimation(getApplicationContext(), R.anim.anim_shrink_to_middle);
            shrinkAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation) {
                    clockInfo.setVisibility(View.GONE);
                    textConnecting.setVisibility(View.VISIBLE);
                    Animation growAnimation = AnimationUtils
                            .loadAnimation(getApplicationContext(), R.anim.anim_grow_from_middle);
                    textConnecting.startAnimation(growAnimation);
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });
            clockInfo.startAnimation(shrinkAnimation);
        } else {
            stopConnectionAnimation();
        }
    }

    /**
     * Halts the connection animations.
     */
    private void stopConnectionAnimation() {
        final RelativeLayout clockInfo = findViewById(R.id.meeting_clock_info);

        Animation shrinkAnimation =
                AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_shrink_to_middle);
        shrinkAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                textConnecting.setVisibility(View.GONE);
                clockInfo.setVisibility(View.VISIBLE);
                Animation growAnimation = AnimationUtils
                        .loadAnimation(getApplicationContext(), R.anim.anim_grow_from_middle);
                clockInfo.startAnimation(growAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        textConnecting.startAnimation(shrinkAnimation);
    }

    /**
     * Animates the progress of the progress bar.
     * @param progress the amount the progress bar should be filled.
     * @param duration the length of the animation.
     * @param setInterpolator sets whether or not to use an interpolator.
     */
    private void animateProgressBar(int progress, int duration, boolean setInterpolator) {
        progressBar.setRotation(-90);
        progressBar.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        final ObjectAnimator animation = ObjectAnimator
                .ofInt(progressBar, "progress", progress);
        animation.setDuration(duration);
        if(setInterpolator) {
            animation.setInterpolator(new DecelerateInterpolator());
        }

        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) { }

            @Override
            public void onAnimationEnd(Animator animator) {
                connectingAnimationFinished = true;
            }

            @Override
            public void onAnimationCancel(Animator animator) { }

            @Override
            public void onAnimationRepeat(Animator animator) { }
        });

        animation.start();
    }

    /**
     * Resets the progress bar.
     */
    private void progressBarAnimateFinished() {
        animateProgressBar(1000, 500, false);
        textClockTime.setText("00:00");
        textClockHint.setText("Finished");
    }

    /**
     * Initializes a UI component to show applause.
     */
    private void showApplause() {
        if (animIndex >= applauseIcons.length) {
            animIndex = 0;
        }
        final ImageView image = (ImageView) findViewById(applauseIcons[animIndex]);
        image.setVisibility(View.VISIBLE);

        Animation fadeInAnimation =
                AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_float_up);
        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                image.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        image.startAnimation(fadeInAnimation);
        animIndex++;
    }

    /**
     * Initializes a connection with websockets.
     */
    private void connectToWebsocket() {
        final String url = getString(R.string.ws_endpoint)
                + "?token=" + user.getFirebaseToken();
        final String topic = "meeting:" + meeting.getMid();

        try {
            // First connect to the websocket endpoint, then join the appropriate meeting channel.
            socket = new Socket(url);
            socket.onOpen(new ISocketOpenCallback() {
                @Override
                public void onOpen() {
                    channel = socket.chan(topic, null);

                    try {
                        channel.join().receive("ok", new IMessageCallback() {
                            @Override
                            public void onMessage(final Envelope envelope) {
                                connecting = false;
                                started = envelope.getPayload()
                                        .get("response").get("in_progress").asBoolean();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        stopConnectionAnimation();
                                        if(started) {
                                            initialTimePassed = envelope.getPayload()
                                                    .get("response").get("time_passed").asLong();
                                            startTimer(meeting.getTimeLimit());
                                        } else {
                                            long minutes = timeLimit / 60000;
                                            long seconds = (timeLimit % 60000) / 1000;
                                            textClockTime.setText(String.format("%02d", minutes) +
                                                    ":" + String.format("%02d", seconds));
                                        }
                                    }
                                });

                            }
                        });

                        /*
                        Channels will broadcast information to all listeners.
                        The following specifies how to handle this information.
                         */

                        channel.on("msg", new IMessageCallback() {
                            @Override
                            public void onMessage(Envelope envelope) {
                                final String message = envelope.getPayload().get("msg").asText();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        newMessage(message);
                                    }
                                });
                            }
                        });

                        channel.on("applause", new IMessageCallback() {
                            @Override
                            public void onMessage(Envelope envelope) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showApplause();
                                    }
                                });
                            }
                        });

                        channel.on("start_meeting", new IMessageCallback() {
                            @Override
                            public void onMessage(Envelope envelope) {
                                started = true;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        startTimer(meeting.getTimeLimit());
                                    }
                                });
                            }
                        });

                        channel.on("presence_state", new IMessageCallback() {
                            @Override
                            public void onMessage(Envelope envelope) {
                                connecting = false;

                                stringBuilder = new StringBuilder("Welcome, " +
                                        user.getDisplayName().split(" ")[0] + "!");

                                iterator = envelope.getPayload().fieldNames();

                                while(iterator.hasNext()) {
                                    final String uid = (String) iterator.next();

                                    if(!users.containsKey(uid)) {
                                        new GetUserAsync(GetUserAsync.GET_BY_U_ID,
                                                uid, new AsyncPresenceStateCallback()).execute();
                                    }
                                }
                            }
                        });

                        channel.on("presence_diff", new IMessageCallback() {
                            @Override
                            public void onMessage(Envelope envelope) {
                                HashMap<String, Boolean> existingUsers = new HashMap<>();
                                Iterator existingUsersIterator = users.keySet().iterator();

                                while (existingUsersIterator.hasNext()) {
                                    String uid = (String) existingUsersIterator.next();
                                    existingUsers.put(uid, false);
                                }

                                final Iterator iterator = envelope.getPayload().fieldNames();

                                while(iterator.hasNext()) {
                                    final String uid = (String) iterator.next();

                                    if(users.containsKey(uid)) {
                                        existingUsers.put(uid, true);
                                    } else {
                                        new GetUserAsync(GetUserAsync.GET_BY_U_ID,
                                                uid, new AsyncPresenceDiffCallback()).execute();
                                    }
                                }

                                existingUsersIterator = existingUsers.keySet().iterator();
                                while (existingUsersIterator.hasNext()) {
                                    String uid = (String) existingUsersIterator.next();
                                    if(!existingUsers.get(uid)) {
                                        String message = users.get(uid)
                                                .getDisplayName() + " left the meeting";
                                        newMessage(message);
                                    }
                                }
                            }
                        });

                    } catch (Exception e) {
                        Log.e(TAG, "Failed to join channel " + topic, e);
                    }
                }
            })
                    .onClose(new ISocketCloseCallback() {
                        @Override
                        public void onClose() {
                            Log.d(TAG, "onClose: Closed Websocket Connection");
                        }
                    })
                    .onError(new IErrorCallback() {
                        @Override
                        public void onError(final String reason) {
                            Log.d(TAG, "Error connecting to websocket: " + reason);
                        }
                    })
                    .connect();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the activity's menu.
     * @param menu the menu to initialize.
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_meeting, menu);

        mRecordButton = menu.findItem(R.id.record);
        mRecordButton.setVisible(user.getUid().equals(meeting.getUid()));

        return true;
    }

    /**
     * Called when a menu item is selected.
     * @param item The meu item in question.
     * @return true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_photo:
                // Navigate to the new photo activity
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    java.io.File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(this,
                                "edu.calbaptist.android.projectmeetings.fileprovider",
                                photoFile);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    }
                }
                return true;
            case R.id.record:
                recordPressed(mStartRecording);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Creates an image file for a new photo.
     */
    private java.io.File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        java.io.File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        java.io.File image = java.io.File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        mCurrentPhotoName = image.getName();
        return image;
    }

    /**
     * Handles the return data from an activity.
     * @param requestCode The code associated with the request.
     * @param resultCode The code specifying the outcome of the request.
     * @param data The data returned from the request.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            uploadToDrive(new java.io.File(mCurrentPhotoPath), "image/jpeg");
        }
    }

    /**
     * Initiate/stop a recording.
     */
    private void recordPressed(boolean recording) {
        if (!recording) {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String formattedDate = df.format(c.getTime());
            mFilePath = getDir("recordings", MODE_PRIVATE).getAbsolutePath();
            mFilePath += "/" + formattedDate + ".aac";

            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
            mMediaRecorder.setOutputFile(mFilePath);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            mRecordButton.setIcon(R.drawable.ic_stop_recording);

            try {
                mMediaRecorder.prepare();
            } catch (IOException e) {
                Log.e(TAG, "recordPressed: ", e);
            }

            mMediaRecorder.start();
            mStartRecording = true;
        } else {
            mRecordButton.setIcon(R.drawable.ic_start_recording);
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mStartRecording = false;

            // Upload the file to Drive
            uploadToDrive(new File(mFilePath), "audio/*");

            mFilePath = null;
        }
    }

    /**
     * Upload a file to the appropriate Drive folder.
     */
    private void uploadToDrive(final java.io.File uploadFile, final String mimeType) {
        final com.google.api.services.drive.model.File driveFile =
                new com.google.api.services.drive.model.File();
        driveFile.setName(user.getDisplayName()
                .replace(" ", "_").concat(new Date().toString()));
        driveFile.setParents(Collections.singletonList(meeting.getDriveFolderId()));
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    com.google.api.services.drive.model.File file =
                            DriveFiles.getInstance().getDriveService().files()
                                    .create(driveFile, new FileContent(mimeType, uploadFile))
                                    .setFields("id, parents").execute();

                    String text = user.getDisplayName();

                    switch (mimeType) {
                        case "audio/*":
                            text += " uploaded an audio recording to Google Drive!";
                            break;
                        case "image/jpeg":
                            text += " uploaded an image to Google Drive!";
                    }

                    ObjectNode node = new ObjectNode(JsonNodeFactory.instance)
                            .put("msg", text);

                    channel.push("msg", node);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Specifies the RestClientUserCallback implementation after getting users already in the meeting.
     */
    private class AsyncPresenceStateCallback implements RestClientUserCallback {
        @Override
        public void onTaskExecuted(User u) {
            final StringBuilder stringBuilder = new StringBuilder("Welcome, " +
                    user.getDisplayName().split(" ")[0] + "!");

            users.put(u.getUid(), u);

            if(!iterator.hasNext() && users.size() > 1) {
                stringBuilder.append(" Here's who else is here:\n");

                Iterator userIterator = users.values().iterator();

                while (userIterator.hasNext()) {
                    User iteratorUser = (User) userIterator.next();
                    if(!iteratorUser.getUid().equals(user.getUid())) {
                        stringBuilder.append(iteratorUser.getDisplayName() + "\n");
                    }
                }
            }

            final String message = stringBuilder.toString();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(users.size() > 1) {
                        newMessage(message);
                    } else {
                        ObjectNode node = new ObjectNode(JsonNodeFactory.instance)
                                .put("msg", user.getDisplayName() + " joined the meeting!");

                        try {
                            channel.push("msg", node);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        @Override
        public void onTaskFailed(RestClientException e) {
            e.printStackTrace();
        }

        @Override
        public void onExceptionRaised(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Specifies the RestClientUserCallback implementation after getting users joining the meeting.
     */
    private class AsyncPresenceDiffCallback implements RestClientUserCallback {
        @Override
        public void onTaskExecuted(User u) {
            if(!users.containsKey(u.getUid())){
                users.put(u.getUid(), u);
                final String message = u.getDisplayName().split(" ")[0]
                        + " joined the meeting!";

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        newMessage(message);
                    }
                });
            }
        }

        @Override
        public void onTaskFailed(RestClientException e) {
            e.printStackTrace();
        }

        @Override
        public void onExceptionRaised(Exception e) {
            e.printStackTrace();
        }
    }
}
