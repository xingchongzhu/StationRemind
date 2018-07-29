package com.traffic.locationremind.baidu.location.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.traffic.location.remind.R;

import com.traffic.locationremind.baidu.location.adapter.ViewPagerAdapter;
import com.traffic.locationremind.baidu.location.search.widge.SearchView;
import com.traffic.locationremind.baidu.location.pagerbottomtabstrip.NavigationController;
import com.traffic.locationremind.baidu.location.pagerbottomtabstrip.PageNavigationView;
import com.traffic.locationremind.baidu.location.service.RemonderLocationService;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.common.util.PathSerachUtil;
import com.traffic.locationremind.common.util.ReadExcelDataUtil;
import com.traffic.locationremind.common.util.ToastUitl;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataManager;
import com.traffic.locationremind.manager.serach.SearchManager;

import java.lang.reflect.Field;
import java.util.List;


public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private NavigationController mNavigationController;
    private SearchView searchView;
    private ListView listView;
    private SearchManager mSearchManager;
    private ViewPagerAdapter mViewPagerAdapter;
    private DataManager mDataManager;
    private ViewGroup root;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_behavior);
        setStatus();
        PageNavigationView pageBottomTabLayout = (PageNavigationView) findViewById(R.id.tab);

        mDataManager = DataManager.getInstance(this);
        mNavigationController = pageBottomTabLayout.material()
                .addItem(R.drawable.ic_restore_teal_24dp,getString(R.string.full_subway_title))
                .addItem(R.drawable.ic_favorite_teal_24dp,getString(R.string.line_title))
                .addItem(R.drawable.ic_nearby_teal_24dp,getString(R.string.remind_title))
                .build();

        root = (ViewGroup) findViewById(R.id.root);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        searchView = (SearchView) findViewById(R.id.search);
        listView = (ListView)findViewById(R.id.main_lv_search_results) ;
        searchView.setListView(listView);
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),mNavigationController);
        viewPager.setAdapter(mViewPagerAdapter);
        mSearchManager = new SearchManager();
        mSearchManager.initData(this);
        mSearchManager.initViews(this,listView,searchView);
        mNavigationController.setupWithViewPager(viewPager);


        Intent bindIntent = new Intent(this, RemonderLocationService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);

        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftInput( v);
                listView.setVisibility(View.GONE);
            }
        });
    }

    private void hideSoftInput(View v){
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void setStatus() {
        //隐藏标题栏
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.hide();
    }

    //arg0 ==1的时辰默示正在滑动，arg0==2的时辰默示滑动完毕了，arg0==0的时辰默示什么都没做。
    //当页面开始滑动的时候，三种状态的变化顺序为（1，2，0）
    public void onPageScrollStateChanged(int arg0) {
        //listView.bringToFront();
    }
    //当你滑动时一直调用这个方法直到停止滑到
    //arg0：表示现在的页面； arg1：表示当前页面偏移百分比； arg2：表示当前页面偏移的像素；
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }
    //此方法里的 arg0 是表示显示的第几页，当滑到第N页，就会调用此方法，arg0=N；
    public void onPageSelected(int arg0) {
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

        }
    };
}
