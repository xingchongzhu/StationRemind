/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.traffic.locationremind.baidu.location.activity;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.animation.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.Log;
import android.view.*;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.notification.AnimatorUtils;
import com.traffic.locationremind.baidu.location.activity.BaseActivity;
import com.traffic.locationremind.baidu.location.view.CircleView;
import com.traffic.locationremind.baidu.location.utils.TimerKlaxon;

import java.util.List;

import static android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC;

public class AlarmActivity extends BaseActivity
        implements View.OnClickListener, View.OnTouchListener {
    String TAG = "AlarmActivity";

    public static final String ALARM_SNOOZE_ACTION = "com.android.deskclock.ALARM_SNOOZE";

    /**
     * AlarmActivity and AlarmService listen for this broadcast intent so that other
     * applications can dismiss the alarm (after ALARM_ALERT_ACTION and before ALARM_DONE_ACTION).
     */
    public static final String ALARM_DISMISS_ACTION = "com.android.deskclock.ALARM_DISMISS";

    /**
     * A public action sent by AlarmService when the alarm has started.
     */
    public static final String ALARM_ALERT_ACTION = "com.android.deskclock.ALARM_ALERT";

    /**
     * A public action sent by AlarmService when the alarm has stopped for any reason.
     */
    public static final String ALARM_DONE_ACTION = "com.android.deskclock.ALARM_DONE";

    /**
     * Private action used to stop an alarm with this service.
     */
    public static final String STOP_ALARM_ACTION = "STOP_ALARM";

    private static final TimeInterpolator PULSE_INTERPOLATOR =
            PathInterpolatorCompat.create(0.4f, 0.0f, 0.2f, 1.0f);
    private static final TimeInterpolator REVEAL_INTERPOLATOR =
            PathInterpolatorCompat.create(0.0f, 0.0f, 0.2f, 1.0f);


    private static final int PULSE_DURATION_MILLIS = 1000;
    private static final int ALARM_BOUNCE_DURATION_MILLIS = 500;
    private static final int ALERT_REVEAL_DURATION_MILLIS = 500;
    private static final int ALERT_FADE_DURATION_MILLIS = 500;
    private static final int ALERT_DISMISS_DELAY_MILLIS = 2000;

    private static final float BUTTON_SCALE_DEFAULT = 0.7f;
    private static final int BUTTON_DRAWABLE_ALPHA_DEFAULT = 165;

    private final Handler mHandler = new Handler();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.v(TAG, "Received broadcast: %s" + action);

            if (!mAlarmHandled) {
                switch (action) {
                    case ALARM_SNOOZE_ACTION:
                        snooze();
                        break;
                    case ALARM_DISMISS_ACTION:
                        dismiss();
                        break;
                    case ALARM_DONE_ACTION:
                        finish();
                        break;
                    default:
                        Log.i(TAG, "Unknown broadcast: %s" + action);
                        break;
                }
            } else {
                Log.v("Ignored broadcast: %s", action);
            }
        }
    };


    private boolean mAlarmHandled;
    private int mCurrentHourColor;
    private AccessibilityManager mAccessibilityManager;

    private ViewGroup mAlertView;
    private TextView mAlertTitleView;
    private TextView mAlertInfoView;

    private ViewGroup mContentView;
    private ImageView mAlarmButton;
    private ImageView mSnoozeButton;
    private ImageView mDismissButton;
    private TextView mHintView;

    private ValueAnimator mAlarmAnimator;
    private ValueAnimator mSnoozeAnimator;
    private ValueAnimator mDismissAnimator;
    private ValueAnimator mPulseAnimator;

    private int mInitialPointerIndex = MotionEvent.INVALID_POINTER_ID;

    private long[] vibrator = {500, 500};

    private String hintText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setVolumeControlStream(AudioManager.STREAM_ALARM);

        // Get the volume/camera button behavior setting

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        // Hide navigation bar to minimize accidental tap on Home key
        hideNavigationBar();

        // Close dialogs and window shade, so this is fully visible
        sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        mAccessibilityManager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);

        setContentView(R.layout.alarm_activity);

        mAlertView = (ViewGroup) findViewById(R.id.alert);
        mAlertTitleView = (TextView) mAlertView.findViewById(R.id.alert_title);
        mAlertInfoView = (TextView) mAlertView.findViewById(R.id.alert_info);

        mContentView = (ViewGroup) findViewById(R.id.content);
        mAlarmButton = (ImageView) mContentView.findViewById(R.id.alarm);
        mSnoozeButton = (ImageView) mContentView.findViewById(R.id.snooze);
        mDismissButton = (ImageView) mContentView.findViewById(R.id.dismiss);
        mHintView = (TextView) mContentView.findViewById(R.id.hint);

        final TextView content = (TextView) mContentView.findViewById(R.id.content_text);
        final TextView title = (TextView) mContentView.findViewById(R.id.title);
        final TextView change = (TextView) mContentView.findViewById(R.id.change);
        final CircleView pulseView = (CircleView) mContentView.findViewById(R.id.pulse);


        mCurrentHourColor = getResources().getColor(R.color.clock_gray);
        getWindow().setBackgroundDrawable(new ColorDrawable(mCurrentHourColor));

        mAlarmButton.setOnTouchListener(this);
        mSnoozeButton.setOnClickListener(this);
        mDismissButton.setOnClickListener(this);

        mAlarmAnimator = AnimatorUtils.getScaleAnimator(mAlarmButton, 1.0f, 0.0f);
        mSnoozeAnimator = getButtonAnimator(mSnoozeButton, Color.WHITE);
        mDismissAnimator = getButtonAnimator(mDismissButton, mCurrentHourColor);
        mPulseAnimator = ObjectAnimator.ofPropertyValuesHolder(pulseView,
                PropertyValuesHolder.ofFloat(CircleView.RADIUS, 0.0f, pulseView.getRadius()),
                PropertyValuesHolder.ofObject(CircleView.FILL_COLOR, AnimatorUtils.ARGB_EVALUATOR,
                        ColorUtils.setAlphaComponent(pulseView.getFillColor(), 0)));
        mPulseAnimator.setDuration(PULSE_DURATION_MILLIS);
        mPulseAnimator.setInterpolator(PULSE_INTERPOLATOR);
        mPulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mPulseAnimator.start();
        //VibratorUtil.Vibrate(this,vibrator);
        Intent intent = getIntent();
        TimerKlaxon.start(this, intent.getBooleanExtra("arrive", true));
        title.setText(intent.getStringExtra("title"));
        content.setText(intent.getStringExtra("content"));
        change.setText(intent.getStringExtra("change"));
        hintText = intent.getBooleanExtra("arrive", true)?getResources().getString(R.string.alarm_alert_off_text):
                getResources().getString(R.string.alarm_alert_snoozed_text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetAnimations();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        // Do this in dispatch to intercept a few of the system keys.
        Log.v(TAG, "dispatchKeyEvent: %s" + keyEvent);

        final int keyCode = keyEvent.getKeyCode();
        switch (keyCode) {
            // Volume keys and camera keys dismiss the alarm.
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                //case KeyEvent.KEYCODE_HEADSETHOOK:
                //case KeyEvent.KEYCODE_CAMERA:
                // case KeyEvent.KEYCODE_FOCUS:
                if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    snooze();
                }
                return true;

        }
        return super.dispatchKeyEvent(keyEvent);
    }

    @Override
    public void onBackPressed() {
        // Don't allow back to dismiss.
    }

    @Override
    public void onClick(View view) {
        if (mAlarmHandled) {
            return;
        }

        // If in accessibility mode, allow snooze/dismiss by double tapping on respective icons.
        if (isAccessibilityEnabled()) {
            if (view == mSnoozeButton) {
                snooze();
            } else if (view == mDismissButton) {
                dismiss();
            }
            return;
        }

        if (view == mSnoozeButton) {
            hintSnooze();
        } else if (view == mDismissButton) {
            hintDismiss();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {


        final int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {

            // Track the pointer that initiated the touch sequence.
            mInitialPointerIndex = event.getPointerId(event.getActionIndex());

            // Stop the pulse, allowing the last pulse to finish.
            mPulseAnimator.setRepeatCount(0);
        } else if (action == MotionEvent.ACTION_CANCEL) {

            // Clear the pointer index.
            mInitialPointerIndex = MotionEvent.INVALID_POINTER_ID;

            // Reset everything.
            resetAnimations();
        }

        final int actionIndex = event.getActionIndex();
        if (mInitialPointerIndex == MotionEvent.INVALID_POINTER_ID
                || mInitialPointerIndex != event.getPointerId(actionIndex)) {
            // Ignore any pointers other than the initial one, bail early.
            return true;
        }

        final int[] contentLocation = {0, 0};
        mContentView.getLocationOnScreen(contentLocation);

        final float x = event.getRawX() - contentLocation[0];
        final float y = event.getRawY() - contentLocation[1];

        final int alarmLeft = mAlarmButton.getLeft() + mAlarmButton.getPaddingLeft();
        final int alarmRight = mAlarmButton.getRight() - mAlarmButton.getPaddingRight();

        final float snoozeFraction, dismissFraction;
       /* if (mContentView.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            snoozeFraction = getFraction(alarmRight, mSnoozeButton.getLeft(), x);
            dismissFraction = getFraction(alarmLeft, mDismissButton.getRight(), x);
        } else {*/
        snoozeFraction = getFraction(alarmLeft, mSnoozeButton.getRight(), x);
        dismissFraction = getFraction(alarmRight, mDismissButton.getLeft(), x);
        //}
        setAnimatedFractions(snoozeFraction, dismissFraction);

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {

            mInitialPointerIndex = MotionEvent.INVALID_POINTER_ID;
            if (snoozeFraction == 1.0f) {
                snooze();
            } else if (dismissFraction == 1.0f) {
                dismiss();
            } else {
                if (snoozeFraction > 0.0f || dismissFraction > 0.0f) {
                    // Animate back to the initial state.
                    AnimatorUtils.reverse(mAlarmAnimator, mSnoozeAnimator, mDismissAnimator);
                } else if (mAlarmButton.getTop() <= y && y <= mAlarmButton.getBottom()) {
                    // User touched the alarm button, hint the dismiss action.
                    hintDismiss();
                }

                // Restart the pulse.
                mPulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
                if (!mPulseAnimator.isStarted()) {
                    mPulseAnimator.start();
                }
            }
        }

        return true;
    }

    private void hideNavigationBar() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /**
     * Returns {@code true} if accessibility is enabled, to enable alternate behavior for click
     * handling, etc.
     */
    private boolean isAccessibilityEnabled() {
        if (mAccessibilityManager == null || !mAccessibilityManager.isEnabled()) {
            // Accessibility is unavailable or disabled.
            return false;
        } else if (mAccessibilityManager.isTouchExplorationEnabled()) {
            // TalkBack's touch exploration mode is enabled.
            return true;
        }

        // Check if "Switch Access" is enabled.
        final List<AccessibilityServiceInfo> enabledAccessibilityServices =
                mAccessibilityManager.getEnabledAccessibilityServiceList(FEEDBACK_GENERIC);
        return !enabledAccessibilityServices.isEmpty();
    }

    private void hintSnooze() {
        final int alarmLeft = mAlarmButton.getLeft() + mAlarmButton.getPaddingLeft();
        final int alarmRight = mAlarmButton.getRight() - mAlarmButton.getPaddingRight();
        final float translationX = Math.max(mSnoozeButton.getLeft() - alarmRight, 0)
                + Math.min(mSnoozeButton.getRight() - alarmLeft, 0);
        getAlarmBounceAnimator(translationX, translationX < 0.0f ?//左右滑动
                hintText : getResources().getString(R.string.stop_hint)).start();
    }

    private void hintDismiss() {
        final int alarmLeft = mAlarmButton.getLeft() + mAlarmButton.getPaddingLeft();
        final int alarmRight = mAlarmButton.getRight() - mAlarmButton.getPaddingRight();
        final float translationX = Math.max(mDismissButton.getLeft() - alarmRight, 0)
                + Math.min(mDismissButton.getRight() - alarmLeft, 0);
        getAlarmBounceAnimator(translationX, translationX < 0.0f ?
                hintText : getResources().getString(R.string.stop_hint)).start();
    }

    /**
     * Set animators to initial values and restart pulse on alarm button.
     */
    private void resetAnimations() {
        // Set the animators to their initial values.
        setAnimatedFractions(0.0f /* snoozeFraction */, 0.0f /* dismissFraction */);
        // Restart the pulse.
        mPulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        if (!mPulseAnimator.isStarted()) {
            mPulseAnimator.start();
        }
    }

    /**
     * Perform snooze animation and send snooze intent.
     */
    private void snooze() {
        mAlarmHandled = true;

        final int colorAccent = getResources().getColor(R.color.clock_gray);//ThemeUtils.resolveColor(this, R.attr.colorAccent);
        setAnimatedFractions(1.0f /* snoozeFraction */, 0.0f /* dismissFraction */);

        final String infoText = "";
        final String accessibilityText = "";

        getAlertAnimator(mSnoozeButton, hintText, infoText,
                accessibilityText, colorAccent, colorAccent).start();
        // VibratorUtil.StopVibrate(this);
        TimerKlaxon.stop(this);

    }

    private void dismiss() {
        mAlarmHandled = true;
        setAnimatedFractions(0.0f /* snoozeFraction */, 1.0f /* dismissFraction */);
        getAlertAnimator(mDismissButton, getResources().getString(R.string.stop_hint), null /* infoText */,
                getString(R.string.alarm_alert_off_text) /* accessibilityText */,
                Color.WHITE, mCurrentHourColor).start();
        TimerKlaxon.stop(this);
        setResult(MainActivity.SHUTDOWNACTIVITY);
    }


    private void setAnimatedFractions(float snoozeFraction, float dismissFraction) {
        final float alarmFraction = Math.max(snoozeFraction, dismissFraction);
        AnimatorUtils.setAnimatedFraction(mAlarmAnimator, alarmFraction);
        AnimatorUtils.setAnimatedFraction(mSnoozeAnimator, snoozeFraction);
        AnimatorUtils.setAnimatedFraction(mDismissAnimator, dismissFraction);
    }

    private float getFraction(float x0, float x1, float x) {
        return Math.max(Math.min((x - x0) / (x1 - x0), 1.0f), 0.0f);
    }

    private ValueAnimator getButtonAnimator(ImageView button, int tintColor) {
        return ObjectAnimator.ofPropertyValuesHolder(button,
                PropertyValuesHolder.ofFloat(View.SCALE_X, BUTTON_SCALE_DEFAULT, 1.0f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, BUTTON_SCALE_DEFAULT, 1.0f),
                PropertyValuesHolder.ofInt(AnimatorUtils.BACKGROUND_ALPHA, 0, 255),
                PropertyValuesHolder.ofInt(AnimatorUtils.DRAWABLE_ALPHA,
                        BUTTON_DRAWABLE_ALPHA_DEFAULT, 255),
                PropertyValuesHolder.ofObject(AnimatorUtils.DRAWABLE_TINT,
                        AnimatorUtils.ARGB_EVALUATOR, Color.WHITE, tintColor));
    }

    private ValueAnimator getAlarmBounceAnimator(float translationX, final String text) {
        final ValueAnimator bounceAnimator = ObjectAnimator.ofFloat(mAlarmButton,
                View.TRANSLATION_X, mAlarmButton.getTranslationX(), translationX, 0.0f);
        bounceAnimator.setInterpolator(AnimatorUtils.DECELERATE_ACCELERATE_INTERPOLATOR);
        bounceAnimator.setDuration(ALARM_BOUNCE_DURATION_MILLIS);
        bounceAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animator) {
                mHintView.setText(text);
                if (mHintView.getVisibility() != View.VISIBLE) {
                    mHintView.setVisibility(View.VISIBLE);
                    ObjectAnimator.ofFloat(mHintView, View.ALPHA, 0.0f, 1.0f).start();
                }
            }
        });
        return bounceAnimator;
    }

    private Animator getAlertAnimator(final View source, final String titleResId,
                                      final String infoText, final String accessibilityText, final int revealColor,
                                      final int backgroundColor) {
        final ViewGroup containerView = (ViewGroup) findViewById(android.R.id.content);

        final Rect sourceBounds = new Rect(0, 0, source.getHeight(), source.getWidth());
        containerView.offsetDescendantRectToMyCoords(source, sourceBounds);

        final int centerX = sourceBounds.centerX();
        final int centerY = sourceBounds.centerY();

        final int xMax = Math.max(centerX, containerView.getWidth() - centerX);
        final int yMax = Math.max(centerY, containerView.getHeight() - centerY);

        final float startRadius = Math.max(sourceBounds.width(), sourceBounds.height()) / 2.0f;
        final float endRadius = (float) Math.sqrt(xMax * xMax + yMax * yMax);

        final CircleView revealView = new CircleView(this)
                .setCenterX(centerX)
                .setCenterY(centerY)
                .setFillColor(revealColor);
        containerView.addView(revealView);

        // TODO: Fade out source icon over the reveal (like LOLLIPOP version).

        final Animator revealAnimator = ObjectAnimator.ofFloat(
                revealView, CircleView.RADIUS, startRadius, endRadius);
        revealAnimator.setDuration(ALERT_REVEAL_DURATION_MILLIS);
        revealAnimator.setInterpolator(REVEAL_INTERPOLATOR);
        revealAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                mAlertView.setVisibility(View.VISIBLE);
                mAlertTitleView.setText(titleResId);

                if (infoText != null) {
                    mAlertInfoView.setText(infoText);
                    mAlertInfoView.setVisibility(View.VISIBLE);
                }
                mContentView.setVisibility(View.GONE);

                getWindow().setBackgroundDrawable(new ColorDrawable(backgroundColor));
            }
        });

        final ValueAnimator fadeAnimator = ObjectAnimator.ofFloat(revealView, View.ALPHA, 0.0f);
        fadeAnimator.setDuration(ALERT_FADE_DURATION_MILLIS);
        fadeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                containerView.removeView(revealView);
            }
        });

        final AnimatorSet alertAnimator = new AnimatorSet();
        alertAnimator.play(revealAnimator).before(fadeAnimator);
        alertAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                mAlertView.announceForAccessibility(accessibilityText);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, ALERT_DISMISS_DELAY_MILLIS);
            }
        });

        return alertAnimator;
    }
}
