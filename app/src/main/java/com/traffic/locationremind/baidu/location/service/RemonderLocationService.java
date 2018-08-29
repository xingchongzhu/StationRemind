package com.traffic.locationremind.baidu.location.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import android.widget.Toast;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.activity.LocationApplication;
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

import java.util.*;

/**
 * @author baidu
 */
public class RemonderLocationService extends Service {

    private final static String TAG = "RemonderLocationService";
    public final static String CLOSE_REMINDER_SERVICE = "close.reminder.service";
    private UpdateBinder downLoadBinder = new UpdateBinder();
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
    StationInfo nextStation, currentStation;
    private Map<Integer, String> lineDirection;
    private List<StationInfo> needChangeStationList;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        System.out.println("=====onBind=====");

        return downLoadBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        this.callback = null;
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 内部类继承Binder
     *
     * @author lenovo
     */
    public class UpdateBinder extends Binder {
        /**
         * 声明方法返回值是MyService本身
         *
         * @return
         */
        public RemonderLocationService getService() {
            return RemonderLocationService.this;
        }
    }

    /**
     * 服务创建的时候调用
     */
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        // -----------location config ------------
        locationService = ((LocationApplication) getApplication()).locationService;
        // 获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService.registerListener(mListener);
        /*
         * 执行Timer 2000毫秒后执行，5000毫秒执行一次
         */
        mNotificationUtil = new NotificationUtil(this);

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
                if (isReminder && callback != null) {
                    callback.loactionStation(location);
                }
                if (mLocationChangerListener != null) {
                    mLocationChangerListener.loactionStation(location);
                }

                if (list != null && list.size() > 0) {
                    StationInfo nerstStationInfo = PathSerachUtil.getNerastNextStation(location, list);
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

                        Log.d(TAG, "loactionStation getCname = " + currentStation.getCname() + " isRemind = " + isReminder);
                        if (isReminder) {
                            for (StationInfo stationInfo : tempChangeStationList) {
                                if (list.get(list.size() - 1).getCname().equals(stationInfo.getCname()) &&
                                        stationInfo.getCname().equals(currentStation.getCname())) {
                                    Log.d(TAG, "arrive stationInfo.getCname()" + stationInfo.getCname());
                                    tempChangeStationList.remove(stationInfo);
                                    isReminder = false;
                                    cancleNotification();
                                    NotificationUtils.sendHint(getApplicationContext(), true, getResources().getString(R.string.arrive), getResources().getString(R.string.hint_arrive_end_station), "");
                                    break;
                                } else if (stationInfo.getCname().equals(currentStation.getCname())) {//换乘点
                                    tempChangeStationList.remove(stationInfo);
                                    String str = String.format(getResources().getString(R.string.change_station_hint), stationInfo.getCname()) +
                                            DataManager.getInstance(getApplicationContext()).getLineInfoList().get(currentStation.lineid).linename;
                                    NotificationUtils.sendHint(getApplicationContext(), false, getResources().getString(R.string.change), str,
                                            lineDirection.get(stationInfo.lineid) + getResources().getString(R.string.direction));
                                    Log.d(TAG, "change stationInfo.getCname()" + stationInfo.getCname());
                                    break;
                                }
                            }
                            updataNotification(NotificationUtils.createNotificationObject(getApplicationContext(), lineDirection, currentStation, nextStation));
                        }
                    }

                }
            }
        }

    };

    public boolean moveTaskToBack() {
        Log.d(TAG, "moveTaskToBack isRemind = " + isReminder);
        if (isReminder) {
            NotificationObject mNotificationObject = NotificationUtils.createNotificationObject(getApplicationContext(), lineDirection, currentStation, nextStation);
            setNotification(mNotificationObject);
        }
        return true;
    }

    public void setNotification(NotificationObject mNotificationObject) {
        mNotificationUtil.showNotification(getApplicationContext(), NotificationUtil.notificationId, mNotificationObject);
    }

    public void updataNotification(NotificationObject mNotificationObject) {
        mNotificationUtil.updateProgress(getApplicationContext(), NotificationUtil.notificationId, mNotificationObject);
    }


    /**
     * 提供接口回调方法
     *
     * @param
     */

    public void setStartReminder(Boolean state) {
        isReminder = state;
        if (!isReminder) {
            cancleNotification();
        }else if(needChangeStationList != null){
            tempChangeStationList = new ArrayList<>(needChangeStationList);
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setCancleReminder() {
        isReminder = false;
        mNotificationUtil.cancel(NotificationUtil.notificationId);
    }

    public void cancleNotification() {
        mNotificationUtil.cancel(NotificationUtil.notificationId);
    }

    /**
     * 回调接口
     *
     * @author lenovo
     */
    public static interface Callback {
        /**
         * 得到实时更新的数据
         *
         * @return
         */
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
        // TODO Auto-generated method stub
        System.out.println("=========onDestroy======");
        /**
         * 停止Timer
         */
        if (locationService != null) {
            locationService.unregisterListener(mListener); // 注销掉监听
            locationService.stop(); // 停止定位服务
        }
        setCancleReminder();
        Log.d(TAG, "onDestroy ");
    }

    public void setStationInfoList(List<StationInfo> list, List<StationInfo> needChangeStationList,Map<Integer, String> lineDirection) {
        this.list = list;
        tempChangeStationList = new ArrayList<>(needChangeStationList);
        this.lineDirection = lineDirection;
    }

    public void setDirection(Map<Integer, String> lineDirection) {
        this.lineDirection = lineDirection;
    }
}