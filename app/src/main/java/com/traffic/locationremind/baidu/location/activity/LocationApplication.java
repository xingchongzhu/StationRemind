package com.traffic.locationremind.baidu.location.activity;


import android.app.ActivityManager;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;

import com.baidu.mapapi.SDKInitializer;
import com.squareup.leakcanary.LeakCanary;
import com.traffic.locationremind.baidu.location.service.LocationService;
import com.traffic.locationremind.baidu.location.service.RemonderLocationService;
import com.traffic.locationremind.common.util.CopyDBDataUtil;
import com.traffic.locationremind.common.util.ReadExcelDataUtil;
import com.traffic.locationremind.manager.database.DataHelper;
import com.traffic.locationremind.manager.database.SqliteHelper;

import java.util.List;

/**
 * 主Application，所有百度定位SDK的接口说明请参考线上文档：http://developer.baidu.com/map/loc_refer/index.html
 * <p>
 * 百度定位SDK官方网站：http://developer.baidu.com/map/index.php?title=android-locsdk
 * <p>
 * 直接拷贝com.baidu.location.service包到自己的工程下，简单配置即可获取定位结果，也可以根据demo内容自行封装
 */
public class LocationApplication extends Application {
    public LocationService locationService;
    public Vibrator mVibrator;
    //private DataHelper mDataHelper;
    //private ReadExcelDataUtil mReadExcelDataUtil;
    private CopyDBDataUtil mCopyDBDataUtil;
    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        /***
         * 初始化定位sdk，建议在Application中创建
         */
        //mDataHelper = DataHelper.getInstance(this);
        locationService = new LocationService(getApplicationContext());
        mVibrator = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        //SDKInitializer.initialize(getApplicationContext());
        String processName = getProcessName(this);
        if (processName!= null) {
            if(processName.equals("com.traffic.location.remind")){
                //mReadExcelDataUtil = ReadExcelDataUtil.getInstance();
               // mReadExcelDataUtil.execute(this);
                //mCopyDBDataUtil = CopyDBDataUtil.getInstance();
                //mCopyDBDataUtil.execute(this);
            }
        }

/*        Intent startIntent = new Intent(this, RemonderLocationService.class);
        startService(startIntent);*/

    }

    private String getProcessName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo proInfo : runningApps) {
            if (proInfo.pid == android.os.Process.myPid()) {
                if (proInfo.processName != null) {
                    return proInfo.processName;
                }
            }
        }
        return null;
    }

   /* public DataHelper getDataHelper(){
        return mDataHelper;
    }

    public void onDestory(){
        if(mDataHelper != null){
            mDataHelper.Close();
            Intent stopIntent = new Intent(this, RemonderLocationService.class);
            stopService(stopIntent);
        }
    }*/
}
