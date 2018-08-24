package com.traffic.locationremind.manager.serach;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.activity.MainActivity;
import com.traffic.locationremind.baidu.location.dialog.SearchLoadingDialog;
import com.traffic.locationremind.baidu.location.dialog.SettingReminderDialog;
import com.traffic.locationremind.baidu.location.listener.RemindSetViewListener;
import com.traffic.locationremind.baidu.location.listener.SearchResultListener;
import com.traffic.locationremind.baidu.location.object.LineObject;
import com.traffic.locationremind.baidu.location.search.adapter.CardAdapter;
import com.traffic.locationremind.baidu.location.search.adapter.GridViewAdapter;
import com.traffic.locationremind.baidu.location.search.adapter.SearchAdapter;
import com.traffic.locationremind.baidu.location.search.model.Bean;
import com.traffic.locationremind.baidu.location.search.widge.SearchView;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.common.util.PathSerachUtil;
import com.traffic.locationremind.common.util.ReadExcelDataUtil;
import com.traffic.locationremind.manager.bean.CityInfo;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataManager;

import java.lang.ref.WeakReference;
import java.util.*;

public class SearchManager implements SearchView.SearchViewListener ,SearchResultListener {

    private final static String TAG = "SearchManager";
    /**
     * 搜索结果列表view
     */
    private ListView lvResults;
    private ListView serachResults;
    private GridView recentSerachGrid;
    private SearchView searchView;
    private View loading;
    private RemindSetViewListener mRemindSetViewListener;

    GridViewAdapter mGridViewAdapter;

    CardAdapter mCardAdapter = null;
    int searchTaskNum = 0;
    int finishTaskNum = 0;
    List<Map.Entry<List<Integer>, List<StationInfo>>> lastLinesLast = new ArrayList<>();

    private MyHandler myHandler;
    private List<String> recentList = new ArrayList<>();
    /**
     * 自动补全列表adapter
     */
    private SearchAdapter autoCompleteAdapter;

    private Map<String, StationInfo> allstations = new HashMap<>();
    private DataManager mDataManager;
    private MainActivity activity;
    private SearchLoadingDialog mSearchLoadingDialog;
    public class MyHandler extends Handler {
        private WeakReference<MainActivity> mActivity;

        public MyHandler(WeakReference<MainActivity> weakReference) {
            mActivity = weakReference;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(lastLinesLast.size() <= 0){
                return;
            }
            sortStationNum(lastLinesLast);
            Map.Entry<List<Integer>, List<StationInfo>> first = lastLinesLast.get(0);
            //去除相同
            //换乘次数排序
            sortChangeTime(lastLinesLast);
            if(lastLinesLast.get(0) == first){
                lastLinesLast.clear();
                lastLinesLast.add(first);
                mCardAdapter.setData(lastLinesLast);
                return;
            }
            //去除相同
            List<Map.Entry<List<Integer>, List<StationInfo>>> add = new ArrayList<>();
            Map<Integer,Integer> array = new HashMap();
            for(Map.Entry<List<Integer>, List<StationInfo>>entry: lastLinesLast){
                boolean isEqual = false;
                for(Map.Entry<List<Integer>, List<StationInfo>>listEntry: add){
                    if(entry.getKey().toString().equals(listEntry.getKey().toString()) &&
                            entry.getValue().size() == listEntry.getValue().size()){
                        isEqual = true;
                    }
                }
                if(!isEqual)
                    add.add(entry);
                array.put(entry.getKey().size(),entry.getKey().size());
            }

            //换乘次数分类
            List<Integer> list = new ArrayList<>(array.keySet());
            Collections.sort(list, new Comparator<Integer>() {
                public int compare(Integer o1,Integer o2) {
                    if (o1< o2) {
                        return -1;
                    } else if (o1 == o2) {
                        return 0;
                    }
                    return 1;
                }
            });
            List<Map.Entry<List<Integer>, List<StationInfo>>> needadd = new ArrayList<>();
            List<Map.Entry<List<Integer>, List<StationInfo>>> templist = new ArrayList<>();
            if(lastLinesLast != null)
                lastLinesLast.clear();
            for(Integer size:list){
                needadd.clear();
                templist.clear();
                for(Map.Entry<List<Integer>, List<StationInfo>>entry: add){
                    if(size == entry.getKey().size()){
                        needadd.add(entry);
                    }
                }
                sortStationNum(needadd);
                if(needadd.size() >0){
                    int number = needadd.get(0).getValue().size();
                    for(Map.Entry<List<Integer>, List<StationInfo>>entry: needadd){
                        if(number == entry.getValue().size()){
                            templist.add(entry);
                        }
                    }
                    lastLinesLast.addAll(templist);
                }
            }
            sortChangeTime(lastLinesLast);
            mCardAdapter.setData(lastLinesLast);
        }
    }

    public void sortChangeTime(List<Map.Entry<List<Integer>, List<StationInfo>>> lastLinesLast){
        Collections.sort(lastLinesLast, new Comparator<Map.Entry<List<Integer>, List<StationInfo>>>() {
            public int compare(Map.Entry<List<Integer>, List<StationInfo>> o1,
                               Map.Entry<List<Integer>, List<StationInfo>> o2) {
                if (o1.getKey().size() < o2.getKey().size()) {
                    return -1;
                } else if (o1.getKey().size() == o2.getKey().size()) {
                    return 0;
                }
                return 1;
            }
        });
    }

    public void sortStationNum(List<Map.Entry<List<Integer>, List<StationInfo>>> lastLinesLast){

        Collections.sort(lastLinesLast, new Comparator<Map.Entry<List<Integer>, List<StationInfo>>>() {
            public int compare(Map.Entry<List<Integer>, List<StationInfo>> o1,
                               Map.Entry<List<Integer>, List<StationInfo>> o2) {
                if (o1.getValue().size() < o2.getValue().size()) {
                    return -1;
                } else if (o1.getValue().size() == o2.getValue().size()) {
                    return 0;
                }
                return 1;
            }
        });
    }

    /**
     * 搜索过程中自动补全数据
     */
    private List<StationInfo> autoCompleteData = new ArrayList<>();

    /**
     * 初始化视图
     */
    public void initViews(final Context context, final SearchView searchView) {
        activity = (MainActivity) context;
        myHandler = new MyHandler(new WeakReference<>(activity));
        this.searchView = searchView;
        ViewGroup serachLayoutManagerRoot = (ViewGroup)((MainActivity) context).findViewById(R.id.serach_layout_manager_root);
        serachResults = searchView.getResultListview();
        lvResults = searchView.getLvTips();
        recentSerachGrid = searchView.getRecentSerachGrid();
        autoCompleteAdapter = new SearchAdapter(context, autoCompleteData, R.layout.item_bean_list);
        loading = searchView.getLoading();
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
        mCardAdapter = new CardAdapter(activity, new ArrayList<Map.Entry<List<Integer>, List<StationInfo>>>(), R.layout.serach_result_item_layout);
        serachResults.setAdapter(mCardAdapter);
        getRecendData(context);
        mGridViewAdapter = new GridViewAdapter(context, recentList);
        recentSerachGrid.setAdapter(mGridViewAdapter);
        recentSerachGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchView.setRecentSelectStation(recentList.get(position));
            }
        });

        serachResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (mRemindSetViewListener != null) {
                    LineObject lineObject = new LineObject();
                    lineObject.lineidList = mCardAdapter.getItem(position).getKey();
                    lineObject.stationList = mCardAdapter.getItem(position).getValue();
                    mRemindSetViewListener.openSetWindow(lineObject);
                }
            }
        });

        serachLayoutManagerRoot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
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
        getAutoCompleteData(context, null);
    }

    public void reloadData(Context context){
        getDbData();
        getAutoCompleteData(context, null);
    }

    private void getRecendData(Context context) {
        recentList.clear();
        String string[] = CommonFuction.getRecentSearchHistory(context);
        if (string != null && string.length > 0) {
            for (int i = 0; i < string.length; i++) {
                if (!TextUtils.isEmpty(string[i]))
                    recentList.add(string[i]);
            }
        }
    }

    public void setRemindSetViewListener(RemindSetViewListener mRemindSetViewListener) {
        this.mRemindSetViewListener = mRemindSetViewListener;

    }

    @Override
    public void notificationRecentSerachChange(Context context) {
        getRecendData(context);
        mGridViewAdapter.notifyDataSetChanged();
        Log.d(TAG, "notificationRecentSerachChange");
    }

    /**
     * 获取db 数据
     */
    private void getDbData() {
        allstations = mDataManager.getAllstations();
    }

    /**
     * 获取自动补全data 和adapter
     */
    private void getAutoCompleteData(Context context, String text) {
        if (autoCompleteData == null) {
            //初始化
            autoCompleteData = new ArrayList<>();
        } else {
            // 根据text 获取auto data
            autoCompleteData.clear();

            for (Map.Entry<String, StationInfo> entry : allstations.entrySet()) {
                if (!TextUtils.isEmpty(text) && entry.getKey().contains(text.trim())) {
                    Log.d(TAG, "getAutoCompleteData serach result = " + entry.getValue().getCname());
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
     *
     * @param text
     */
    @Override
    public void onRefreshAutoComplete(Context context, String text) {
        //更新数据
        getAutoCompleteData(context, text);
    }

    StationInfo startStation = null, endStation = null;
    /**
     * 点击搜索键时edit text触发的回调
     */
    @Override
    public void onSearch(final Context context, String start, String end) {
        if(isSearch){
            return;
        }
        synchronized (lock) {
            String currentCity = CommonFuction.getSharedPreferencesValue(activity, CityInfo.CITYNAME);
            String locationCity = CommonFuction.getSharedPreferencesValue(activity, CityInfo.LOCATIONNAME);
            if (mSearchLoadingDialog != null) {
                mSearchLoadingDialog.dismiss();
            }
            //更新result数据
            if (TextUtils.isEmpty(start) || TextUtils.isEmpty(end))
                return;

            String current = context.getResources().getString(R.string.current_location);

            if ((start.equals(current) || end.equals(current)) && !currentCity.equals(locationCity)) {
                return;
            }
            if (start.equals(current)) {
                StationInfo stationInfo = activity.getLocationCurrentStation();
                if (stationInfo != null) {
                    startStation = stationInfo;
                }
            } else {
                startStation = allstations.get(start);
            }

            if (end.equals(current)) {
                StationInfo stationInfo = activity.getLocationCurrentStation();
                if (stationInfo != null) {
                    endStation = stationInfo;
                }
            } else {
                endStation = allstations.get(end);
            }
            if (startStation == null) {
                Toast.makeText(context, "请输入有效起点站名", Toast.LENGTH_SHORT).show();
                return;
            }
            if (endStation == null) {
                Toast.makeText(context, "请输入有效终点站名", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("zxc1","onSearch ");
            Log.d(TAG, "onSearch start = " + start + " end = " + end + " startStation.getCname() = " + startStation.getCname() + " endStation.getCname() = " + endStation.getCname());
            if (mSearchLoadingDialog == null) {
                mSearchLoadingDialog = new SearchLoadingDialog(activity, R.style.Dialog);
                mSearchLoadingDialog.setContentView(R.layout.search_loading);
            }
            mSearchLoadingDialog.show();
            lastLinesLast.clear();
            searchTaskNum = 0;
            finishTaskNum = 0;
            PathSerachUtil.getReminderLines(this, myHandler, startStation, endStation,
                    activity.getBDLocation(), mDataManager);
        }
    }

    private boolean isSearch = false;

    private Object lock = new Object();
    @Override
    public void setLineNumber(int number) {
        Log.d("zxc1","setLineNumber number = "+number);
        finishTaskNum = number;
        isSearch = true;
    }

    @Override
    public void updateResult(List<Map.Entry<List<Integer>, List<StationInfo>>> lines){
        //Log.d("zxc1","updateResult lastLinesLast = "+lastLinesLast);
        searchTaskNum ++;
        if(mSearchLoadingDialog != null && finishTaskNum <= searchTaskNum){
            mSearchLoadingDialog.dismiss();
        }
        lastLinesLast.addAll(lines);
        Log.d("zxc1","updateResult searchTaskNum = "+searchTaskNum+" lastLinesLast.size = "+lastLinesLast.size());
        if(finishTaskNum <= searchTaskNum){
            Message message = myHandler.obtainMessage();
            message.what = 0;
            myHandler.sendMessage(message);
            isSearch = false;
        }
    }
}
