package com.traffic.locationremind.baidu.location.utils;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.activity.AlarmActivity;
import com.traffic.locationremind.baidu.location.object.NotificationObject;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataManager;

import java.util.Map;

public class NotificationUtils extends ContextWrapper {

    private NotificationManager mManager;
    public static final String ANDROID_CHANNEL_ID = "com.baidu.baidulocationdemo";
    public static final String ANDROID_CHANNEL_NAME = "ANDROID CHANNEL";

    public NotificationUtils(Context base) {
        super(base);
        createChannels();
    }

    public static void sendHint(Context context,boolean isArrive, String title, String content, String change) {
        Intent intent = new Intent(context, AlarmActivity.class);
        intent.putExtra("arrive", isArrive);
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.putExtra("change", change);
        context.startActivity(intent);
    }

    public static NotificationObject createNotificationObject(Context context, Map<Integer, String> lineDirection,StationInfo currentStation, StationInfo nextStation) {
        if (currentStation == null) {
            return null;
        }
        String linename = "";
        String currentStationName = "";
        String direction = "";
        String nextStationName = "";
        String time = "2分钟";
        if (currentStation != null) {
            linename = DataManager.getInstance(context).getLineInfoList().get(currentStation.lineid).linename;
            currentStationName = context.getResources().getString(R.string.current_station) + currentStation.getCname();
            direction = lineDirection.get(currentStation.lineid) + context.getResources().getString(R.string.direction);
        }
        if (nextStation != null) {
            nextStationName = context.getResources().getString(R.string.next_station) + nextStation.getCname();
        }
        NotificationObject mNotificationObject = new NotificationObject(linename, currentStationName, direction, nextStationName, time);
        return mNotificationObject;
    }

    public void createChannels() {

//        // create android channel
//        NotificationChannel androidChannel = new NotificationChannel(ANDROID_CHANNEL_ID,
//                ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
//        // Sets whether notifications posted to this channel should display notification lights
//        androidChannel.enableLights(true);
//        // Sets whether notification posted to this channel should vibrate.
//        androidChannel.enableVibration(true);
//        // Sets the notification light color for notifications posted to this channel
//        androidChannel.setLightColor(Color.GREEN);
//        // Sets whether notifications posted to this channel appear on the lockscreen or not
//        androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
//
//        getManager().createNotificationChannel(androidChannel);

    }

    private NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public Notification.Builder getAndroidChannelNotification(String title, String body) {
        return new Notification.Builder(getApplicationContext())
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(android.R.drawable.stat_notify_more)
                .setAutoCancel(true);
    }
}