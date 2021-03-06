package com.traffic.locationremind.baidu.location.activity;

import android.Manifest;
import android.annotation.TargetApi;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.SDKInitializer;
import com.traffic.location.remind.R;
import android.support.v4.app.Fragment;
import com.traffic.locationremind.baidu.location.adapter.ViewPagerAdapter;
import com.traffic.locationremind.baidu.location.dialog.AutoManagerHintDialog;
import com.traffic.locationremind.baidu.location.fragment.FullMapFragment;
import com.traffic.locationremind.baidu.location.fragment.LineMapFragment;
import com.traffic.locationremind.baidu.location.fragment.RemindFragment;
import com.traffic.locationremind.baidu.location.listener.ActivityListener;
import com.traffic.locationremind.baidu.location.listener.GoToFragmentListener;
import com.traffic.locationremind.baidu.location.listener.LoadDataListener;
import com.traffic.locationremind.baidu.location.listener.LocationChangerListener;
import com.traffic.locationremind.baidu.location.object.LineObject;
import com.traffic.locationremind.baidu.location.search.widge.SearchView;
import com.traffic.locationremind.baidu.location.pagerbottomtabstrip.NavigationController;
import com.traffic.locationremind.baidu.location.pagerbottomtabstrip.PageNavigationView;
import com.traffic.locationremind.baidu.location.service.LocationService;
import com.traffic.locationremind.baidu.location.service.RemonderLocationService;
import com.traffic.locationremind.baidu.location.utils.UWhiteListSettingUtils;
import com.traffic.locationremind.baidu.location.view.CustomViewPager;
import com.traffic.locationremind.manager.AsyncTaskManager;
import com.traffic.locationremind.common.util.*;
import com.traffic.locationremind.manager.RemindSetViewManager;
import com.traffic.locationremind.baidu.location.view.SearchEditView;
import com.traffic.locationremind.manager.bean.CityInfo;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataManager;
import com.traffic.locationremind.manager.SearchManager;
import com.traffic.locationremind.share.utils.Constants;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.PushAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCommonActivity implements View.OnClickListener,
        CopyDBDataUtil.DbWriteFinishListener, GoToFragmentListener, LoadDataListener {

    public final static int SELECTCITYREQUEST = 2018;
    public final static int SELECTCITYRESULTCODE = 2019;
    public final static int SHUTDOWNACTIVITY = 2020;
    private final static String TAG = "MainActivity";

    private RemonderLocationService.UpdateBinder mUpdateBinder;
    private RemonderLocationService mRemonderLocationService;

    private NavigationController mNavigationController;
    private SearchView searchView;
    private SearchManager mSearchManager;
    private ViewPagerAdapter mViewPagerAdapter;
    private DataManager mDataManager;
    private ViewGroup root;
    private ViewGroup serachLayoutRoot;
    private ImageButton searchBackButton;
    private TextView citySelect;
    private ViewGroup set_remind_layout;
    private SearchEditView editButton;
    private PageNavigationView pageBottomTabLayout;

    private RemindSetViewManager mRemindSetViewManager;

    private List<LocationChangerListener> locationChangerListenerList = new ArrayList<>();
    private List<ActivityListener> activityListenerList = new ArrayList<>();

    private CityInfo currentCityNo = null;
    //private Toolbar mToolbarSet;
    private ImageView colloction_btn;
    private String currentCity = "北京";
    private Handler mHandler = new Handler();
    public boolean hasLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 打开统计SDK调试模式
        UMConfigure.setLogEnabled(true);
        PushAgent.getInstance(this).onAppStart();
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_behavior);
        mDataManager = DataManager.getInstance(this);
        mDataManager.addLoadDataListener(this);
        initView();
        initData();
        Intent bindIntent = new Intent(this, RemonderLocationService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);
    }

    private void initView(){
        //mToolbarSet = (Toolbar)findViewById(R.id.toolbar);
        citySelect = (TextView) findViewById(R.id.city_select);
        editButton = (SearchEditView) findViewById(R.id.edit_button);

        pageBottomTabLayout = (PageNavigationView) findViewById(R.id.tab);

        mRemindSetViewManager = new RemindSetViewManager();
        mRemindSetViewManager.setGoToFragmentListener(this);
        mRemindSetViewManager.initView(this, mDataManager);

        mNavigationController = pageBottomTabLayout.material()
                .addItem(R.drawable.ic_passport, getString(R.string.full_subway_title))
                .addItem(R.drawable.ic_audio_order_pressed, getString(R.string.line_title))
                .addItem(R.drawable.personal_more_share, getString(R.string.remind_title))
                .build();
        root = (ViewGroup) findViewById(R.id.root);
        CustomViewPager viewPager = (CustomViewPager) findViewById(R.id.viewPager);
        mViewPagerAdapter = new ViewPagerAdapter(this, getSupportFragmentManager(),
                mNavigationController, mRemindSetViewManager);
        viewPager.setScanScroll(false);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(mViewPagerAdapter);

        mNavigationController.setupWithViewPager(viewPager);

        set_remind_layout = (ViewGroup) findViewById(R.id.set_remind_layout);

        serachLayoutRoot = (ViewGroup) findViewById(R.id.serach_layout_manager_root);
        searchBackButton = (ImageButton) findViewById(R.id.search_back);
        searchView = (SearchView) findViewById(R.id.search_root);
        searchBackButton.setOnClickListener(this);
        editButton.setOnClickListener(this);
        citySelect.setOnClickListener(this);

        mSearchManager = new SearchManager();
        mSearchManager.initViews(this, searchView);
        mSearchManager.initData(this, mDataManager);
        mSearchManager.setRemindSetViewListener(mRemindSetViewManager);
        currentCity = CommonFuction.getSharedPreferencesValue(this, CityInfo.CITYNAME);
    }

    private void setBackgroudRun(){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!CommonFuction.getSharedPreferencesBooleanValue(MainActivity.this, IDef.AUTO_MANAGER_FLAG)) {
                    CommonFuction.writeBooleanSharedPreferences(MainActivity.this, IDef.AUTO_MANAGER_FLAG, true);
                    UWhiteListSettingUtils.enterWhiteListSetting(MainActivity.this);
                }
            }
        }, 5000);
    }

    private AutoManagerHintDialog mAutoManagerHintDialog;
    public void showAutoManagerDialog(){
        if (mAutoManagerHintDialog == null) {
            mAutoManagerHintDialog = new AutoManagerHintDialog(MainActivity.this, R.style.Dialog);
        }
        mAutoManagerHintDialog.show();
    }
/*    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }*/

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // super.onSaveInstanceState(outState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        //  super.onSaveInstanceState(outState, outPersistentState);
    }

    public DataManager getDataManager() {
        return mDataManager;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent.getAction() != null && intent.getAction().equals(RemonderLocationService.CLOSE_REMINDER_SERVICE)) {
            RemindFragment remindFragment = (RemindFragment) mViewPagerAdapter.getFragment(ViewPagerAdapter.REMINDFRAGMENTINDEX);
            remindFragment.cancleRemind();
        }
        if (getRemindState()) {
            mNavigationController.setSelect(ViewPagerAdapter.REMINDFRAGMENTINDEX);
        }
        if (mRemonderLocationService != null) {
            mRemonderLocationService.moveInForeground();
        }
        //  MobclickAgent.onPageStart("MainActivity"); //手动统计页面("SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this); //统计时长
        /*if (mRemonderLocationService != null) {
            mRemonderLocationService.cancleNotification();
        }*/
    }

    public void onPause() {
        super.onPause();
        //MobclickAgent.onPageEnd("MainActivity"); //手动统计页面("SplashScreen"为页面名称，可自定义)，必须保证 onPageEnd 在 onPause 之前调用，因为SDK会在 onPause 中保存onPageEnd统计到的页面数据。
        MobclickAgent.onPause(this);
    }

    public void useForground() {
        NotificationUtil mNotificationUtils = new NotificationUtil(this);

        LocationService locationService = ((LocationApplication) getApplication()).locationService;
        locationService.getLocationClient().enableLocInForeground(1001, mNotificationUtils.arrivedNotification(this, 1));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (getRemindState()) {
            notificationMoveTaskToBack();
        }
    }

    private void initData() {
        /*if (ReadExcelDataUtil.getInstance().hasWrite) {
            mDataManager.loadData(this);
        }*/
        if (CopyDBDataUtil.getInstance().hasWrite) {
            mDataManager.loadData(this);
        }
        String shpno = CommonFuction.getSharedPreferencesValue(this, CityInfo.CITYNAME);

        if (TextUtils.isEmpty(shpno)) {
            shpno = IDef.DEFAULTCITY;
        }
        citySelect.setText(shpno);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_button:

                showSerachView();
                break;
            case R.id.search_back:
                hideSerachView();
                break;
            case R.id.city_select:
                Intent intent = new Intent(this, LocationCityActivity.class);
                this.startActivityForResult(intent, SELECTCITYREQUEST);
                overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit);
                break;
        }
    }

    public void searchStation(String start, String end) {
        showSerachView();
        mSearchManager.setSearchText(start, end);
    }

    public void showSerach(View view) {
        // 显示动画
        view.setVisibility(View.VISIBLE);
        TranslateAnimation mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                -1.0f, Animation.RELATIVE_TO_SELF, -0.0f);
        mShowAction.setRepeatMode(Animation.REVERSE);
        mShowAction.setDuration(500);
        view.startAnimation(mShowAction);//开始动画
        searchView.setStartCurrentLocation();
        mSearchManager.clearResult();
    }

    @Override
    public void openRemindFragment(final LineObject lineObject) {
        //List<Integer> tempList = new ArrayList<>();
        //StringBuffer str = new StringBuffer();
        /*for (StationInfo stationInfo : lineObject.stationList) {
            tempList.add(stationInfo.lineid);
        }*/
        mNavigationController.setSelect(ViewPagerAdapter.REMINDFRAGMENTINDEX);
        RemindFragment remindFragment = (RemindFragment) mViewPagerAdapter.getFragment(ViewPagerAdapter.REMINDFRAGMENTINDEX);
        //str.delete(0,str.length());
        /*for (int i = 0 ;i < tempList.size();i++) {
            list.get(i).lineid = tempList.get(i);
        }*/
        remindFragment.setData(lineObject);
        hideSerachView();
        hideSetRemindView();
    }

    public void setLocationChangerListener(LocationChangerListener locationChangerListener) {
        locationChangerListenerList.add(locationChangerListener);
    }

    private void hideSerach(View view) {
        // 隐藏动画
        view.setVisibility(View.GONE);
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                -1.0f);
        mHiddenAction.setDuration(500);
        view.startAnimation(mHiddenAction);//开始动画
    }


    public void showSerachView() {
        hideSerach(editButton);
        pageBottomTabLayout.setVisibility(View.GONE);
        showSerach(serachLayoutRoot);
    }

    public void hideSerachView() {
        hideSerach(serachLayoutRoot);
        showSerach(editButton);
        pageBottomTabLayout.setVisibility(View.VISIBLE);
    }

    public void hideSetRemindView() {
        if (mRemindSetViewManager.getRemindWindowState()) {
            mRemindSetViewManager.closeRemindWindow();
        }
    }

    @Override
    public void loadFinish() {
        Log.d(TAG, "loadFinish");
        mSearchManager.reloadData(this);
        LineMapFragment lineMapFragment = (LineMapFragment) mViewPagerAdapter.getFragment(ViewPagerAdapter.LINEMAPFRAGMENTINDEX);
        lineMapFragment.upadaData();

        RemindFragment remindFragment = (RemindFragment) mViewPagerAdapter.getFragment(ViewPagerAdapter.REMINDFRAGMENTINDEX);
        remindFragment.upadaData();

        FullMapFragment fullFragment = (FullMapFragment) mViewPagerAdapter.getFragment(ViewPagerAdapter.FULLMAPFRAGMENTINDEX);
        fullFragment.updateCity();
    }

    @Override
    public void updataFinish() {

    }

    @Override
    public void dbWriteFinishNotif() {
        initData();
        Log.d(TAG, "dbWriteFinishNotif ReadExcelDataUtil.getInstance().hasWrite = " + CopyDBDataUtil.getInstance().hasWrite);
    }

    private ServiceConnection connection = new ServiceConnection() {
        /**
         * 服务解除绑定时候调用
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
        }

        /**
         * 绑定服务的时候调用
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mUpdateBinder = (RemonderLocationService.UpdateBinder) service;
            mRemonderLocationService = mUpdateBinder.getService();
            if (mRemonderLocationService != null) {
                if (mRemonderLocationService != null) {
                    mRemonderLocationService.startLocationService();
                }
                mRemonderLocationService.setLocationChangerListener(new LocationChangerListener() {
                    @Override
                    public void loactionStation(BDLocation location) {
                        if (location.getCity() != null) {
                            String tempCity = location.getCity().substring(0, location.getCity().length() - 1);
                            CommonFuction.writeSharedPreferences(MainActivity.this, CityInfo.LOCATIONNAME, tempCity);
                            if (!hasLocation && FileUtil.dbIsExist(MainActivity.this, mDataManager.getCityInfoList().get(tempCity)) &&
                                    mDataManager.getCityInfoList() != null && mDataManager.getCityInfoList().get(tempCity) != null) {
                                setNewCity(tempCity);
                            }
                        }
                        for (LocationChangerListener locationChangerListener : locationChangerListenerList) {
                            locationChangerListener.loactionStation(location);
                        }
                    }

                    @Override
                    public void stopRemind() {
                        for (LocationChangerListener locationChangerListener : locationChangerListenerList) {
                            locationChangerListener.stopRemind();
                        }
                    }
                });
            }
        }
    };

    public void setRemindState(boolean state) {
        if (mRemonderLocationService != null) {
            mRemonderLocationService.setStartReminder(state);
        }
        if (mNavigationController != null && state) {
            mNavigationController.setHasMessage(2, true);
        } else if (mNavigationController != null) {
            mNavigationController.setHasMessage(2, false);
        }
    }

    public void setRemindList(List<StationInfo> list, List<StationInfo> needChangeStationList, Map<Integer, String> lineDirection) {
        if (mRemonderLocationService != null) {
            mRemonderLocationService.setStationInfoList(list, needChangeStationList, lineDirection);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == SELECTCITYRESULTCODE) {
            if (data != null && !TextUtils.isEmpty(data.getAction())) {
                String tempCity = data.getAction();
                if (FileUtil.dbIsExist(this, mDataManager.getCityInfoList().get(tempCity)) && !currentCity.equals(tempCity)) {
                    setNewCity(tempCity);
                }
            }
        }
        if (resultCode == SHUTDOWNACTIVITY) {
            finish();
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            SDKInitializer.initialize(getApplicationContext());
        }

        /*if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            FullMapFragment fullFragment = (FullMapFragment) mViewPagerAdapter.getFragment(ViewPagerAdapter.FULLMAPFRAGMENTINDEX);
            fullFragment.updateCity();
        }*/
        FullMapFragment fullFragment = (FullMapFragment) mViewPagerAdapter.getFragment(ViewPagerAdapter.FULLMAPFRAGMENTINDEX);
        fullFragment.updateCity();
        setBackgroudRun();
    }

    public void setFragment(int index) {
        mNavigationController.setSelect(index);
    }

    public Fragment getFragment(int index) {
        Fragment fragment = mViewPagerAdapter.getFragment(index);
        return fragment;
    }

    public String getCurrentCity() {
        return currentCity;
    }

    public void setNewCity(String city) {
        //city = "北京";
        Log.d(TAG, "setNewCity tempCity = " + city);
        citySelect.setText(city);
        currentCity = city;
        MobclickAgent.onEvent(getApplicationContext(), "城市", city);
        CommonFuction.writeSharedPreferences(MainActivity.this, CityInfo.CITYNAME, currentCity);
        mDataManager.loadData(MainActivity.this);
        hasLocation = true;
        FullMapFragment fullMapFragment = (FullMapFragment) mViewPagerAdapter.getFragment(ViewPagerAdapter.FULLMAPFRAGMENTINDEX);
        fullMapFragment.updateCity();

        //WebViewActivity.startActivity(MainActivity.this, "title", url);
    }

    public BDLocation getBDLocation() {
        BDLocation location = null;
        if (mRemonderLocationService != null) {
            location = mRemonderLocationService.getCurrentLocation();
        }
        return location;
    }

    public void addActivityListener(ActivityListener activityListener) {
        activityListenerList.add(activityListener);
    }


    public boolean notificationOnKeyDown(int keyCode, KeyEvent event) {
        boolean result = false;
        for (ActivityListener activityListener : activityListenerList) {
            if (activityListener.onKeyDown(keyCode, event)) {
                result = true;
            }
        }
        return result;
    }

    public void notificationMoveTaskToBack() {
        if (mRemonderLocationService != null) {
            mRemonderLocationService.moveTaskToBack();
        }
        for (ActivityListener activityListener : activityListenerList) {
            activityListener.moveTaskToBack();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AsyncTaskManager.getInstance().stopAllGeekRunable();
            if (mRemindSetViewManager.getRemindWindowState()) {
                hideSetRemindView();
                return true;
            }
            if (serachLayoutRoot.getVisibility() == View.VISIBLE) {
                hideSerachView();
                return true;
            }
        }
        if (notificationOnKeyDown(keyCode, event)) {
            return true;
        }
        if (getRemindState()) {
            //useForground();
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (serachLayoutRoot.getVisibility() == View.GONE && !mRemindSetViewManager.getRemindWindowState()) {
                    moveTaskToBack(true);
                    MobclickAgent.onEvent(getApplicationContext(), "移动到后台定位", "move background");
                    return true;
                }
            }

        }

        return super.onKeyDown(keyCode, event);
    }

    public boolean getRemindState() {
        RemindFragment remindFragment = (RemindFragment) mViewPagerAdapter.getFragment(ViewPagerAdapter.REMINDFRAGMENTINDEX);
        return remindFragment.getRemindState();
    }

    public StationInfo getLocationCurrentStation() {
        StationInfo nerstStationInfo = null;

        if (!MainActivity.this.getPersimmions()) {
            return nerstStationInfo;
        } else if (mRemonderLocationService != null) {
            mRemonderLocationService.startLocationService();
        }

        if (mRemonderLocationService != null) {
            BDLocation location = getBDLocation();
            if (location != null) {
                nerstStationInfo = PathSerachUtil.getNerastStation(location, mDataManager.getLineInfoList());

                if (nerstStationInfo != null) {
                    CommonFuction.writeSharedPreferences(MainActivity.this, CommonFuction.INITCURRENTLINEID, nerstStationInfo.getCname());
                }
                return nerstStationInfo;
            }
        }
        return nerstStationInfo;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        mDataManager.releaseResource();
        LocationService locationService = ((LocationApplication) getApplication()).locationService;
        locationService.getLocationClient().disableLocInForeground(true);

    }
}
