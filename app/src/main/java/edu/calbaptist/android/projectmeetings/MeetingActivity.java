package edu.calbaptist.android.projectmeetings;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
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

public class MeetingActivity extends AppCompatActivity {
    private final String TAG = "MeetingActivity";

    TextView textClockHint;

    MeetingMessagesPagerAdapter mPagerAdapter;
    ViewPager mViewPager;

    private Meeting meeting;
    private boolean connecting = true;

    private int[] applauseIcons = new int[10];
    private ImageButton buttonApplause;
    private ImageButton buttonSendMessage;
    private int animIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);

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

        textClockHint = (TextView) findViewById(R.id.objective2);

        final ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "CLICKED THE CENTER BUTTON");
                connecting = !connecting;
                if(connecting) {
                    showConnectionAnimation();
                }
            }
        });
        progressBar.setProgress(100);
        showConnectionAnimation();

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

                showApplause();
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

                    newMessage(new MeetingMessage(text, false));
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

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_meeting, menu);
        return true;
    }

    private void newMessage(MeetingMessage message) {
        int pos = mPagerAdapter.getCurrentPos();
        int count = mPagerAdapter.getCount();

        mPagerAdapter.addMessage(message);

        if(pos + 1 == count) {
            mViewPager.setCurrentItem(count);
        }
    }

    private void showConnectionAnimation() {
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        final ObjectAnimator animationRtl = ObjectAnimator.ofInt(progressBar, "progress", 0);
        animationRtl.setDuration(500); // 0.5 second
        animationRtl.setInterpolator(new DecelerateInterpolator());

        final ObjectAnimator animationLtr = ObjectAnimator.ofInt(progressBar, "progress", 100);
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
                    animateProgress(progressBar, 50);
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

        if(progressBar.getProgress() == 100) {
            ObjectAnimator startingAnimation = animationRtl.clone();
            startingAnimation.setStartDelay(250);
            startingAnimation.start();
        } else {
            animationLtr.start();
        }
    }



    private void animateProgress(final ProgressBar progressBar, int progress) {
        progressBar.setRotation(-90);
        progressBar.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        final ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", progress);
        animation.setDuration(500);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }

    private void showApplause() {
        if (animIndex >= applauseIcons.length) {
            animIndex = 0;
        }
        final ImageView image = (ImageView) findViewById(applauseIcons[animIndex]);
        image.setVisibility(View.VISIBLE);

        Animation fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.float_up);
        fadeInAnimation.setAnimationListener(new FadeAnimationListener(image));
        image.startAnimation(fadeInAnimation);
        animIndex++;
    }

    private class FadeAnimationListener implements Animation.AnimationListener {
        View mView;

        FadeAnimationListener(View view) {
            mView = view;
        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mView.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

}
