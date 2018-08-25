/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.traffic.locationremind.baidu.location.activity;

import android.animation.*;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.notification.ThemeUtils;


/**
 * Base activity class that changes the app window's color based on the current hour.
 */
public abstract class BaseActivity extends AppCompatActivity {
    public static final TypeEvaluator<Integer> ARGB_EVALUATOR = new ArgbEvaluator();
    /** Sets the app window color on each frame of the {@link #mAppColorAnimator}. */
    private final AppColorAnimationListener mAppColorAnimationListener
            = new AppColorAnimationListener();

    /** The current animator that is changing the app window color or {@code null}. */
    private ValueAnimator mAppColorAnimator;

    /** Draws the app window's color. */
    private ColorDrawable mBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Allow the content to layout behind the status and navigation bars.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        final  int color = ThemeUtils.resolveColor(this, android.R.attr.windowBackground);
        adjustAppColor(color, false /* animate */);
        setStatusBar(getResources().getColor(R.color.clock_gray));
    }

    protected void setStatusBar(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // 设置状态栏底色颜色
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(color);

            // 如果亮色，设置状态栏文字为黑色
            if (isLightColor(color)) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }

        //隐藏标题栏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();

    }

    /**
     * 判断颜色是不是亮色
     *
     * @param color
     * @return
     * @from https://stackoverflow.com/questions/24260853/check-if-color-is-dark-or-light-in-android
     */
    private boolean isLightColor(@ColorInt int color) {
        return ColorUtils.calculateLuminance(color) >= 0.5;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Ensure the app window color is up-to-date.
        final  int color = ThemeUtils.resolveColor(this, android.R.attr.windowBackground);
        adjustAppColor(color, false /* animate */);
    }

    /**
     * Adjusts the current app window color of this activity; animates the change if desired.
     *
     * @param color   the ARGB value to set as the current app window color
     * @param animate {@code true} if the change should be animated
     */
    protected void adjustAppColor( int color, boolean animate) {
        // Create and install the drawable that defines the window color.
        if (mBackground == null) {
            mBackground = new ColorDrawable(color);
            getWindow().setBackgroundDrawable(mBackground);
        }

        // Cancel the current window color animation if one exists.
        if (mAppColorAnimator != null) {
            mAppColorAnimator.cancel();
        }

        final int currentColor = mBackground.getColor();
        if (currentColor != color) {
            if (animate) {
                mAppColorAnimator = ValueAnimator.ofObject(ARGB_EVALUATOR, currentColor, color)
                        .setDuration(3000L);
                mAppColorAnimator.addUpdateListener(mAppColorAnimationListener);
                mAppColorAnimator.addListener(mAppColorAnimationListener);
                mAppColorAnimator.start();
            } else {
                setAppColor(color);
            }
        }
    }

    private void setAppColor( int color) {
        mBackground.setColor(color);
    }

    /**
     * Sets the app window color to the current color produced by the animator.
     */
    private final class AppColorAnimationListener extends AnimatorListenerAdapter
            implements AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            final int color = (int) valueAnimator.getAnimatedValue();
            setAppColor(color);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mAppColorAnimator == animation) {
                mAppColorAnimator = null;
            }
        }
    }
}
