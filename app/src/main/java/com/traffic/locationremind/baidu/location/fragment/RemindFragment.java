package com.traffic.locationremind.baidu.location.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.location.BDLocation;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.activity.MainActivity;
import com.traffic.locationremind.baidu.location.activity.MainViewActivity;
import com.traffic.locationremind.baidu.location.listener.GoToFragmentListener;
import com.traffic.locationremind.baidu.location.listener.LocationChangerListener;
import com.traffic.locationremind.baidu.location.object.MarkObject;
import com.traffic.locationremind.baidu.location.view.LineNodeView;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.common.util.PathSerachUtil;
import com.traffic.locationremind.common.util.ToastUitl;
import com.traffic.locationremind.manager.bean.CityInfo;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemindFragment extends Fragment implements LocationChangerListener ,View.OnClickListener{

    private String TAG = "RemindFragment";
    private LinearLayout linearlayout;
    private HorizontalScrollView horizontalScrollView;
    private List<StationInfo> list = null;
    private Map<String,LineNodeView> textViewList = new HashMap<>();
    private int lineNodeWidth = 0;
    private int lineNodeHeight = 0;
    private DataManager mDataManager;

    private View rootView;
    private Bitmap transFerBitmap = null;
    private LineNodeView currentStationView;

    private TextView start_and_end;
    private TextView line_change_introduce;
    private TextView line_color_view;
    private TextView current_info_text;
    private TextView collection_btn;
    private TextView favtor_btn;
    private TextView cancle_remind_btn;

    //起点，当前站，换乘点，终点
    private List<StationInfo> needChangeStationList = new ArrayList<>();

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
            rootView = inflater.inflate(R.layout.remind_layout,container,false);
            initView(rootView);// 控件初始化
        }
        return rootView;
    }

    public void setData(List<StationInfo> list){
        this.list = list;
        for(StationInfo stationInfo:list){
            stationInfo.colorId = mDataManager.getLineInfoList().get(stationInfo.lineid).colorid;
        }
        createLine(list);
    }

    public void createLine(List<StationInfo> list){
        if(list == null || list.size() <= 0){
            return;
        }
        needChangeStationList.clear();
        linearlayout.removeAllViews();
        int chageNum = 0;
        StationInfo preStationInfo = null;
        for (int i = 0; i < list.size(); i++) {
            StationInfo stationInfo = list.get(i);
            //创建textview
            LineNodeView textView = new LineNodeView(getActivity());
            textView.setId(i+1000);

            textView.setStation(stationInfo);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(lineNodeWidth, lineNodeHeight);
            linearlayout.addView(textView,layoutParams);
            //添加到集合
            textViewList.put(stationInfo.getCname(),textView);
            if(i == 0){//起点
                int size = (int)getResources().getDimension(R.dimen.current_bitmap_siez);
                Bitmap bitmap = CommonFuction.getbitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.cm_main_map_pin_start), size,size);
                textView.setBitMap(bitmap);
            }
            //换乘点
            if(preStationInfo != null && preStationInfo.lineid != stationInfo.lineid) {
                needChangeStationList.add(stationInfo);
                int size = (int)getResources().getDimension(R.dimen.transfer_bitmap_size);
                Bitmap bitmap = CommonFuction.getbitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.cm_route_map_pin_dottransfer), size,size);
                textView.setTransFerBitmap(bitmap);
                chageNum++;
            }
            //终点
            if(i == list.size() - 1){//起点
                int size = (int)getResources().getDimension(R.dimen.current_bitmap_siez);
                Bitmap bitmap = CommonFuction.getbitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.cm_main_map_pin_end), size,size);
                textView.setBitMap(bitmap);
            }
            preStationInfo = stationInfo;
        }
        String start = list.get(0).getCname()+" -> "+list.get(list.size() -1).getCname();
        start_and_end.setText(start);

        String stationNum = String.format(getResources().getString(R.string.station_number),list.size()+"");
        String changeNumstr = String.format(getResources().getString(R.string.change_number),chageNum+"");
        line_change_introduce.setText(changeNumstr+"  "+stationNum);

        String surplusNum = String.format(getResources().getString(R.string.surples_station),list.size()+"");
        String currenStr = getResources().getString(R.string.current_station)+list.get(0).getCname();
        current_info_text.setText(surplusNum+"    "+currenStr);
    }

    private void initView(View rootView){
        linearlayout = (LinearLayout) rootView.findViewById(R.id.linearlayout);
        horizontalScrollView = (HorizontalScrollView) rootView.findViewById(R.id.horizontalScrollView);

        start_and_end = (TextView)rootView.findViewById(R.id.start_and_end);
        line_change_introduce = (TextView)rootView.findViewById(R.id.line_change_introduce);
        line_color_view = (TextView)rootView.findViewById(R.id.line_color_view);
        current_info_text = (TextView)rootView.findViewById(R.id.current_info_text);
        collection_btn = (TextView)rootView.findViewById(R.id.collection_btn);
        favtor_btn  = (TextView)rootView.findViewById(R.id.favtor_btn);
        cancle_remind_btn  = (TextView)rootView.findViewById(R.id.cancle_remind_btn);

        mDataManager = ((MainActivity)getActivity()).getDataManager();
        lineNodeWidth = (int)getResources().getDimension(R.dimen.line_node_width);
        lineNodeHeight = (int)getResources().getDimension(R.dimen.line_node_height);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.collection_btn:
                break;
            case R.id.favtor_btn:
                break;
            case R.id.cancle_remind_btn:
                break;
        }
    }

    @Override
    public void loactionStation(BDLocation location) {
        MainActivity activity = (MainActivity)getActivity();
        if (activity == null || !activity.getPersimmions()) {
            return;
        } else {
            CityInfo currentCityNo = mDataManager.getCityInfoList().get(location.getCityCode());
            StationInfo nerstStationInfo = PathSerachUtil.getNerastStation(location,mDataManager.getLineInfoList());
            if(currentStationView != null){
                currentStationView.setBitMap(null);
            }
            if(nerstStationInfo != null){
                LineNodeView lineNodeView = textViewList.get(nerstStationInfo.getCname());
                if(lineNodeView != null){
                    int size = (int)getResources().getDimension(R.dimen.current_bitmap_siez);
                    Bitmap bitmap = CommonFuction.getbitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.cm_main_map_pin_location), size,size);
                    lineNodeView.setBitMap(bitmap);
                    currentStationView = lineNodeView;
                    int n = 0;
                    for(StationInfo stationInfo:list){
                        if(stationInfo.getCname().equals(lineNodeView.getStationInfo().getCname())){
                            break;
                        }
                        n++;
                    }
                    String surplusNum = String.format(getResources().getString(R.string.surples_station),(list.size()-n)+"");
                    String currenStr = getResources().getString(R.string.current_station)+lineNodeView.getStationInfo().getCname();
                    current_info_text.setText(surplusNum+"    "+currenStr);
                }
            }
        }
    }
}
