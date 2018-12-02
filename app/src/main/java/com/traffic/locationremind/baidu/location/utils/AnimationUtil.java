package com.traffic.locationremind.baidu.location.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;

public class AnimationUtil {
    public static void rotateOpen(final View v, long durationMillis) {

        AnimationSet animationSet = new AnimationSet(true);
        /**
         *  //参数1：从哪个旋转角度开始

         //参数2：转到什么角度

         //后4个参数用于设置围绕着旋转的圆的圆心在哪里

         //参数3：确定x轴坐标的类型，有ABSOLUT绝对坐标、RELATIVE_TO_SELF相对于自身坐标、RELATIVE_TO_PARENT相对于父控件的坐标

         //参数4：x轴的值，0.5f表明是以自身这个控件的一半长度为x轴

         //参数5：确定y轴坐标的类型

         //参数6：y轴的值，0.5f表明是以自身这个控件的一半长度为x轴
         */

        RotateAnimation animation = new RotateAnimation(0f, 180f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(durationMillis);//设置动画持续时间
/** 常用方法 */
        //animation.setRepeatCount(0);//设置重复次数
        //animation.setStartOffset(long startOffset);//执行前的等待时间
        //animation.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        animationSet.addAnimation(animation);
        animationSet.setFillAfter(true);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // v.clearAnimation();

                // v.setRotation(180f);


            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        // v.setAnimation(animation);
        // animation.startNow();
        v.startAnimation(animationSet);
    }

    public static void rotateClose(final View v, long durationMillis) {
        //
        AnimationSet animationSet = new AnimationSet(true);
        //参数1：从哪个旋转角度开始

        //参数2：转到什么角度

        //后4个参数用于设置围绕着旋转的圆的圆心在哪里

        //参数3：确定x轴坐标的类型，有ABSOLUT绝对坐标、RELATIVE_TO_SELF相对于自身坐标、RELATIVE_TO_PARENT相对于父控件的坐标

        //参数4：x轴的值，0.5f表明是以自身这个控件的一半长度为x轴

        //参数5：确定y轴坐标的类型

        //参数6：y轴的值，0.5f表明是以自身这个控件的一半长度为x轴
        RotateAnimation animation = new RotateAnimation(180f, 360f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(durationMillis);//设置动画持续时间

        //animation.setRepeatCount(0);//设置重复次数
        //animation.setStartOffset(long startOffset);//执行前的等待时间
        //animation.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        animationSet.addAnimation(animation);
        animationSet.setFillAfter(true);
        // v.setAnimation(animation);
        // animation.startNow();
        v.startAnimation(animationSet);
    }

}
