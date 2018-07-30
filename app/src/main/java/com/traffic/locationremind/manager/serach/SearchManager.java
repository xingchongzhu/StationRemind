package com.traffic.locationremind.manager.serach;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.activity.MainActivity;
import com.traffic.locationremind.baidu.location.search.adapter.GridViewAdapter;
import com.traffic.locationremind.baidu.location.search.adapter.SearchAdapter;
import com.traffic.locationremind.baidu.location.search.model.Bean;
import com.traffic.locationremind.baidu.location.search.widge.SearchView;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.common.util.PathSerachUtil;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchManager implements SearchView.SearchViewListener{

    private final static String TAG = "SearchManager";
    /**
     * 搜索结果列表view
     */
    private ListView lvResults;
    private GridView recentSerachGrid;
    private SearchView searchView;

    GridViewAdapter mGridViewAdapter;

    private List<String> recentList = new ArrayList<>();
    /**
     * 自动补全列表adapter
     */
    private SearchAdapter autoCompleteAdapter;

    private Map<String,StationInfo> allstations = new HashMap<>();
    private DataManager mDataManager;
    private MainActivity activity;

    /**
     * 搜索过程中自动补全数据
     */
    private List<StationInfo> autoCompleteData = new ArrayList<>();

    /**
     * 初始化视图
     */
    public void initViews(final Context context, final SearchView searchView) {
        activity = (MainActivity)context;
        this.searchView = searchView;
        lvResults = searchView.getLvTips();
        recentSerachGrid = searchView.getRecentSerachGrid();
        autoCompleteAdapter = new SearchAdapter(context, autoCompleteData, R.layout.item_bean_list);
        //设置监听
        searchView.setSearchViewListener(this);
        //设置adapter
        searchView.setAutoCompleteAdapter(autoCompleteAdapter);
        lvResults.setAdapter(autoCompleteAdapter);
        lvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                searchView.setSelectStation(position);
            }
        });

        getRecendData(context);
        mGridViewAdapter = new GridViewAdapter(context,recentList);
        recentSerachGrid.setAdapter(mGridViewAdapter);
        recentSerachGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchView.setRecentSelectStation(recentList.get(position));
            }
        });
    }

    /**
     * 初始化数据
     */
    public void initData(Context context, DataManager dataManager) {
        this.mDataManager = dataManager;
        //从数据库获取数据
        getDbData();
        getAutoCompleteData(context,null);
    }

    private void getRecendData(Context context){
        recentList.clear();
       String string[] = CommonFuction.getRecentSearchHistory(context);
       if(string != null && string.length > 0){
           for(int i = 0;i<string.length;i++){
               if(!TextUtils.isEmpty(string[i]))
                    recentList.add(string[i]);
           }
       }
    }

    @Override
    public void notificationRecentSerachChange(Context context){
        getRecendData(context);
        mGridViewAdapter.notifyDataSetChanged();
        Log.d(TAG,"notificationRecentSerachChange");
    }

    /**
     * 获取db 数据
     */
    private void getDbData() {
        Map<Integer,LineInfo> allLines = mDataManager.getLineInfoList();
        Log.d(TAG,"getDbData"+" allLines = "+allLines);
        if(allLines != null){
            for(Map.Entry<Integer,LineInfo> entry:allLines.entrySet()){
                for(StationInfo stationInfo:entry.getValue().getStationInfoList()){
                    allstations.put(stationInfo.getCname(),stationInfo);
                }
            }
        }
    }

    /**
     * 获取自动补全data 和adapter
     */
    private void getAutoCompleteData(Context context,String text) {
        if (autoCompleteData == null) {
            //初始化
            autoCompleteData = new ArrayList<>();
        } else {
            // 根据text 获取auto data
            autoCompleteData.clear();

            for (Map.Entry<String,StationInfo> entry:allstations.entrySet()) {
                if (!TextUtils.isEmpty(text) && entry.getKey().contains(text.trim())) {
                    Log.d(TAG,"getAutoCompleteData serach result = "+entry.getValue().getCname());
                    autoCompleteData.add(entry.getValue());
                }
            }
        }
        if (autoCompleteAdapter == null) {
            autoCompleteAdapter = new SearchAdapter(context, autoCompleteData, R.layout.item_bean_list);
        } else {
            autoCompleteAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 当搜索框 文本改变时 触发的回调 ,更新自动补全数据
     * @param text
     */
    @Override
    public void onRefreshAutoComplete(Context context,String text) {
        //更新数据
        getAutoCompleteData(context,text);
    }

    /**
     * 点击搜索键时edit text触发的回调
     *
     */
    @Override
    public void onSearch(Context context,String start,String end) {
        //更新result数据
        //getAutoCompleteData(context,text);
        lvResults.setVisibility(View.GONE);
        StationInfo startStation = null,endStation = null;
        if(TextUtils.isEmpty(start) || start.equals(context.getResources().getString(R.string.current_location))){
            StationInfo stationInfo = activity.getLocationCurrentStation();
            if(stationInfo != null){
                startStation = stationInfo;
            }
        }else{
            startStation = allstations.get(start);
        }

        if(TextUtils.isEmpty(end) || end.equals(context.getResources().getString(R.string.current_location))){
            StationInfo stationInfo = activity.getLocationCurrentStation();
            if(stationInfo != null){
                endStation = stationInfo;
            }
        }else{
            endStation = allstations.get(end);;
        }
        if(startStation == null){
            Toast.makeText(context, "请输入有效起点站名", Toast.LENGTH_SHORT).show();
            return;
        }
        if(endStation == null){
            Toast.makeText(context, "请输入有效终点站名", Toast.LENGTH_SHORT).show();
            return;
        }
        if(startStation.getCname().equals(endStation.getCname())){
            Toast.makeText(context, "起始站和终点站不能相同", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG,"onSearch start = "+start+" end = "+end);
        List<Map.Entry<List<Integer>,List<StationInfo>>>  lastLinesLast = PathSerachUtil.getReminderLines(startStation,endStation,
                mDataManager.getMaxLineid(),activity.getBDLocation(),mDataManager.getLineInfoList(),mDataManager.getAllLineCane());
        PathSerachUtil.printAllRecomindLine(lastLinesLast);
    }

}
