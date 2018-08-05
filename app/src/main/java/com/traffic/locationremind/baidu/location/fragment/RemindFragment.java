package com.traffic.locationremind.baidu.location.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.*;
import com.baidu.location.BDLocation;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.activity.MainActivity;
import com.traffic.locationremind.baidu.location.activity.MainViewActivity;
import com.traffic.locationremind.baidu.location.listener.ActivityListener;
import com.traffic.locationremind.baidu.location.listener.GoToFragmentListener;
import com.traffic.locationremind.baidu.location.listener.LocationChangerListener;
import com.traffic.locationremind.baidu.location.object.MarkObject;
import com.traffic.locationremind.baidu.location.object.NotificationObject;
import com.traffic.locationremind.baidu.location.view.LineNodeView;
import com.traffic.locationremind.baidu.location.view.RemindLineColorView;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.common.util.NotificationUtil;
import com.traffic.locationremind.common.util.PathSerachUtil;
import com.traffic.locationremind.common.util.ToastUitl;
import com.traffic.locationremind.manager.bean.CityInfo;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemindFragment extends Fragment implements LocationChangerListener ,View.OnClickListener,ActivityListener {

    private String TAG = "RemindFragment";
    private LinearLayout linearlayout;
    private HorizontalScrollView horizontalScrollView;
    private List<StationInfo> list = null;
    private Map<String,LineNodeView> textViewList = new HashMap<>();
    private Map<Integer,String > lineDirection = new HashMap<>();
    private int lineNodeWidth = 0;
    private int lineNodeHeight = 0;
    private DataManager mDataManager;

    private View rootView;
    private LineNodeView currentStationView;

    private TextView  hint_text;
    private TextView start_and_end;
    private TextView line_change_introduce;
    private RemindLineColorView line_color_view;
    private TextView current_info_text;
    private TextView collection_btn;
    private TextView favtor_btn;
    private TextView cancle_remind_btn;
    private RelativeLayout remind_root;

    private NotificationUtil mNotificationUtil;
    private StationInfo currentStation,nextStation;

    private boolean isRemind = false;
    private boolean isPause = false;
    private MainActivity activity;

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
        isRemind = true;
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
        remind_root.setVisibility(View.VISIBLE);
        hint_text.setVisibility(View.GONE);
        needChangeStationList.clear();
        linearlayout.removeAllViews();
        Map<Integer,LineInfo> lineInfoMap = new HashMap<>();
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
                currentStation = list.get(0);
                int size = (int)getResources().getDimension(R.dimen.current_bitmap_siez);
                Bitmap bitmap = CommonFuction.getbitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.cm_main_map_pin_start), size,size);
                textView.setBitMap(bitmap);
            }else{
                nextStation = list.get(1);
            }
            //换乘点
            if(preStationInfo != null && preStationInfo.lineid != stationInfo.lineid) {
                needChangeStationList.add(stationInfo);
                int size = (int)getResources().getDimension(R.dimen.transfer_bitmap_size);
                Bitmap bitmap = CommonFuction.getbitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.cm_route_map_pin_dottransfer), size,size);
                textView.setTransFerBitmap(bitmap);
            }else if(preStationInfo != null && preStationInfo.lineid == stationInfo.lineid) {
                if(preStationInfo.pm < stationInfo.pm){
                    lineDirection.put(preStationInfo.lineid,mDataManager.getLineInfoList().get(preStationInfo.lineid).getForwad());
                }else{
                    lineDirection.put(preStationInfo.lineid,mDataManager.getLineInfoList().get(preStationInfo.lineid).getReverse());
                }

            }
            //终点
            if(i == list.size() - 1){
                int size = (int)getResources().getDimension(R.dimen.current_bitmap_siez);
                Bitmap bitmap = CommonFuction.getbitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.cm_main_map_pin_end), size,size);
                textView.setBitMap(bitmap);
                needChangeStationList.add(stationInfo);
            }
            preStationInfo = stationInfo;
            int lineid = list.get(i).lineid;
            lineInfoMap.put(lineid,mDataManager.getLineInfoList().get(lineid));
        }
        String start = list.get(0).getCname()+" -> "+list.get(list.size() -1).getCname();
        start_and_end.setText(start);

        String stationNum = String.format(getResources().getString(R.string.station_number),list.size()+"");
        String changeNumstr = String.format(getResources().getString(R.string.change_number),lineInfoMap.size()+"");
        line_change_introduce.setText(changeNumstr+"  "+stationNum);

        String surplusNum = String.format(getResources().getString(R.string.surples_station),list.size()+"");
        String currenStr = getResources().getString(R.string.current_station)+list.get(0).getCname();
        current_info_text.setText(surplusNum+"    "+currenStr);
        line_color_view.setLineInfoMap(lineInfoMap);
        cancle_remind_btn.setText(getResources().getString(R.string.cancle_remind));
    }

    private void initView(View rootView){
        linearlayout = (LinearLayout) rootView.findViewById(R.id.linearlayout);
        horizontalScrollView = (HorizontalScrollView) rootView.findViewById(R.id.horizontalScrollView);

        remind_root = (RelativeLayout)rootView.findViewById(R.id.remind_root);
        hint_text = (TextView)rootView.findViewById(R.id.hint_text);
        start_and_end = (TextView)rootView.findViewById(R.id.start_and_end);
        line_change_introduce = (TextView)rootView.findViewById(R.id.line_change_introduce);
        line_color_view = (RemindLineColorView)rootView.findViewById(R.id.line_color_view);
        current_info_text = (TextView)rootView.findViewById(R.id.current_info_text);
        collection_btn = (TextView)rootView.findViewById(R.id.collection_btn);
        favtor_btn  = (TextView)rootView.findViewById(R.id.favtor_btn);
        cancle_remind_btn  = (TextView)rootView.findViewById(R.id.cancle_remind_btn);

        mDataManager = ((MainActivity)getActivity()).getDataManager();
        lineNodeWidth = (int)getResources().getDimension(R.dimen.line_node_width);
        lineNodeHeight = (int)getResources().getDimension(R.dimen.line_node_height);
        mNotificationUtil = new NotificationUtil(getActivity());
        activity = (MainActivity)getActivity();

        hint_text.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.collection_btn:
                break;
            case R.id.favtor_btn:
                break;
            case R.id.hint_text:
                activity.showSerachView();
                break;
            case R.id.cancle_remind_btn:
                if(isRemind){
                    isRemind = false;
                    cancleNotification();
                }else{
                    isRemind = true;
                    cancle_remind_btn.setText(getResources().getString(R.string.cancle_remind));
                }
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

            StationInfo nerstStationInfo = PathSerachUtil.getNerastNextStation(location,mDataManager.getLineInfoList());
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
                    horizontalScrollView.scrollTo( lineNodeWidth*n,horizontalScrollView.getScrollY());
                    String surplusNum = String.format(getResources().getString(R.string.surples_station),(list.size()-n)+"");
                    String currenStr = getResources().getString(R.string.current_station)+lineNodeView.getStationInfo().getCname();
                    current_info_text.setText(surplusNum+"    "+currenStr);

                    currentStation = lineNodeView.getStationInfo();
                    if(n+1 < list.size()-1){
                        nextStation = list.get(n+1);
                    }

                    n = 0;
                    size = needChangeStationList.size();
                    Log.d(TAG,"loactionStation getCname = "+nerstStationInfo.getCname());
                    if(isRemind) {
                        for (StationInfo stationInfo : needChangeStationList) {
                            if (stationInfo.getCname().equals(nerstStationInfo.getCname())) {
                                if (size - 1 == n) {//终点站
                                    isRemind = false;
                                    cancleNotification();
                                    arriveNotification();
                                    break;
                                } else {//换乘点
                                    needChangeStationList.remove(stationInfo);
                                    String str = String.format(getResources().getString(R.string.change_station_hint),stationInfo.getCname(),stationInfo.lineid+"")+
                                            "   "+lineDirection.get(stationInfo.lineid)+getResources().getString(R.string.direction);
                                    changeNotification(str);
                                    break;
                                }
                            }
                            n++;
                        }
                        Log.d(TAG,"loactionStation updataNotification ");
                        updataNotification(createNotificationObject(currentStation, nextStation));
                    }

                }
            }
        }
    }

    public NotificationObject createNotificationObject(StationInfo currentStation,StationInfo nextStation){
        if(currentStation == null){
            return null;
        }
        String linename = "";
        String currentStationName = "";
        String direction = "";
        String nextStationName = "";
        String time = "2分钟";
        if(currentStation != null){
            linename = String.format(getResources().getString(R.string.line_tail),currentStation.lineid+"");
            currentStationName = currentStation.getCname();
            direction = lineDirection.get(currentStation.lineid);
        }
        if(nextStation == null){
            nextStationName = currentStation.getCname();
        }
        NotificationObject mNotificationObject = new NotificationObject(linename,currentStationName,direction,nextStationName,time);
        return mNotificationObject;
    }

    @Override
    public void onResume() {
        cancleNotification();
        super.onResume();
        this.isPause = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        this.isPause = true;
    }

    public void cancleRemind(){
        isRemind = false;
        cancleNotification();
    }
    public boolean getRemindState(){
        return isRemind;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return true;
    }

    @Override
    public boolean moveTaskToBack(){
        Log.d(TAG,"moveTaskToBack isRemind = "+isRemind);
        if(isRemind){
            NotificationObject mNotificationObject = createNotificationObject(currentStation,nextStation);
            setNotification(mNotificationObject);
        }
        return true;
    }

    public void cancleNotification(){
        mNotificationUtil.cancel(NotificationUtil.notificationId);
        cancle_remind_btn.setText(getResources().getString(R.string.startlocation));
    }

    public void setNotification(NotificationObject mNotificationObject) {
        mNotificationUtil.showNotification(getActivity(),NotificationUtil.notificationId,mNotificationObject);
    }

    public void updataNotification(NotificationObject mNotificationObject) {
        mNotificationUtil.updateProgress(getActivity(),NotificationUtil.notificationId,mNotificationObject);
    }

    public void arriveNotification() {
        mNotificationUtil.arrivedNotification(getActivity(),NotificationUtil.arriveNotificationId);
    }

    public void changeNotification(String str) {
        mNotificationUtil.changeNotification(getActivity(),NotificationUtil.changeNotificationId,str);
    }

}
