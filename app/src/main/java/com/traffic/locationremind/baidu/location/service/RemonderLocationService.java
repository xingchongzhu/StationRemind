package com.traffic.locationremind.baidu.location.service;

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.*;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import android.widget.Toast;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDNotifyListener;
import com.baidu.location.LocationClient;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.activity.LocationApplication;
import com.traffic.locationremind.baidu.location.activity.MainActivity;
import com.traffic.locationremind.baidu.location.listener.LocationChangerListener;
import com.traffic.locationremind.baidu.location.object.MarkObject;
import com.traffic.locationremind.baidu.location.object.NotificationObject;
import com.traffic.locationremind.baidu.location.utils.NotificationUtils;
import com.traffic.locationremind.baidu.location.view.LineNodeView;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.common.util.NotificationUtil;
import com.traffic.locationremind.common.util.PathSerachUtil;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataManager;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;
import java.util.*;

/**
 * @author baidu
 */
public class RemonderLocationService extends Service {

    private final static String TAG = "RemonderLocationService";
    public final static String CLOSE_REMINDER_SERVICE = "close.reminder.service";
    private UpdateBinder updateBinder= new UpdateBinder();
    private LocationService locationService;
    /**
     * 回调
     */
    private Callback callback;
    private LocationChangerListener mLocationChangerListener;
    public static boolean state = false;

    BDLocation currentLocation;

    private NotificationUtil mNotificationUtil;
    private boolean isReminder = false;
    private boolean locationServiceHasStart = false;
    List<StationInfo> list, tempChangeStationList;
    StationInfo nextStation, currentStation,preStation;
    private Map<Integer, String> lineDirection;
    private List<StationInfo> needChangeStationList;

    private PowerManager pm;
    private PowerManager.WakeLock wakeLock;

    public IBinder onBind(Intent intent) {
        return updateBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        this.callback = null;
        return super.onUnbind(intent);
    }

    public class UpdateBinder extends Binder {
        public RemonderLocationService getService() {
            return RemonderLocationService.this;
        }
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        // -----------location config ------------
        locationService = ((LocationApplication) getApplication()).locationService;
        // 获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService.registerListener(mListener);
        mNotificationUtil = new NotificationUtil(this);
        //startForeground(NotificationUtil.arriveNotificationId,mNotificationUtil.arrivedNotification(getApplicationContext(),NotificationUtil.arriveNotificationId));
        //创建PowerManager对象
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    }

    public void setLocationChangerListener(LocationChangerListener locationChangerListener) {
        this.mLocationChangerListener = locationChangerListener;
    }

    public void startLocationService() {
        if (locationService != null && !locationServiceHasStart) {
            locationService.start();
            locationServiceHasStart = true;
        }
    }

    public BDLocation getCurrentLocation() {
        return currentLocation;
    }

    /*****
     *
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     *
     */
    int n = 0;
    int number = 0;
    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO Auto-generated method stub

            if (null != location
                    && location.getLocType() != BDLocation.TypeServerError) {
                if (!CommonFuction.isvalidLocation(location)) {
                    Log.d(TAG, "BDAbstractLocationListener location invale ");
                    currentLocation = null;
                    return;
                }
                currentLocation = location;
                /*if (isReminder && callback != null) {
                    callback.loactionStation(location);
                }*/
                if (mLocationChangerListener != null) {
                    mLocationChangerListener.loactionStation(location);
                }

                if (list != null && list.size() > 0) {

                    StationInfo nerstStationInfo = PathSerachUtil.getNerastNextStation(location, list);//list.get(n)
                    /*if (number > 100) {
                        number = 0;
                        if (n < list.size()) {
                            n++;
                        }
                    }*/
                    number++;
                    if (nerstStationInfo != null) {
                        currentStation = nerstStationInfo;
                        int n = 0;
                        for (StationInfo stationInfo : list) {
                            if (stationInfo.getCname().equals(currentStation.getCname())) {
                                break;
                            }
                            n++;
                        }

                        if (n + 1 < list.size() - 1) {
                            nextStation = list.get(n + 1);
                        }

                        Log.d(TAG, "loactionStation getCname = " + currentStation.getCname() + " isRemind = " + isReminder+" getCity = "+currentLocation.getCity());
                        if (isReminder) {
                            double longitude = CommonFuction.convertToDouble(currentStation.getLot(), 0);
                            double latitude = CommonFuction.convertToDouble(currentStation.getLat(), 0);
                            double dis = CommonFuction.getDistanceLat(longitude, latitude, location.getLongitude(), location.getLatitude());
                            if(dis < PathSerachUtil.MINDIS) {
                                for (StationInfo stationInfo : tempChangeStationList) {
                                    if (list.get(list.size() - 1).getCname().equals(stationInfo.getCname()) &&
                                            stationInfo.getCname().equals(currentStation.getCname())) {
                                        Log.d(TAG, "arrive stationInfo.getCname()" + stationInfo.getCname());
                                        if(location != null) {
                                            MobclickAgent.onEvent(getApplicationContext(), getResources().getString(R.string.event_arrived), "到站");
                                        }
                                        tempChangeStationList.remove(stationInfo);
                                        isReminder = false;
                                        cancleNotification();
                                        NotificationUtils.sendHint(getApplicationContext(), true, getResources().getString(R.string.arrive), getResources().getString(R.string.hint_arrive_end_station), "");
                                        if (mLocationChangerListener != null) {
                                            mLocationChangerListener.stopRemind();
                                        }
                                        break;
                                    } else if (stationInfo.getCname().equals(currentStation.getCname())) {//换乘点
                                        tempChangeStationList.remove(stationInfo);
                                        if(location != null) {
                                            MobclickAgent.onEvent(getApplicationContext(), getResources().getString(R.string.event_changeStation), "换乘");
                                        }
                                        String str = String.format(getResources().getString(R.string.change_station_hint), stationInfo.getCname()) +
                                                DataManager.getInstance(getApplicationContext()).getLineInfoList().get(currentStation.lineid).linename;
                                        NotificationUtils.sendHint(getApplicationContext(), false, getResources().getString(R.string.change), str,
                                                lineDirection.get(stationInfo.lineid) + getResources().getString(R.string.direction));
                                        Log.d(TAG, "change stationInfo.getCname()" + stationInfo.getCname());
                                        break;
                                    }
                                }
                            }
                            if(preStation != currentStation)
                                updataNotification(NotificationUtils.createNotificationObject(getApplicationContext(), lineDirection, currentStation, nextStation));
                        }
                        preStation = currentStation;
                    }

                }
            }
        }
    };

    private boolean isBacrground = false;

    public boolean moveTaskToBack() {
        isBacrground = true;
        Log.d(TAG, "moveTaskToBack isRemind = " + isReminder);
        if (isReminder) {
            NotificationObject mNotificationObject = NotificationUtils.createNotificationObject(getApplicationContext(), lineDirection, currentStation, nextStation);
            setNotification(mNotificationObject);
        }
        return true;
    }

    public void moveInForeground(){
        isBacrground = false;
    }

    public void setNotification(NotificationObject mNotificationObject) {
        //保持cpu一直运行，不管屏幕是否黑屏
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RemonderLocationService");
        wakeLock.acquire();
        Notification mNotification = mNotificationUtil.showNotification(getApplicationContext(),
                NotificationUtil.notificationId, mNotificationObject);
        //startForeground(NotificationUtil.notificationId,mNotification);
        locationService.getLocationClient().enableLocInForeground(NotificationUtil.notificationId,mNotification);
    }

    public void updataNotification(NotificationObject mNotificationObject) {
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RemonderLocationService");
        wakeLock.acquire();
        locationService.getLocationClient().enableLocInForeground(NotificationUtil.notificationId,
               mNotificationUtil.updateProgress(getApplicationContext(), NotificationUtil.notificationId, mNotificationObject));
        //mNotificationUtil.updateProgress(getApplicationContext(), NotificationUtil.notificationId, mNotificationObject);
    }

    /**
     * 提供接口回调方法
     *
     * @param
     */
    public void setStartReminder(Boolean state) {
        n = 0;
        isReminder = state;
        if (!isReminder) {
            cancleNotification();
        } else if (needChangeStationList != null) {
            tempChangeStationList = new ArrayList<>(needChangeStationList);
        }
        if (isReminder) {
            //NotificationObject mNotificationObject = NotificationUtils.createNotificationObject(getApplicationContext(), lineDirection, currentStation, nextStation);
            //setNotification(mNotificationObject);
        } else {
            cancleNotification();
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setCancleReminder() {
        isReminder = false;
        if(wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        stopForeground(true);
        if (mNotificationUtil != null)
            mNotificationUtil.cancel(NotificationUtil.notificationId);
        locationService.getLocationClient().disableLocInForeground(true);// 关闭前台定位，同时移除通知栏
    }

    public void cancleNotification() {
        stopForeground(true);
        if(wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        if (mNotificationUtil != null) {
            mNotificationUtil.cancel(NotificationUtil.notificationId);
        }
        locationService.getLocationClient().disableLocInForeground(true);// 关闭前台定位，同时移除通知栏
    }

    /**
     * 回调接口
     *
     * @author lenovo
     */
    public static interface Callback {
        void setCurrentStation(String startCname, String endName, String current);

        void arriaved(boolean state);

        void loactionStation(BDLocation location);

        void errorHint(String error);
    }

    /**
     * 服务销毁的时候调用
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationService != null) {
            //locationService.getLocationClient().disableLocInForeground(true);// 关闭前台定位，同时移除通知栏
            locationService.unregisterListener(mListener); // 注销掉监听
            locationService.stop(); // 停止定位服务
        }
        setCancleReminder();
        if(wakeLock != null)
            wakeLock.release();
        Log.d(TAG, "onDestroy ");
    }

    public void setStationInfoList(List<StationInfo> list, List<StationInfo> needChangeStationList, Map<Integer, String> lineDirection) {
        if(this.list != null)
            this.list.clear();
        this.list = list;
        if(tempChangeStationList != null)
            tempChangeStationList.clear();
        this.needChangeStationList = needChangeStationList;
        tempChangeStationList = new ArrayList<>(needChangeStationList);
        if(tempChangeStationList.size() > 1) {
            currentStation = tempChangeStationList.get(0);
            nextStation = tempChangeStationList.get(1);
        }
        this.lineDirection = lineDirection;
    }

    public void setDirection(Map<Integer, String> lineDirection) {
        this.lineDirection = lineDirection;
    }

}