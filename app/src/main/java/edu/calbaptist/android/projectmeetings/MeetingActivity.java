package edu.calbaptist.android.projectmeetings;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import edu.calbaptist.android.projectmeetings.Exceptions.RestClientException;
import edu.calbaptist.android.projectmeetings.javaphoenixchannels.Channel;
import edu.calbaptist.android.projectmeetings.javaphoenixchannels.Envelope;
import edu.calbaptist.android.projectmeetings.javaphoenixchannels.IErrorCallback;
import edu.calbaptist.android.projectmeetings.javaphoenixchannels.IMessageCallback;
import edu.calbaptist.android.projectmeetings.javaphoenixchannels.ISocketCloseCallback;
import edu.calbaptist.android.projectmeetings.javaphoenixchannels.ISocketOpenCallback;
import edu.calbaptist.android.projectmeetings.javaphoenixchannels.Socket;

/**
 *  Meeting Activity
 *  Handles actions taken during a meeting.
 *
 *  @author Caleb Solorio
 *  @version 0.7.0 12/3/17
 */
public class MeetingActivity extends AppCompatActivity {
    private final String TAG = "MeetingActivity";
    private final String MEETING_KEY = "meeting";
    private final String USER_KEY = "user";

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

    MeetingMessagesPagerAdapter mPagerAdapter;
    ViewPager mViewPager;

    private boolean connecting = true;
    private boolean connectingAnimationFinished = false;
    private boolean started = false;
    CountDownTimer timer;

    private int[] applauseIcons = new int[10];
    private ImageButton buttonApplause;
    private ImageButton buttonSendMessage;
    private int animIndex = 0;

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        textToolbarTitle = (TextView) findViewById(R.id.text_toolbar_title);
        textToolbarTitle.setText(meeting.getName());

        textObjective = (TextView) findViewById(R.id.text_objective);
        textObjective.setText(meeting.getObjective());

        textConnecting = findViewById(R.id.textConnecting);

        timeLimit = meeting.getTimeLimit();

        textClockTime = (TextView) findViewById(R.id.textClockTime);
        textClockHint = (TextView) findViewById(R.id.textClockHint);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!started && user.getUid().equals(meeting.getUid())) {
                    try {
                        channel.push("start_meeting", null);
//                        TODO: Start recording audio.
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        progressBar.setProgress(100);
        startConnectionAnimation();

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mPagerAdapter = new MeetingMessagesPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);

        buttonApplause = (ImageButton) findViewById(R.id.button_applause);
        applauseIcons[0] = R.id.applause_icon_1;
        applauseIcons[1] = R.id.applause_icon_2;
        applauseIcons[2] = R.id.applause_icon_3;
        applauseIcons[3] = R.id.applause_icon_4;
        applauseIcons[4] = R.id.applause_icon_5;
        applauseIcons[5] = R.id.applause_icon_6;
        applauseIcons[6] = R.id.applause_icon_7;
        applauseIcons[7] = R.id.applause_icon_8;
        applauseIcons[8] = R.id.applause_icon_9;
        applauseIcons[9] = R.id.applause_icon_10;

        buttonApplause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScaleAnimation clapAnimation = new ScaleAnimation(1f, .8f, 1f, .8f,
                        Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, 0.5f);
                clapAnimation.setDuration(80);
                buttonApplause.startAnimation(clapAnimation);

                try {
                    channel.push("applause");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        buttonSendMessage = (ImageButton) findViewById(R.id.button_send_message);
        buttonSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });

        final RelativeLayout rootView = (RelativeLayout) findViewById(R.id.meeting_root_view);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
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
        });
    }

    /**
     * Starts a timer and updates UI components accoordingly.
     * @param timeTotal
     */
    private void startTimer(final long timeTotal) {
        if(initialTimePassed < meeting.getTimeLimit()) {
            Log.d(TAG, "startTimer: STSRTING TIMER");
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

                    textClockTime.setText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds));

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
    private void newMessage(MeetingMessage message) {
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
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        final ObjectAnimator animationRtl = ObjectAnimator.ofInt(progressBar, "progress", 0);
        animationRtl.setDuration(500); // 0.5 second
        animationRtl.setInterpolator(new DecelerateInterpolator());

        final ObjectAnimator animationLtr = ObjectAnimator.ofInt(progressBar, "progress", 1000);
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

                    Log.d(TAG, "onAnimationEnd: " + ratio + " " + progress);

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
            final RelativeLayout clockInfo = findViewById(R.id.clockInfo);
            Animation shrinkAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shrink_to_middle);
            shrinkAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation) {
                    clockInfo.setVisibility(View.GONE);
                    textConnecting.setVisibility(View.VISIBLE);
                    Animation growAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.grow_from_middle);
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
        final RelativeLayout clockInfo = findViewById(R.id.clockInfo);

        Animation shrinkAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shrink_to_middle);
        shrinkAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                textConnecting.setVisibility(View.GONE);
                clockInfo.setVisibility(View.VISIBLE);
                Animation growAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.grow_from_middle);
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

        final ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", progress);
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

        Animation fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.float_up);
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
            socket = new Socket(url);

            socket.onOpen(new ISocketOpenCallback() {
                @Override
                public void onOpen() {
                    channel = socket.chan(topic, null);

                    try {
                        channel.join().receive("ok", new IMessageCallback() {
                            @Override
                            public void onMessage(final Envelope envelope) {
                                Log.d(TAG, "JOINED with " + envelope.toString());

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
                                            textClockTime.setText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
                                        }
                                    }
                                });

                            }
                        });

                        channel.on("msg", new IMessageCallback() {
                            @Override
                            public void onMessage(Envelope envelope) {
                                Log.d(TAG, "onMessage: " + envelope.toString());
                                final String message = envelope.getPayload().get("msg").asText();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        newMessage(new MeetingMessage(message, false));
                                    }
                                });
                            }
                        });

                        channel.on("applause", new IMessageCallback() {
                            @Override
                            public void onMessage(Envelope envelope) {
                                Log.d(TAG, "onApplause: " + envelope.toString());
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
                                Log.d(TAG, "on meeting start: " + envelope.toString());
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
                                Log.d(TAG, "presence_state: " + envelope.toString());
                                connecting = false;

                                final Iterator iterator = envelope.getPayload().fieldNames();

                                while(iterator.hasNext()) {
                                    final String uid = (String) iterator.next();
                                    Log.d(TAG, "onMessage: UID " + (users == null));

                                    if(!users.containsKey(uid)) {
                                        AsyncTask.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                RestClient.getUserByUid(uid, user.getFirebaseToken(), new Callback.RestClientUser() {
                                                    @Override
                                                    void onTaskExecuted(User user) {
                                                        users.put(uid, user);

                                                        StringBuilder stringBuilder = new StringBuilder("Welcome, " +
                                                                user.getDisplayName().split(" ")[0] + "!");

                                                        if(!iterator.hasNext() && users.size() > 1) {
                                                            stringBuilder.append(" Here's who else is here:\n");

                                                            for(Object obj: users.entrySet().toArray()) {
                                                                User u = (User) obj;

                                                                if(u.getUid() != user.getUid()) {
                                                                    stringBuilder.append(u.getDisplayName() + "\"");
                                                                }
                                                            }
                                                        }

                                                        final String message = stringBuilder.toString();
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                newMessage(new MeetingMessage(message.toString(), false));
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    void onTaskFailed(RestClientException e) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    void onExceptionRaised(Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }
                            }
                        });

                        channel.on("presence_diff", new IMessageCallback() {
                            @Override
                            public void onMessage(Envelope envelope) {
                                Log.d(TAG, "presence_diff: " + envelope.toString());

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
                                        AsyncTask.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                RestClient.getUserByUid(uid, user.getFirebaseToken(), new Callback.RestClientUser() {
                                                    @Override
                                                    void onTaskExecuted(User user) {
                                                        if(!users.containsKey(uid)){
                                                            users.put(uid, user);
                                                            final String message = user.getDisplayName().split(" ")[0]
                                                                    + " joined the meeting!";

                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    newMessage(new MeetingMessage(message, false));
                                                                }
                                                            });
                                                        }
                                                    }

                                                    @Override
                                                    void onTaskFailed(RestClientException e) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    void onExceptionRaised(Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }

                                existingUsersIterator = existingUsers.keySet().iterator();
                                while (existingUsersIterator.hasNext()) {
                                    String uid = (String) existingUsersIterator.next();
                                    if(!existingUsers.get(uid)) {
                                        String message = users.get(uid).getDisplayName() + " left the meeting";
                                        newMessage(new MeetingMessage(message, false));
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

//        TODO: Navigate into photo menu on menu button select

        return true;
    }

}
