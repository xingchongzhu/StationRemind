package com.traffic.locationremind.baidu.location.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.traffic.locationremind.baidu.location.activity.MainActivity;
import com.traffic.locationremind.baidu.location.fragment.CityFragment;
import com.traffic.locationremind.baidu.location.fragment.FullMapFragment;
import com.traffic.locationremind.baidu.location.fragment.LineMapFragment;
import com.traffic.locationremind.baidu.location.fragment.RemindFragment;
import com.traffic.locationremind.baidu.location.listener.GoToFragmentListener;
import com.traffic.locationremind.baidu.location.listener.RemindSetViewListener;
import com.traffic.locationremind.baidu.location.pagerbottomtabstrip.NavigationController;
import com.traffic.locationremind.manager.RemindSetViewManager;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter{

    public static final int FULLMAPFRAGMENTINDEX = 0;
    public static final int LINEMAPFRAGMENTINDEX = 1;
    public static final int REMINDFRAGMENTINDEX = 2;
    NavigationController mNavigationController;
    private FragmentManager mFragmentManager;
    private List<Fragment> fragments = new ArrayList<>();
    public ViewPagerAdapter(MainActivity activity,FragmentManager mFragmentManager,NavigationController mNavigationController,
                            RemindSetViewListener mRemindSetViewManager){
        super(mFragmentManager);
        this.mFragmentManager = mFragmentManager;
        this.mNavigationController = mNavigationController;
        RemindFragment remindFragment = new RemindFragment();
        LineMapFragment lineMapFragment=  new LineMapFragment();
        FullMapFragment fullMapFragment = new FullMapFragment();
        fragments.add(fullMapFragment);
        fragments.add(lineMapFragment);
        fragments.add(remindFragment);
        activity.setLocationChangerListener(remindFragment);
        activity.addActivityListener(remindFragment);
        remindFragment.setRemindSetViewListener(mRemindSetViewManager);
    }

    public Fragment getFragment(int index){
        return fragments.get(index);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }


    @Override
    public int getCount() {
        if(mNavigationController != null)
            return mNavigationController.getItemCount();
        return 0;
    }
}
