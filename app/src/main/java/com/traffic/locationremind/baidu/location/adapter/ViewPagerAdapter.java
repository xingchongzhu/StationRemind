package com.traffic.locationremind.baidu.location.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.traffic.locationremind.baidu.location.activity.MainActivity;
import com.traffic.locationremind.baidu.location.fragment.CityFragment;
import com.traffic.locationremind.baidu.location.fragment.FullMapFragment;
import com.traffic.locationremind.baidu.location.fragment.LineMapFragment;
import com.traffic.locationremind.baidu.location.fragment.RemindFragment;
import com.traffic.locationremind.baidu.location.listener.GoToFragmentListener;
import com.traffic.locationremind.baidu.location.pagerbottomtabstrip.NavigationController;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter{

    public static final int FULLMAPFRAGMENTINDEX = 0;
    public static final int LINEMAPFRAGMENTINDEX = 1;
    public static final int REMINDFRAGMENTINDEX = 2;
    NavigationController mNavigationController;
    private FragmentManager mFragmentManager;
    private List<Fragment> fragments = new ArrayList<>();
    public ViewPagerAdapter(MainActivity activity,FragmentManager mFragmentManager,NavigationController mNavigationController){
        super(mFragmentManager);
        this.mFragmentManager = mFragmentManager;
        this.mNavigationController = mNavigationController;
        RemindFragment remindFragment = new RemindFragment();
        fragments.add(new FullMapFragment());
        fragments.add(new LineMapFragment());
        fragments.add(remindFragment);
        activity.setLocationChangerListener(remindFragment);
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
