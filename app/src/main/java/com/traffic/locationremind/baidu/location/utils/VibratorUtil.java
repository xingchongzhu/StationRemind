package com.traffic.locationremind.baidu.location.utils;

import android.app.Activity;
import android.app.Service;
import android.media.AudioAttributes;
import android.os.Vibrator;
public class VibratorUtil {
    /**
     * final Activity activity ：调用该方法的Activity实例 long milliseconds ：震动的时长，单位是毫秒
     * long[] pattern ：自定义震动模式 。数组中数字的含义依次是[静止时长，震动时长，静止时长，震动时长。。。]时长的单位是毫秒
     * boolean isRepeat ： 是否反复震动，如果是true，反复震动，如果是false，只震动一次
     */
    // 一直震动多少秒
    public static void Vibrate(final Activity activity, long milliseconds) {
        Vibrator vib = (Vibrator) activity
                .getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(milliseconds);
    }

    // 按照我们传进去的数组进行间歇性的震动
    public static void Vibrate(final Activity activity, long[] pattern) {
        Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(pattern, 0, new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build());
    }

    // 停止震动
    public static void StopVibrate(final Activity activity) {
        Vibrator vib = (Vibrator) activity
                .getSystemService(Service.VIBRATOR_SERVICE);
        vib.cancel();
    }
}
