/**
 * Copyright (C) 2015~2050 by foolstudio. All rights reserved.
 * 
 * ��Դ�ļ��д��벻����������������ҵ��;�����߱�������Ȩ��
 * 
 * ���ߣ�������
 * 
 * �������䣺foolstudio@qq.com
 * 
*/

package com.traffic.locationremind.common.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.activity.AlarmActivity;
import com.traffic.locationremind.baidu.location.activity.MainActivity;
import com.traffic.locationremind.baidu.location.object.NotificationObject;
import com.traffic.locationremind.baidu.location.service.RemonderLocationService;

import java.util.HashMap;
import java.util.Map;

public class NotificationUtil {

	public final static int notificationId = 4;
	public final static int changeNotificationId = 2;
	public final static int arriveNotificationId = 3;
	//private Context mContext;
	// NotificationManager ： 是状态栏通知的管理类，负责发通知、清楚通知等。
	private NotificationManager manager;
	// 定义Map来保存Notification对象
	private Map<Integer, Notification> map = null;

	public NotificationUtil(Context context) {
		//this.mContext = context;
		// NotificationManager 是一个系统Service，必须通过 getSystemService()方法来获取。
		manager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		map = new HashMap<Integer, Notification>();
	}

	public Notification   showNotification(Context context,int notificationId, NotificationObject mNotificationObject) {
		//notification();

		// 判断对应id的Notification是否已经显示， 以免同一个Notification出现多次
		if (!map.containsKey(notificationId)) {

			String name = "reminding";//渠道名字
			String id = "com.traffic.location.remind"; // 渠道ID
			String description = "my_package_first_channel"; // 渠道解释说明
			//PendingIntent pendingIntent;//非紧急意图，可设置可不设置
			//判断是否是8.0上设备
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				int importance = NotificationManager.IMPORTANCE_HIGH;
				NotificationChannel mChannel = null;
				if (mChannel == null) {
					mChannel = new NotificationChannel(id, name, importance);
					mChannel.setDescription(description);
					mChannel.enableLights(true); //是否在桌面icon右上角展示小红点
					mChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);//设置在锁屏界面上显示这条通知
					mChannel.setSound(null, null);
					manager.createNotificationChannel(mChannel);
				}
			}

			// 创建通知对象
			//Notification notification = new Notification();
			NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
			notification.setPriority(Notification.PRIORITY_HIGH);// 设置该通知优先级
			// 设置通知栏滚动显示文字
			//notification.tickerText = mContext.getResources().getString(R.string.arrived_reminder);
			notification.setTicker(context.getResources().getString(R.string.arrived_reminder));;
			// 设置显示时间
			//notification.when = System.currentTimeMillis();
			notification.setWhen(System.currentTimeMillis());
			notification.setChannelId(id);
			// 设置通知显示的图标
			//notification.icon = R.drawable.cm_mainmap_notice_green;
			notification.setSmallIcon(R.mipmap.notification_icon);
			notification.setColor(Color.parseColor("#880000FF"));
			notification.setContentText(context.getString(R.string.line_background_hint));
			// 设置通知的特性: 通知被点击后，自动消失
			notification.setAutoCancel(true);
			//notification.flags = Notification.FLAG_AUTO_CANCEL;
			// 设置点击通知栏操作
			Intent in = new Intent(context, MainActivity.class);// 点击跳转到指定页面
			PendingIntent pIntent = PendingIntent.getActivity(context, 0, in, 0);
			notification.setContentIntent(pIntent);

			//notification.contentIntent = pIntent;
			// 设置通知的显示视图
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.reminder_notify);

			//remoteViews.setTextViewCompoundDrawablesRelative(R.id.line,mNotificationObject.getLineName());
			if(mNotificationObject != null) {
				remoteViews.setTextViewText(R.id.line, mNotificationObject.getLineName());
				remoteViews.setTextViewText(R.id.start, mNotificationObject.getStartStation());
				remoteViews.setTextViewText(R.id.end, mNotificationObject.getEndStation());
				remoteViews.setTextViewText(R.id.next, mNotificationObject.getNextStation());
				//remoteViews.setTextViewText(R.id.time, mNotificationObject.getTime());
			}

			// 设置暂停按钮的点击事件
			//Intent close = new Intent(context,MainActivity.class);// 设置跳转到对应界面
			//close.setAction(RemonderLocationService.CLOSE_REMINDER_SERVICE);
			//PendingIntent pauseIn = PendingIntent.getService(context, 0, in, 0);
			// 这里可以通过Bundle等传递参数
			//remoteViews.setOnClickPendingIntent(R.id.close, pauseIn);
			/********** 简单分隔 **************************/
			// 设置通知的显示视图
			notification.setCustomContentView(remoteViews);
			// 发出通知
			Notification notifi = notification.build();

			//manager.notify(notificationId,notifi);
			map.put(notificationId, notifi);// 存入Map中
            return notifi;
		}
		return map.get(notificationId);
	}

	public Notification arrivedNotification(Context context,int notificationId) {
		//notification();

		// 判断对应id的Notification是否已经显示， 以免同一个Notification出现多次
		//if (!map.containsKey(notificationId)) {

		String name = "my_android_pm";//渠道名字
		String id = "my_package_channel_2"; // 渠道ID
		String description = "my_package_first_channel2"; // 渠道解释说明
		PendingIntent pendingIntent;//非紧急意图，可设置可不设置
		//判断是否是8.0上设备
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			int importance = NotificationManager.IMPORTANCE_HIGH;
			NotificationChannel mChannel = null;
			if (mChannel == null) {
				mChannel = new NotificationChannel(id, name, importance);
				mChannel.setDescription(description);
				mChannel.enableLights(true); //是否在桌面icon右上角展示小红点
				mChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);//设置在锁屏界面上显示这条通知
				manager.createNotificationChannel(mChannel);
			}
		}

		// 创建通知对象
		//Notification notification = new Notification();
		NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
		// 设置通知栏滚动显示文字
		//notification.tickerText = mContext.getResources().getString(R.string.arrived_reminder);
		//notification.setVibrate(new long[]{500, 500, 500, 500, 500, 500});
        //notification.setContentText(context.getResources().getString(R.string.arrive_station_title));
        notification.setContentTitle(context.getResources().getString(R.string.location));
		// 设置显示时间
		//notification.when = System.currentTimeMillis();
		//notification.setWhen(System.currentTimeMillis());
		notification.setChannelId(id);
		// 设置通知显示的图标
		//notification.icon = R.drawable.cm_mainmap_notice_green;
		notification.setSmallIcon(R.mipmap.notification_icon);
		notification.setColor(Color.parseColor("#880000FF"));
		// 设置通知的特性: 通知被点击后，自动消失
		notification.setAutoCancel(false);
		// 发出通知
		Notification notifi = notification.build();

		//manager.notify(notificationId,notifi);
		return notifi;
		//manager.notify(notificationId, notification);
		//map.put(notificationId, notifi);// 存入Map中
		//}
	}

	public void changeNotification(Context context,int notificationId,String changeHint) {
		//notification();

		// 判断对应id的Notification是否已经显示， 以免同一个Notification出现多次
		//if (!map.containsKey(notificationId)) {

			String name = "my_package_channel";//渠道名字
			String id = "my_package_channel_1"; // 渠道ID
			String description = "my_package_first_channel"; // 渠道解释说明
			PendingIntent pendingIntent;//非紧急意图，可设置可不设置
			//判断是否是8.0上设备
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				int importance = NotificationManager.IMPORTANCE_HIGH;
				NotificationChannel mChannel = null;
				if (mChannel == null) {
					mChannel = new NotificationChannel(id, name, importance);
					mChannel.setDescription(description);
					mChannel.enableLights(true); //是否在桌面icon右上角展示小红点
					mChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);//设置在锁屏界面上显示这条通知
					manager.createNotificationChannel(mChannel);
				}
			}

			// 创建通知对象
			//Notification notification = new Notification();
			NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
			// 设置通知栏滚动显示文字
			//notification.tickerText = mContext.getResources().getString(R.string.arrived_reminder);
            notification.setContentText(changeHint);
		    notification.setVibrate(new long[]{500, 500, 500, 500, 500, 500});
            notification.setContentTitle(context.getResources().getString(R.string.changet_hint_tile));
			notification.setChannelId(id);
			// 设置通知显示的图标
			//notification.icon = R.drawable.cm_mainmap_notice_green;
			notification.setSmallIcon(R.mipmap.notification_icon);
			notification.setColor(Color.parseColor("#880000FF"));
			// 设置通知的特性: 通知被点击后，自动消失
			notification.setAutoCancel(false);
			//notification.flags = Notification.FLAG_AUTO_CANCEL;
			// 设置点击通知栏操作
			Intent in = new Intent(context, MainActivity.class);// 点击跳转到指定页面
			PendingIntent pIntent = PendingIntent.getActivity(context, 0, in, 0);
			notification.setContentIntent(pIntent);
			//notification.contentIntent = pIntent;
			// 发出通知
			Notification notifi = notification.build();

			manager.notify(notificationId,notifi);

			//manager.notify(notificationId, notification);
			//map.put(notificationId, notifi);// 存入Map中
		//}
	}

	/**
	 * 取消通知操作
	 *
	 * @description：
	 * @author ldm
	 * @date 2016-5-3 上午9:32:47
	 */
	public void cancel(int notificationId) {
		manager.cancel(notificationId);
		map.remove(notificationId);
	}

	public Notification updateProgress(Context context,int notificationId, NotificationObject mNotificationObject) {
		Notification notify = map.get(notificationId);
		if (null != notify) {
			// 修改进度条
            notify.contentView.setTextViewText(R.id.line,mNotificationObject.getLineName());
            notify.contentView.setTextViewText(R.id.start,mNotificationObject.getStartStation());
            notify.contentView.setTextViewText(R.id.end,mNotificationObject.getEndStation());
            notify.contentView.setTextViewText(R.id.next,mNotificationObject.getNextStation());
            //notify.contentView.setTextViewText(R.id.time,mNotificationObject.getTime());
			//manager.notify(notificationId, notify);
		}
		return notify;
	}
}