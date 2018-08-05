package com.traffic.locationremind.baidu.location.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.ColorInt;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.TextView;

import android.widget.Toast;
import com.baidu.location.BDLocation;
import com.traffic.location.remind.R;

import com.traffic.locationremind.baidu.location.adapter.ViewPagerAdapter;
import com.traffic.locationremind.baidu.location.fragment.LineMapFragment;
import com.traffic.locationremind.baidu.location.fragment.RemindFragment;
import com.traffic.locationremind.baidu.location.listener.ActivityListener;
import com.traffic.locationremind.baidu.location.listener.GoToFragmentListener;
import com.traffic.locationremind.baidu.location.listener.LoadDataListener;
import com.traffic.locationremind.baidu.location.listener.LocationChangerListener;
import com.traffic.locationremind.baidu.location.search.widge.SearchView;
import com.traffic.locationremind.baidu.location.pagerbottomtabstrip.NavigationController;
import com.traffic.locationremind.baidu.location.pagerbottomtabstrip.PageNavigationView;
import com.traffic.locationremind.baidu.location.service.RemonderLocationService;
import com.traffic.locationremind.common.util.ToastUitl;
import com.traffic.locationremind.manager.RemindSetViewManager;
import com.traffic.locationremind.baidu.location.view.SearchEditView;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.common.util.PathSerachUtil;
import com.traffic.locationremind.common.util.ReadExcelDataUtil;
import com.traffic.locationremind.manager.bean.CityInfo;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataManager;
import com.traffic.locationremind.manager.serach.SearchManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCommonActivity implements View.OnClickListener,
        ReadExcelDataUtil.DbWriteFinishListener, GoToFragmentListener, LoadDataListener {

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
    private SearchEditView editButton;
    private PageNavigationView pageBottomTabLayout;

    private RemindSetViewManager mRemindSetViewManager;

    private List<LocationChangerListener> locationChangerListenerList = new ArrayList<>();
    private List<ActivityListener> activityListenerList = new ArrayList<>();

    private CityInfo currentCityNo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_behavior);
        mDataManager = DataManager.getInstance(this);
        mDataManager.addLoadDataListener(this);
        setStatusBar(Color.WHITE);
        pageBottomTabLayout = (PageNavigationView) findViewById(R.id.tab);

        mRemindSetViewManager = new RemindSetViewManager();
        mRemindSetViewManager.setGoToFragmentListener(this);
        mRemindSetViewManager.initView(this, mDataManager);

        ReadExcelDataUtil.getInstance().addDbWriteFinishListener(this);

        mNavigationController = pageBottomTabLayout.material()
                .addItem(R.drawable.all_icon, getString(R.string.full_subway_title))
                .addItem(R.drawable.line_icon, getString(R.string.line_title))
                .addItem(R.drawable.remind_icon, getString(R.string.remind_title))
                .build();

        root = (ViewGroup) findViewById(R.id.root);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPagerAdapter = new ViewPagerAdapter(this, getSupportFragmentManager(), mNavigationController);
        viewPager.setAdapter(mViewPagerAdapter);

        mNavigationController.setupWithViewPager(viewPager);

        citySelect = (TextView) findViewById(R.id.city_select);
        editButton = (SearchEditView) findViewById(R.id.edit_button);
        serachLayoutRoot = (ViewGroup) findViewById(R.id.serach_layout_manager_root);
        searchBackButton = (ImageButton) findViewById(R.id.search_back);
        searchView = (SearchView) findViewById(R.id.search_root);
        searchBackButton.setOnClickListener(this);
        editButton.setOnClickListener(this);

        mSearchManager = new SearchManager();
        mSearchManager.initViews(this, searchView);
        mSearchManager.initData(this, mDataManager);
        mSearchManager.setRemindSetViewListener(mRemindSetViewManager);
        if (ReadExcelDataUtil.getInstance().hasWrite) {
            initData();
        }

        Intent bindIntent = new Intent(this, RemonderLocationService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);
    }

    public DataManager getDataManager() {
        return mDataManager;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if(intent.getAction() != null && intent.getAction().equals(RemonderLocationService.CLOSE_REMINDER_SERVICE)){
            RemindFragment remindFragment = (RemindFragment) mViewPagerAdapter.getFragment(ViewPagerAdapter.REMINDFRAGMENTINDEX);
            remindFragment.cancleRemind();
        }
    }

    private void initData() {
        if (ReadExcelDataUtil.getInstance().hasWrite) {
            mDataManager.loadData(this);
        }
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
                break;
        }
    }

    private void showSerach(View view) {
        // 显示动画
        view.setVisibility(View.VISIBLE);
        TranslateAnimation mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                -1.0f, Animation.RELATIVE_TO_SELF, -0.0f);
        mShowAction.setRepeatMode(Animation.REVERSE);
        mShowAction.setDuration(500);
        view.startAnimation(mShowAction);//开始动画
        searchView.setStartCurrentLocation();
    }

    @Override
    public void openRemindFragment(List<StationInfo> list) {
        mNavigationController.setSelect(ViewPagerAdapter.REMINDFRAGMENTINDEX);
        RemindFragment remindFragment = (RemindFragment) mViewPagerAdapter.getFragment(ViewPagerAdapter.REMINDFRAGMENTINDEX);
        remindFragment.setData(list);
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

    /**
     * Android 6.0 以上设置状态栏颜色
     */
    protected void setStatusBar(@ColorInt int color) {
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
        LineMapFragment remindFragment = (LineMapFragment) mViewPagerAdapter.getFragment(ViewPagerAdapter.LINEMAPFRAGMENTINDEX);
        remindFragment.upadaData();
    }

    @Override
    public void updataFinish() {

    }

    @Override
    public void dbWriteFinishNotif() {
        initData();
        Log.d(TAG, "dbWriteFinishNotif ReadExcelDataUtil.getInstance().hasWrite = " + ReadExcelDataUtil.getInstance().hasWrite);
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
                        Log.d(TAG,"setLocationChangerListener");
                        for (LocationChangerListener locationChangerListener : locationChangerListenerList) {
                            locationChangerListener.loactionStation(location);
                        }
                    }
                });
            }

        }
    };

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

    public void notificationOnKeyDown(int keyCode, KeyEvent event) {
        for (ActivityListener activityListener : activityListenerList) {
            activityListener.onKeyDown(keyCode, event);
        }
    }

    public void notificationMoveTaskToBack() {
        for (ActivityListener activityListener : activityListenerList) {
            activityListener.moveTaskToBack();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (getRemindState()) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (mRemindSetViewManager.getRemindWindowState()) {
                    hideSetRemindView();
                    return true;
                }
                if (serachLayoutRoot.getVisibility() == View.VISIBLE) {
                    hideSerachView();
                } else {
                    moveTaskToBack(true);
                    notificationMoveTaskToBack();
                }
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_HOME) {
                notificationMoveTaskToBack();
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
                // Log.d(TAG, "locationCurrentStation location.getCityCode() = " + location.getCityCode() + " lot = " + location.getLatitude() + " lat = " + location.getLongitude());

                currentCityNo = mDataManager.getCityInfoList().get(location.getCityCode());
                // Log.d(TAG, "locationCurrentStation currentCityInfo CityName = " + currentCityNo.getCityName());
                if (currentCityNo != null) {
                    String shpno = CommonFuction.getSharedPreferencesValue(MainActivity.this, CommonFuction.CITYNO);
                    Map<Integer, LineInfo> tempList = mDataManager.getLineInfoList();
                    if (!shpno.equals("" + currentCityNo)) {
                        CommonFuction.writeSharedPreferences(MainActivity.this, CommonFuction.CITYNO, "" + currentCityNo);
                        tempList = mDataManager.getDataHelper().getLineList(currentCityNo.getCityNo(), LineInfo.LINEID, "ASC");
                        if (tempList != null) {
                            for (Map.Entry<Integer, LineInfo> entry : tempList.entrySet()) {
                                entry.getValue().setStationInfoList(mDataManager.getDataHelper().QueryByStationLineNo(entry.getKey(), currentCityNo.getCityNo()));
                            }
                        }
                    }
                    //Log.d(TAG, "locationCurrentStation mLineInfoList = " + tempList.size());
                    nerstStationInfo = PathSerachUtil.getNerastStation(location, tempList);
                }

                //Log.d(TAG,"locationCurrentStation currentStationInfo = "+nerstStationInfo);
                if (nerstStationInfo != null) {
                    CommonFuction.writeSharedPreferences(MainActivity.this, CommonFuction.INITCURRENTLINEID, nerstStationInfo.getCname());
                    //Log.d(TAG, "locationCurrentStation lineid = " + nerstStationInfo.lineid + " name = " + nerstStationInfo.getCname());
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
    }
}
