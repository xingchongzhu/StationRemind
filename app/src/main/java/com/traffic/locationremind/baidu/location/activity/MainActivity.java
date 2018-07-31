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

import com.baidu.location.BDLocation;
import com.traffic.location.remind.R;

import com.traffic.locationremind.baidu.location.adapter.ViewPagerAdapter;
import com.traffic.locationremind.baidu.location.search.widge.SearchView;
import com.traffic.locationremind.baidu.location.pagerbottomtabstrip.NavigationController;
import com.traffic.locationremind.baidu.location.pagerbottomtabstrip.PageNavigationView;
import com.traffic.locationremind.baidu.location.service.RemonderLocationService;
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

import java.util.Map;


public class MainActivity extends AppCommonActivity implements View.OnClickListener,
        ReadExcelDataUtil.DbWriteFinishListener{

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

    private CityInfo currentCityNo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_behavior);
        setStatusBar(Color.WHITE);
        pageBottomTabLayout = (PageNavigationView) findViewById(R.id.tab);

        mRemindSetViewManager = new  RemindSetViewManager();
        mRemindSetViewManager.initView(this);

        mDataManager = DataManager.getInstance(this);
        ReadExcelDataUtil.getInstance().addDbWriteFinishListener(this);

        mNavigationController = pageBottomTabLayout.material()
                .addItem(R.drawable.all_icon,getString(R.string.full_subway_title))
                .addItem(R.drawable.line_icon,getString(R.string.line_title))
                .addItem(R.drawable.remind_icon,getString(R.string.remind_title))
                .build();

        root = (ViewGroup) findViewById(R.id.root);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),mNavigationController);
        viewPager.setAdapter(mViewPagerAdapter);
        mNavigationController.setupWithViewPager(viewPager);


        citySelect = (TextView) findViewById(R.id.city_select);
        editButton = (SearchEditView)findViewById(R.id.edit_button);
        serachLayoutRoot = (ViewGroup) findViewById(R.id.serach_layout_root);
        searchBackButton = (ImageButton) findViewById(R.id.search_back);
        searchView = (SearchView) findViewById(R.id.search_root);
        searchBackButton.setOnClickListener(this);
        editButton.setOnClickListener(this);

        mSearchManager = new SearchManager();
        mSearchManager.initViews(this,searchView);
        mSearchManager.setRemindSetViewListener(mRemindSetViewManager);
        if (ReadExcelDataUtil.getInstance().hasWrite) {
            initData();
        }

        Intent bindIntent = new Intent(this, RemonderLocationService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initData(){
        mSearchManager.initData(this,mDataManager);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
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

    private void showSerach(View view){
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

    private void hideSerach(View view){
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
        if(actionBar != null)
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

    public void showSerachView(){
        hideSerach(editButton);
        pageBottomTabLayout.setVisibility(View.GONE);
        showSerach(serachLayoutRoot);
    }

    public void hideSerachView(){
        hideSerach(serachLayoutRoot);
        showSerach(editButton);
        pageBottomTabLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void dbWriteFinishNotif() {
        initData();
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
            }

        }
    };

    public BDLocation getBDLocation(){
        BDLocation location = null;
        if (mRemonderLocationService != null) {
            location = mRemonderLocationService.getCurrentLocation();
        }
        return location;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mRemindSetViewManager.getRemindWindowState()){
                mRemindSetViewManager.closeRemindWindow();
                return true;
            }
            if(serachLayoutRoot.getVisibility() == View.VISIBLE){
                hideSerachView();
            }else{
                moveTaskToBack(true);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
                Log.d(TAG, "locationCurrentStation location.getCityCode() = " + location.getCityCode() + " lot = " + location.getLatitude() + " lat = " + location.getLongitude());

                currentCityNo = mDataManager.getCityInfoList().get(location.getCityCode());
                Log.d(TAG, "locationCurrentStation currentCityInfo CityName = " + currentCityNo.getCityName());
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
                    Log.d(TAG, "locationCurrentStation mLineInfoList = " + tempList.size());
                    nerstStationInfo = PathSerachUtil.getNerastStation(location, tempList);
                }

                //Log.d(TAG,"locationCurrentStation currentStationInfo = "+nerstStationInfo);
                if (nerstStationInfo != null) {
                    CommonFuction.writeSharedPreferences(MainActivity.this, CommonFuction.INITCURRENTLINEID, nerstStationInfo.getCname());
                    Log.d(TAG, "locationCurrentStation lineid = " + nerstStationInfo.lineid + " name = " + nerstStationInfo.getCname());
                }
                return nerstStationInfo;
            }
        }
        return nerstStationInfo;
    }

}
