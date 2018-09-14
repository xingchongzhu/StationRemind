package com.traffic.locationremind.baidu.location.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.*;
import com.baidu.mapapi.search.poi.*;
import com.baidu.mapapi.search.route.*;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.activity.MainActivity;
import com.traffic.locationremind.baidu.location.adapter.AllLineAdapter;
import com.traffic.locationremind.baidu.location.adapter.ColorLineAdapter;
import com.traffic.locationremind.baidu.location.dialog.SearchLoadingDialog;
import com.traffic.locationremind.baidu.location.dialog.SettingReminderDialog;
import com.traffic.locationremind.common.util.CharSort;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.common.util.MapComparator;
import com.traffic.locationremind.common.util.ReadExcelDataUtil;
import com.traffic.locationremind.manager.bean.ExitInfo;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataHelper;
import com.traffic.locationremind.manager.database.DataManager;

import java.util.*;

public class LineMapFragment extends Fragment implements ReadExcelDataUtil.DbWriteFinishListener, View.OnClickListener
             , OnGetBusLineSearchResultListener,OnGetPoiSearchResultListener,OnGetRoutePlanResultListener {

    private final static String TAG = "LineMapFragment";

    private GridView sceneMap;
    private GridView lineMap;
    private TextView currentLineInfoText;

    private AllLineAdapter sceneMapAdapter;
    private ColorLineAdapter colorLineAdapter;
    private View rootView;
    private DataManager mDataManager;
    private List<LineInfo> list = new ArrayList<>();
    MainActivity activity;
    StationInfo start,end;
    private SettingReminderDialog mSettingReminderDialog;
    private int currentIndex = 0;

    private PoiSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    private BusLineSearch mBusLineSearch = null;
    private List<String> busLineIDList = new ArrayList<>();
    private int busLineIndex = 0;
    private String currentLineName = "";

    RoutePlanSearch mRoutePlanSearch;

    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            setCurrentLine(msg.what);
        }

    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != rootView) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent) {
                parent.removeView(rootView);
            }
        } else {
            rootView = inflater.inflate(R.layout.line_map_layout, container, false);
            initView(rootView);// 控件初始化
        }
        if(savedInstanceState != null){
            upadaData();
            setCurrentLine(currentIndex);
        }
        Log.d(TAG, "onCreateView rootView = "+rootView+" savedInstanceState = "+savedInstanceState);
        return rootView;
    }

    private void initView(View rootView) {
        Log.d(TAG, "initView rootView = "+rootView);
        sceneMap = (GridView) rootView.findViewById(R.id.sceneMap);
        currentLineInfoText = (TextView) rootView.findViewById(R.id.text);
        Log.d(TAG, "initView currentLineInfoText = "+currentLineInfoText);
        lineMap = (GridView) rootView.findViewById(R.id.lineMap);
        //linenail = getResources().getString(R.string.line_tail);
        mDataManager = ((MainActivity) getActivity()).getDataManager();

        colorLineAdapter = new ColorLineAdapter(this.getActivity());
        lineMap.setAdapter(colorLineAdapter);
        sceneMapAdapter = new AllLineAdapter(this.getActivity(), mDataManager);
        sceneMap.setAdapter(sceneMapAdapter);

        sceneMap.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showDialog(sceneMapAdapter.getItem(position));
            }
        });
        activity = (MainActivity) getActivity();
        lineMap.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setCurrentLine(position);
            }
        });
        SDKInitializer.initialize(getContext().getApplicationContext());
        mSearch = PoiSearch.newInstance();
        mSearch.setOnGetPoiSearchResultListener(this);
        mBusLineSearch = BusLineSearch.newInstance();
        mBusLineSearch.setOnGetBusLineSearchResultListener(this);

        mRoutePlanSearch = RoutePlanSearch.newInstance();
        mRoutePlanSearch.setOnGetRoutePlanResultListener(this);

    }

    private void showDialog(final StationInfo stationInfo) {
        Log.d(TAG, "showDialog stationInfo.getCname() = " + stationInfo.getCname());
        List<ExitInfo> existInfoList = mDataManager.getDataHelper().QueryByExitInfoCname(stationInfo.getCname());
        Collections.sort(existInfoList, new MapComparator());
        String existInfostr = "";
        if (existInfoList != null) {
            final int existInfoList_size = existInfoList.size();// Moved  existInfoList.size() call out of the loop to local variable existInfoList_size
            for (int n = 0; n < existInfoList_size; n++) {
                existInfostr += existInfoList.get(n).getExitname() + " " + existInfoList.get(n).getAddr() + "\n";
            }
        }
        if (mSettingReminderDialog != null) {
            mSettingReminderDialog.dismiss();
            mSettingReminderDialog = null;
        } else {
        }
        mSettingReminderDialog = new SettingReminderDialog(getActivity(),
                R.style.Dialog, new SettingReminderDialog.NoticeDialogListener() {
            @Override
            public void onClick(View view) {
                try {
                    switch (view.getId()) {
                        case R.id.start:
                            start = stationInfo;
                            mSettingReminderDialog.dismiss();
                            break;
                        case R.id.end:
                            mSettingReminderDialog.dismiss();
                            end = stationInfo;
                            String startText = start==null ? LineMapFragment.this.getActivity().getResources().getString(R.string.current_location):
                                    start.getCname();
                            String endText = end==null?LineMapFragment.this.getActivity().getResources().getString(R.string.current_location):
                                    end.getCname();
                            ((MainActivity)LineMapFragment.this.getActivity()).searchStation(startText,endText);

                            /*LatLng startLat =new LatLng(CommonFuction.convertToDouble(start.getLat(), 0),
                                    CommonFuction.convertToDouble(start.getLot(), 0));
                            LatLng endLat =new LatLng(CommonFuction.convertToDouble(end.getLat(), 0),
                                    CommonFuction.convertToDouble(end.getLot(), 0));

                            PlanNode stNode = PlanNode.withCityNameAndPlaceName(mDataManager.getCurrentCityNo().getCityName(), start.getCname()+"地铁站");
                            PlanNode enNode = PlanNode.withCityNameAndPlaceName(mDataManager.getCurrentCityNo().getCityName(), end.getCname()+"地铁站");

                            PlanNode stNode = PlanNode.withLocation(startLat);
                            PlanNode enNode = PlanNode.withLocation(endLat);
                            Log.d(TAG,"startText = "+startText+" endText = "+endText);
                            mRoutePlanSearch.transitSearch(
                                    new TransitRoutePlanOption().city(mDataManager.getCurrentCityNo().getCityName()).from(stNode).to(enNode));*/
                            start = null;
                            end = null;
                            break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void selectLine(int lineid){
                setCurrentLineByLineid(lineid);
                mSettingReminderDialog.dismiss();
            }
        }, stationInfo.getTransfer(), existInfostr,
                "" + stationInfo.getLineid(), mDataManager.getCurrentCityNo().getCityName(), stationInfo.getCname());
        mSettingReminderDialog.setContentView(R.layout.setting_reminder_dialog);
        mSettingReminderDialog.show();
    }

    public void upadaData() {
        if (mDataManager != null && mDataManager.getLineInfoList() != null) {
            list.clear();
            for (Map.Entry<Integer, LineInfo> entry : mDataManager.getLineInfoList().entrySet()) {
                list.add(entry.getValue());
            }
        }

        Collections.sort(list, new Comparator<LineInfo>() {
            @Override
            public int compare(LineInfo o1, LineInfo o2) {
                if (o1.getLineid() < o1.getLineid()) {
                    return -1;
                } else if (o1.getLineid() == o1.getLineid()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        setCurrentLine(0);
        if (colorLineAdapter != null) {
            colorLineAdapter.setData(list);
        }
        if (mDataManager != null && mDataManager.getLineInfoList() != null) {
            if (mDataManager.getLineInfoList().size() < 6) {
                ViewGroup.LayoutParams linearParams = lineMap.getLayoutParams();
                linearParams.height = (int) getResources().getDimension(R.dimen.single_color_height);
                lineMap.setLayoutParams(linearParams);
            }else{
                ViewGroup.LayoutParams linearParams = lineMap.getLayoutParams();
                linearParams.height = (int) getResources().getDimension(R.dimen.line_color_height);
                lineMap.setLayoutParams(linearParams);
            }
        }
    }

    private void setCurrentLineByLineid(int lineid) {
        LineInfo lineInfo = mDataManager.getLineInfoList().get(lineid);
        if(lineInfo == null){
            return;
        }
        if (currentLineInfoText != null) {
            String string = lineInfo.linename + " (" +
                    lineInfo.getForwad() + "," +
                    lineInfo.getReverse() + ")\n" + lineInfo.getLineinfo();

            currentLineInfoText.setText(string);
            currentLineInfoText.setBackgroundColor(lineInfo.colorid);
        }
        if (sceneMap != null) {
            sceneMapAdapter.setData(lineInfo);
            int height = (int) activity.getResources().getDimension(R.dimen.count_line_node_rect_height) * (lineInfo.getStationInfoList().size() / 5 + 1);
            ViewGroup.LayoutParams linearParams = sceneMap.getLayoutParams();
            linearParams.height = height;
            sceneMap.setLayoutParams(linearParams); //使设置好的布局参数应用到控件
        }
        currentLineName = lineInfo.linename.split("/")[0];
        if(currentLineName.contains("号线")){
            currentLineName = currentLineName+"地铁";
        }
        searchInCity(currentLineName);
    }

    public void searchNextBusline(View v) {
        if (busLineIndex >= busLineIDList.size()) {
            busLineIndex = 0;
        }
        if (busLineIndex >= 0 && busLineIndex < busLineIDList.size()
                && busLineIDList.size() > 0) {
            mBusLineSearch.searchBusLine((new BusLineSearchOption()
                    .city(mDataManager.getCurrentCityNo().getCityName()).uid(busLineIDList.get(busLineIndex))));
            busLineIndex++;
        }

    }

    @Override
    public void onGetPoiResult(PoiResult result) {

        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Log.e(TAG,"onGetPoiResult 抱歉，未找到结果");
            return;
        }
        busLineIDList.clear();
        for (PoiInfo poi : result.getAllPoi()) {
            busLineIDList.add(poi.uid);//size=2，两个方向
        }
        searchNextBusline(null);
    }

    List<String> needUpdate = new ArrayList<>();
    @Override
    public void onGetBusLineResult(BusLineResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Log.e(TAG,"onGetBusLineResult 抱歉，未找到结果 result = "+result.error);
            if(currentLineName.contains("号线") && currentLineName.contains("地铁")){
                currentLineName.replace("地铁","");
                searchInCity("地铁" + currentLineName);
            }
            return;
        }
        Log.d(TAG,"onGetBusLineResult result.getBusLineName() = "+result.getBusLineName()+" direction "+result.getLineDirection()+ " time = "+result.getStartTime()+" | "+result.getEndTime());
        needUpdate.clear();
        if(mDataManager.getCurrentCityNo() != null && mDataManager.getCurrentCityNo().getCityName().equals("香港")) {
            int n = 1;
           /* for(BusLineResult.BusStation station:result.getStations()){
                StationInfo stationInfo = new StationInfo();
                stationInfo.setCname(station.getTitle());
                stationInfo.setLineid(list.get(currentIndex).lineid);
                stationInfo.setPm(n++);
                stationInfo.setLat(""+station.getLocation().latitude);
                stationInfo.setLot(""+station.getLocation().longitude);
                stationInfo.setStationInfo("——/——|——/——");
                stationInfo.setTransfer("0");
                boolean updateResult = mDataManager.getDataHelper().insetStationInfo(stationInfo);
                *//*if(!updateResult){
                    needUpdate.add(station.getTitle());
                }*//*
                Log.d(TAG,"getTitle = "+station.getTitle()+" updateResult = "+updateResult+" longitude = "+station.getLocation().longitude+" latitude = "+station.getLocation().latitude);
            }*/
        }else{
            for(BusLineResult.BusStation station:result.getStations()){
                /*int updateResult = mDataManager.getDataHelper().updateStation(station.getTitle(),""+station.getLocation().longitude,""+station.getLocation().latitude);
                if(updateResult <= 0){
                    needUpdate.add(station.getTitle());
                }*/
                Log.d(TAG,"getTitle = "+station.getTitle()+" longitude = "+station.getLocation().longitude+" latitude = "+station.getLocation().latitude);
            }
        }

        Log.d(TAG,"-----------------needupdate------------------");
        for(String string:needUpdate){
            Log.d(TAG,string);
        }

        /*for(BusLineResult.BusStep steps:result.getSteps()){
            //mDataManager.getDataHelper().updateStation(station.getTitle(),""+station.getLocation().longitude,""+station.getLocation().latitude);
            Log.d(TAG,"getName = "+steps.getName()+"      "+steps.getWayPoints().toString());
        }*/
    }

    /**
     * V5.2.0版本之后，还方法废弃，使用{@link #onGetPoiDetailResult(PoiDetailSearchResult)}
     * 代替
     */
    @Override
    public void onGetPoiDetailResult(PoiDetailResult result) {

    }

    @Override
    public void onGetPoiDetailResult(PoiDetailSearchResult result) {

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    private void searchInCity(String lineName){
        busLineIDList.clear();
        busLineIndex = 0;
        Log.d(TAG,"searchInCity city "+mDataManager.getCurrentCityNo().getCityName()+" linename = "+lineName);
        mSearch.searchInCity((new PoiCitySearchOption()).city(mDataManager.getCurrentCityNo().getCityName()).keyword(lineName));
    }

    private void setCurrentLine(int index) {
        currentIndex = index;
        if (currentLineInfoText != null) {
            if (index >= list.size()) {
                currentLineInfoText.setBackgroundColor(Color.WHITE);
                currentLineInfoText.setTextColor(Color.WHITE);
                currentLineInfoText.setText("");
                sceneMapAdapter.setData(null);
                return;
            }
            String string = list.get(index).linename + " (" + list.get(index).getForwad() + "," + list.get(index).getReverse() + ")\n" + list.get(index).getLineinfo();
            currentLineInfoText.setText(string);
            currentLineInfoText.setBackgroundColor(list.get(index).colorid);
        }
        if (sceneMap != null) {
            sceneMapAdapter.setData(mDataManager.getLineInfoList().get(list.get(index).lineid));
            int height = (int) activity.getResources().getDimension(R.dimen.count_line_node_rect_height) * (list.get(index).getStationInfoList().size() / 5 + 1);
            ViewGroup.LayoutParams linearParams = sceneMap.getLayoutParams();
            linearParams.height = height;
            sceneMap.setLayoutParams(linearParams); //使设置好的布局参数应用到控件
        }
        currentLineName = list.get(index).linename.split("/")[0];

        if(currentLineName.contains("号线")){
            currentLineName = currentLineName+"地铁";
        }
        searchInCity(currentLineName);


    }

    @Override
    public void onDestroy() {
        mSearch.destroy();
        mBusLineSearch.destroy();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

    }

    public void dbWriteFinishNotif() {

    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult var1){

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult result){
        if (result == null) {
            Log.d(TAG, "onGetTransitRouteResult result is empty");
            return;
        }
        Log.d(TAG, "onGetTransitRouteResult 公交换乘方案数：" + result.getRouteLines());
        if(result.getRouteLines() != null) {
            for (TransitRouteLine transitRouteLine : result.getRouteLines()) {
                Log.d(TAG, " title = " + transitRouteLine.getTitle());
                for (TransitRouteLine.TransitStep step : transitRouteLine.getAllStep()) {
                    Log.d(TAG, " name = " + step.getName() + " title " + step.getEntrance().getTitle());
                }
            }
        }
    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult var1){

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult var1){

    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult var1){

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult var1){

    }
}
