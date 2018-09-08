package com.traffic.locationremind.baidu.location.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.WindowManager;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.activity.MainActivity;
import com.traffic.locationremind.baidu.location.view.FullMapView;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.common.util.FileUtil;
import com.traffic.locationremind.manager.bean.CityInfo;
import com.traffic.locationremind.manager.database.DataManager;

public class FullMapFragment extends Fragment {
    private static final String TAG = "FullMapFragment";
    private View rootView;
    private FullMapView mFullMapView;
    private int screenWidth ,screenHeight;
    private DataManager mDataManager;

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
            WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            screenWidth = dm.widthPixels;         // 屏幕宽度（像素）
            screenHeight = dm.heightPixels-(int)getResources().getDimension(R.dimen.full_map_top_bottom_height);       // 屏幕高度（像素）
            initView(rootView);// 控件初始化
        }
        return rootView;
    }

    private void initView(View rootView){
        mFullMapView = (FullMapView)rootView.findViewById(R.id.map);
        /*Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.shanghai);
        mFullMapView.setBitmap(bitmap2,screenWidth,screenHeight);*/
        mDataManager = ((MainActivity) getActivity()).getDataManager();
        updateCity();
    }

    public void updateCity(){
        String shpno = CommonFuction.getSharedPreferencesValue(getContext(), CityInfo.CITYNAME);
        CityInfo cityInfo = mDataManager.getCityInfoList().get(shpno);
        if(cityInfo != null){
            int id = FileUtil.getResIconId(getContext(),cityInfo.getPingying());
            Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), id);
            mFullMapView.setBitmap(bitmap2,screenWidth,screenHeight);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    }
}
