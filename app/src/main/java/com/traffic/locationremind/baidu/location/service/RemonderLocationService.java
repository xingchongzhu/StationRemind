package com.traffic.locationremind.baidu.location.service;

import android.app.Service;
import android.content.Intent;
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
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.common.util.NotificationUtil;
import com.traffic.locationremind.manager.bean.StationInfo;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author baidu
 */
public class RemonderLocationService extends Service {

    private final static String TAG = "RemonderLocationService";
    public final static String CLOSE_REMINDER_SERVICE = "close.reminder.service";
    private UpdateBinder downLoadBinder=new UpdateBinder();
    private LocationService locationService;
    /**
     * 回调
     */
    private Callback callback;
    private LocationChangerListener mLocationChangerListener;
    public static boolean state = false;
    /**
     * Timer实时更新数据的
     */
    private double longitude,latitude;

    BDLocation currentLocation;
    private List<MarkObject> mStationInfoList = null;//地图站台信息

    private NotificationUtil mNotificationUtil;
    private boolean isReminder = false;
    private boolean locationServiceHasStart = false;
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
        /*if(intent != null){
            Log.d(TAG,"onStartCommand intent.getAction() = "+intent.getAction());
            if(intent.getAction() != null && intent.getAction().equals(CLOSE_REMINDER_SERVICE)){
                setCancleReminder();
                stopSelf();
            }
        }*/
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 内部类继承Binder
     * @author lenovo
     *
     */
    public class UpdateBinder extends Binder {
        /**
         * 声明方法返回值是MyService本身
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

    public void setLocationChangerListener(LocationChangerListener locationChangerListener){
        this.mLocationChangerListener = locationChangerListener;
    }

    public void startLocationService(){
        if(locationService != null && !locationServiceHasStart){
            locationService.start();
            locationServiceHasStart = true;
        }
    }

    public BDLocation getCurrentLocation(){
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

            //double lot =0,lat = 0;
            if (null != location
                    && location.getLocType() != BDLocation.TypeServerError) {
                //StringBuffer sb = new StringBuffer(256);
                /**
                 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
                 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                 */

                if(!CommonFuction.isvalidLocation(location)){
                    Log.d(TAG,"BDAbstractLocationListener location invale ");
                    currentLocation = null;
                    return;
                }
               // Log.d(TAG,"BDAbstractLocationListener location = "+location+" callback");
                currentLocation = location;
                if(isReminder && callback != null){
                    callback.loactionStation(location);
                }
                if(mLocationChangerListener != null){
                    mLocationChangerListener.loactionStation(location);
                }
                //lot = location.getLongitude();
                //lat = location.getLatitude();
                //sb.append("\nlatitude : ");// 纬度
                //currentStationInfo.setLat(""+location.getLatitude());
                //sb.append(location.getLatitude());
                //sb.append("\nlontitude : ");// 经度
                //Log.d(TAG,"BDAbstractLocationListener lot ="+lot+" lat = "+lat);
                //currentStationInfo.setLot(""+location.getLongitude());
                //sb.append(location.getLongitude());
                //sb.append("\nradius : ");// 半径
                //sb.append(location.getRadius());
                //sb.append("\nCountryCode : ");// 国家码
                //sb.append(location.getCountryCode());
                //sb.append("\nCountry : ");// 国家名称
                //sb.append(location.getCountry());
                //sb.append("\ncitycode : ");// 城市编码
                //sb.append(location.getCityCode());
                //sb.append("\ncity : ");// 城市
                //currentStationInfo.setCityNo(""+location.getCityCode());
                //currentStationInfo.setCname(location.getCity());

                /*
                 * 得到最新数据
                 */
                //mStationInfo.setStationInfo(sb.toString());
                //callback.setCurrentStation(mStationInfo);//

                /*if(mStationInfoList != null && isReminder){
                    double dis = 0;
                    String startStation="",endStation="",currentStation="";
                    final int mStationInfoList_size = mStationInfoList.size();// Moved  mStationInfoList.size() call out of the loop to local variable mStationInfoList_size
                    for(int n = 0; n< mStationInfoList_size; n++){
                        longitude = CommonFuction.convertToDouble(mStationInfoList.get(n).mStationInfo.getLot(),0);
                        latitude =  CommonFuction.convertToDouble(mStationInfoList.get(n).mStationInfo.getLat(),0);
                        dis = CommonFuction.getDistanceLat(longitude,latitude,lot,lat);
                        if(mStationInfoList.get(n).isStartStation){
                            startStation = mStationInfoList.get(n).mStationInfo.getCname();
                        }
                        if(mStationInfoList.get(n).isEndStation){
                            endStation = mStationInfoList.get(n).mStationInfo.getCname();
                        }
                        if(mStationInfoList.get(n).isCurrentStation){
                            currentStation = mStationInfoList.get(n).mStationInfo.getCname();
                        }
                        callback.setCurrentStation(startStation,endStation,currentStation);
                        if(dis<= CommonFuction.RANDDIS){//抵达某站台
                            final int mStationInfoList_size1 = mStationInfoList.size();// Moved  mStationInfoList.size() call out of the loop to local variable mStationInfoList_size
                            for(int j = 0; j< mStationInfoList_size1; j++){
                                mStationInfoList.get(j).isCurrentStation = false;
                            }
                            mStationInfoList.get(n).isCurrentStation = true;
                            currentStation = mStationInfoList.get(n).mStationInfo.getCname();
                            callback.setCurrentStation(startStation,endStation,currentStation);
                            CommonFuction.writeSharedPreferences(RemonderLocationService.this,CommonFuction.CURRENTSTATIONNAME,mStationInfoList.get(n).mStationInfo.getCname());
                            if(mStationInfoList.get(n).isEndStation){//到站通知
                                if(callback != null){
                                    callback.arriaved(true);
                                    setCancleReminder();
                                }
                                break;
                            }
                            continue;
                        }
                    }
                }*/

            }
        }
    };

    /**
     * 提供接口回调方法
     * @param
     */

    public void setStartReminder(){
        isReminder = true;
        CommonFuction.writeBooleanSharedPreferences(this,CommonFuction.ISREMINDER,true);
    }
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setCancleReminder(){
        isReminder = false;
        CommonFuction.writeBooleanSharedPreferences(this,CommonFuction.ISREMINDER,false);
        mNotificationUtil.cancel(NotificationUtil.notificationId);
    }

    public void cancleNotification(){
        mNotificationUtil.cancel(NotificationUtil.notificationId);
    }

    public void setNotification(boolean state) {
        mNotificationUtil = new NotificationUtil(this);
        boolean ismark = CommonFuction.getSharedPreferencesBooleanValue(this,CommonFuction.ISREMINDER);
        Log.d(TAG,"isremark = "+ismark);
        if(ismark){
            NotificationObject mNotificationObject = new NotificationObject("1号地铁","老街","机场东","华强北","2分钟");
            mNotificationUtil.showNotification(getApplication(),NotificationUtil.notificationId,mNotificationObject);
        }
    }

    public void setEndStation(StationInfo mStationInfo){
        longitude = CommonFuction.convertToDouble(mStationInfo.getLot(),0);
        latitude = CommonFuction.convertToDouble(mStationInfo.getLat(),0);
    }

    /**
     * 回调接口
     *
     * @author lenovo
     *
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
        if(locationService != null){
            locationService.unregisterListener(mListener); // 注销掉监听
            locationService.stop(); // 停止定位服务
        }
        setCancleReminder();
        Log.d(TAG,"onDestroy ");
    }

    public void setStationInfoList(List<MarkObject> mStationInfoList){
        this.mStationInfoList = mStationInfoList;
        if(mStationInfoList != null && mStationInfoList.size() > 0){
            CommonFuction.writeSharedPreferences(this,CommonFuction.CURRENTLINEID,""+mStationInfoList.get(0).mStationInfo.getLineid());
            CommonFuction.writeSharedPreferences(this,CommonFuction.CURRENTCITYNO,""+mStationInfoList.get(0).mStationInfo.getCityNo());

            final int mStationInfoList_size = mStationInfoList.size();// Moved  mStationInfoList.size() call out of the loop to local variable mStationInfoList_size
            for(int n = 0; n< mStationInfoList_size; n++){
                if(mStationInfoList.get(n).isStartStation){
                    CommonFuction.writeSharedPreferences(this,CommonFuction.STARTSTATIONNAME,mStationInfoList.get(n).mStationInfo.getCname());
                    continue;
                }
                if(mStationInfoList.get(n).isEndStation){
                    CommonFuction.writeSharedPreferences(this,CommonFuction.ENDSTATIONNAME,mStationInfoList.get(n).mStationInfo.getCname());
                    continue;
                }
                if(mStationInfoList.get(n).isCurrentStation){
                    CommonFuction.writeSharedPreferences(this,CommonFuction.CURRENTSTATIONNAME,mStationInfoList.get(n).mStationInfo.getCname());
                    continue;
                }
            }
        }

    }
}