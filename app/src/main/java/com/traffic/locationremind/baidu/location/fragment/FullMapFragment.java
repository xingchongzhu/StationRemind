package com.traffic.locationremind.baidu.location.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.activity.MainActivity;

public class FullMapFragment extends Fragment {
    private static final String TAG = "FullMapFragment";
    private View rootView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView");
        if (null != rootView) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent) {
                parent.removeView(rootView);
            }
        } else {
            rootView = inflater.inflate(R.layout.full_map_layout,container,false);
            initView(rootView);// 控件初始化
        }
        return rootView;
    }
    private void initView(View rootView){

    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    }
}
