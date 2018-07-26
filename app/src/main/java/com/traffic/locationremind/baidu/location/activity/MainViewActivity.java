package com.traffic.locationremind.baidu.location.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

import com.baidu.location.BDLocation;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.object.MarkObject;
import com.traffic.locationremind.baidu.location.object.MarkObject.MarkClickListener;
import com.traffic.locationremind.baidu.location.service.RemonderLocationService;
import com.traffic.locationremind.baidu.location.view.GifView;
import com.traffic.locationremind.baidu.location.view.LineMap;
import com.traffic.locationremind.baidu.location.view.LineMapColor;
import com.traffic.locationremind.baidu.location.view.LineMapColorView;
import com.traffic.locationremind.baidu.location.view.LineMapView;
import com.traffic.locationremind.baidu.location.view.SelectLineDialog;
import com.traffic.locationremind.baidu.location.view.SettingReminderDialog;
import com.traffic.locationremind.baidu.location.view.SettingReminderDialog.NoticeDialogListener;
import com.traffic.locationremind.common.util.*;
import com.traffic.locationremind.manager.bean.CityInfo;
import com.traffic.locationremind.manager.bean.ExitInfo;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainViewActivity extends CommonActivity implements ReadExcelDataUtil.DbWriteFinishListener, View.OnClickListener {

    private final static String TAG = "MainViewActivity";
    private LineMapView sceneMap;
    private LineMapColorView lineMap;
    private GifView gif;
    private ImageView scaleMorebtn;
    private ImageView scaleLessbtn;
    private ImageView button_location;
    private LinearLayout btnLinearLayout;
    private Button screenbtn;
    private Button city_select;
    private Button start_location_reminder;
    private TextView currentLineInfoText;
    private TextView hintText;

    private int currentIndex = 1;//当前线路
    private DataHelper mDataHelper;//数据库
    private CityInfo currentCityNo = null;
    private Map<String,CityInfo> cityInfoList;//所有城市信息
    private Map<Integer,LineInfo> mLineInfoList;//地图线路
    private List<StationInfo> mStationInfoList ;//地图站台信息

    private initDataThread minitDataThread;
    private int extraRow = 1;

    private RemonderLocationService.UpdateBinder mUpdateBinder;
    private RemonderLocationService mRemonderLocationService;
    public int canSetReminder = 0;

    SelectLineDialog mSelectLineDialog;
    SettingReminderDialog mSettingReminderDialog;
    StationInfo startStationInfo, currentStationInfo, endStationInfo,nerstStationInfo;

    private List<StationInfo> currentAllStationList = new ArrayList<StationInfo>();//正在导航线路

    private Map<String, StationInfo> allTransferList;//所有可以换乘站台
    private Map<Integer, Map<Integer,Integer>> allLineCane = new HashMap<Integer, Map<Integer,Integer>>();//(1,(2,3,4,5)),(2,(3,4,6,8)),(3,(3,4,6,8))
    private int maxLineid = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if (mLineInfoList != null && mLineInfoList.size() > 0) {
                        //线路颜色-------------------------------------
                        initLineColorMap();
                        mStationInfoList = mLineInfoList.get(currentIndex).getStationInfoList();
                        currentLineInfoText.setText(CommonFuction.getSubwayShowText(MainViewActivity.this, mDataHelper, mLineInfoList.get(currentIndex)));
                        currentLineInfoText.setBackgroundColor(mLineInfoList.get(currentIndex).colorid);
                        int countRow = mStationInfoList.size() / LineMap.ROWMAXCOUNT + 1;
                        Bitmap bitmap = Bitmap.createBitmap((int) sceneMap.getViewWidth(), MarkObject.ROWHEIGHT * countRow,
                                Bitmap.Config.ARGB_8888);
                        bitmap.eraseColor(MainViewActivity.this.getResources().getColor(R.color.white));//填充颜色
                        sceneMap.setBitmap(bitmap);
                        sceneMap.postInvalidate();
                        //----------------------------------------
                        gif.setVisibility(View.GONE);
                        setViewVisible(View.VISIBLE);
                        hintText.setVisibility(View.GONE);
                    }
                    break;
                case 2:
                    mStationInfoList = mLineInfoList.get(currentIndex).getStationInfoList();
                    currentLineInfoText.setText(CommonFuction.getSubwayShowText(MainViewActivity.this, mDataHelper, mLineInfoList.get(currentIndex)));
                    currentLineInfoText.setBackgroundColor(mLineInfoList.get(currentIndex).colorid);
                    int countRow = mStationInfoList.size() / LineMap.ROWMAXCOUNT + 1;
                    Bitmap bitmap = Bitmap.createBitmap((int) sceneMap.getViewWidth(), MarkObject.ROWHEIGHT * countRow,
                            Bitmap.Config.ARGB_8888);
                    bitmap.eraseColor(MainViewActivity.this.getResources().getColor(R.color.white));//填充颜色
                    sceneMap.setBitmap(bitmap);
                    sceneMap.postInvalidate();
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_view);
        setStatus();
        sceneMap = (LineMapView) findViewById(R.id.sceneMap);
        currentLineInfoText = (TextView) findViewById(R.id.text);
        lineMap = (LineMapColorView) findViewById(R.id.lineMap);
        scaleMorebtn = (ImageView) findViewById(R.id.button_in);
        scaleLessbtn = (ImageView) findViewById(R.id.button_out);
        button_location = (ImageView) findViewById(R.id.button_location);

        screenbtn = (Button) findViewById(R.id.full_screen);
        start_location_reminder = (Button) findViewById(R.id.start_location_reminder);
        city_select = (Button) findViewById(R.id.city_select);
        gif = (GifView) findViewById(R.id.gif);
        hintText = (TextView) findViewById(R.id.hint);
        btnLinearLayout = (LinearLayout) findViewById(R.id.mainmap_zoom_area);

        screenbtn.setOnClickListener(this);
        scaleMorebtn.setOnClickListener(this);
        scaleLessbtn.setOnClickListener(this);
        button_location.setOnClickListener(this);
        start_location_reminder.setOnClickListener(this);
        city_select.setOnClickListener(this);

        mDataHelper = DataHelper.getInstance(this);

        ReadExcelDataUtil.getInstance().addDbWriteFinishListener(this);
        if (ReadExcelDataUtil.getInstance().hasWrite) {
            initData();
            gif.setVisibility(View.GONE);
            setViewVisible(View.VISIBLE);
            hintText.setVisibility(View.GONE);
        } else {
            // 设置背景gif图片资源
            gif.setMovieResource(R.raw.two);
            gif.setVisibility(View.VISIBLE);
            hintText.setVisibility(View.VISIBLE);
            setViewVisible(View.GONE);
        }
        Intent bindIntent = new Intent(MainViewActivity.this, RemonderLocationService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);
        start_location_reminder.setText(MainViewActivity.this.getResources().getString(R.string.start_location));
        CommonFuction.writeBooleanSharedPreferences(this,CommonFuction.ISREMINDER,false);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_in) {
            sceneMap.zoomIn();
            sceneMap.postInvalidate();
        } else if (v.getId() == R.id.button_out) {
            sceneMap.zoomOut();
            sceneMap.postInvalidate();
        } else if (v.getId() == R.id.full_screen) {
            if (!sceneMap.getFullScree()) {
                sceneMap.setFullScree(true);
                sceneMap.setInitScale();
                Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.shenzhen);
                sceneMap.setBitmap(bitmap2);
            }
        } else if (v.getId() == R.id.button_location) {
            if (mRemonderLocationService != null) {
                BDLocation location = mRemonderLocationService.getCurrentLocation();
                if(!CommonFuction.isvalidLocation(location)){
                    ToastUitl.showText(MainViewActivity.this, this.getResources().getString(R.string.location_errot), Toast.LENGTH_LONG, true);
                }
                if (!MainViewActivity.this.getPersimmions() || location == null) {
                    return;
                }
                locationCurrentStation();
            }
        } else if (v.getId() == R.id.start_location_reminder) {

            if (mRemonderLocationService != null) {
                if (!getPersimmions()) {
                    //Toast.makeText(this,this.getString(R.string.please_set_permission),Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    mRemonderLocationService.startLocationService();
                }
                if(CommonFuction.getSharedPreferencesBooleanValue(MainViewActivity.this, CommonFuction.ISREMINDER)) {
                    start_location_reminder.setText(MainViewActivity.this.getResources().getString(R.string.start_location));
                    mRemonderLocationService.setCancleReminder();
                    sceneMap.resetMardEndState();
                    canSetReminder = 0;
                    startStationInfo = null;
                    endStationInfo = null;
                    currentStationInfo = null;
                } else if (endStationInfo != null) {
                    getReminderLines(startStationInfo,endStationInfo);
                    mRemonderLocationService.setStartReminder();
                    mRemonderLocationService.setStationInfoList(sceneMap.getMarkList());
                    start_location_reminder.setText(MainViewActivity.this.getResources().getString(R.string.stop_location));
                } else {
                    Toast.makeText(MainViewActivity.this, MainViewActivity.this.getString(R.string.please_set_target), Toast.LENGTH_SHORT).show();
                }
            }
        } else if (v.getId() == R.id.city_select) {
			/*currentCityNo = CommonFuction.writeSharedPreferences(MainViewActivity.this,CommonFuction.CITYNO,);
				if(TextUtils.isEmpty(currentCityNo)){
					currentCityNo = cityInfoList.get(0).getCityNo();
				}*/
        } else if (v.getId() == R.id.start_location_reminder) {

        }
    }

    public void locationCurrentStation() {

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                BDLocation location = mRemonderLocationService.getCurrentLocation();
                if (!MainViewActivity.this.getPersimmions()) {
                    return;
                } else if (mRemonderLocationService != null) {
                    mRemonderLocationService.startLocationService();
                }
                if (mRemonderLocationService != null && location != null) {

                    Log.d(TAG, "locationCurrentStation location.getCityCode() = " + location.getCityCode() + " lot = " + location.getLatitude() + " lat = " + location.getLongitude());
                    if (location != null) {
                        currentCityNo = cityInfoList.get(location.getCityCode());
                        Log.d(TAG, "locationCurrentStation currentCityInfo CityName = " + currentCityNo.getCityName());
                        if (currentCityNo != null) {
                            String shpno = CommonFuction.getSharedPreferencesValue(MainViewActivity.this, CommonFuction.CITYNO);
                            Map<Integer,LineInfo> tempList = mLineInfoList;
                            if (!shpno.equals("" + currentCityNo)) {
                                CommonFuction.writeSharedPreferences(MainViewActivity.this, CommonFuction.CITYNO, "" + currentCityNo);
                                tempList = mDataHelper.getLineList(currentCityNo.getCityNo(), LineInfo.LINEID, "ASC");
                                if (tempList != null) {
                                    for (Map.Entry<Integer,LineInfo> entry : tempList.entrySet()) {
                                        entry.getValue().setStationInfoList(mDataHelper.QueryByStationLineNo(entry.getKey(), currentCityNo.getCityNo()));
                                    }
                                }
                                mLineInfoList.clear();
                            }
                            mLineInfoList = tempList;
                            Log.d(TAG, "locationCurrentStation mLineInfoList = " + mLineInfoList.size());
                            nerstStationInfo = getNerastStation(location);
                        }

                        //Log.d(TAG,"locationCurrentStation currentStationInfo = "+nerstStationInfo);
                        if (nerstStationInfo != null) {
                            CommonFuction.writeSharedPreferences(MainViewActivity.this, CommonFuction.INITCURRENTLINEID, nerstStationInfo.getCname());
                            Log.d(TAG, "locationCurrentStation lineid = " + nerstStationInfo.lineid + " name = " + nerstStationInfo.getCname());
                        }
                        selectLocationLine(nerstStationInfo);
                    }
                }
                Looper.loop();
            }
        }.start();
    }

    private void setStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            //window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(this.getResources().getColor(R.color.status_color));
        }
    }

    public void setViewVisible(int visible) {
        scaleMorebtn.setVisibility(visible);
        scaleLessbtn.setVisibility(visible);
        screenbtn.setVisibility(visible);
        sceneMap.setVisibility(visible);
        lineMap.setVisibility(visible);
        currentLineInfoText.setVisibility(visible);
        city_select.setVisibility(visible);
        button_location.setVisibility(visible);
        start_location_reminder.setVisibility(visible);
        //middle_line.setVisibility(visible);
    }

    private void initData() {
        Log.d(TAG,"initData");
        minitDataThread = new initDataThread();
        minitDataThread.start();
    }

    class initDataThread extends Thread {
        @Override
        public void run() {
            cityInfoList = mDataHelper.getAllCityInfo();
            String shpno = CommonFuction.getSharedPreferencesValue(MainViewActivity.this, CommonFuction.CITYNO);
            allTransferList = mDataHelper.QueryByStationAllTransfer(CommonFuction.CITYNO);
            if (!TextUtils.isEmpty(shpno)) {
                currentCityNo = cityInfoList.get(shpno);
            }
            if (currentCityNo == null) {
                currentCityNo = CommonFuction.getFirstOrNull(cityInfoList);
            }
            if(currentCityNo == null){
                return;
            }
            mLineInfoList = mDataHelper.getLineList(currentCityNo.getCityNo(), LineInfo.LINEID, "ASC");
            Log.d(TAG, "currentCityNo = " + currentCityNo.getCityNo() + " mLineInfoList.size = " + mLineInfoList.size());
            int firstLineid = -1;
            for (Map.Entry<Integer,LineInfo> entry : mLineInfoList.entrySet()) {
                if(firstLineid < 0)
                    firstLineid = entry.getKey();
                List<StationInfo> list = mDataHelper.QueryByStationLineNoCanTransfer(entry.getValue().lineid, currentCityNo.getCityNo());
                allLineCane.put(entry.getKey(),getLineAllLined(list));
                if(entry.getKey() > maxLineid){
                    maxLineid = entry.getKey();
                }
                entry.getValue().setStationInfoList(mDataHelper.QueryByStationLineNo(entry.getKey(), currentCityNo.getCityNo()));
            }
            maxLineid+= 1;//找出路线最大编号加一
            currentIndex = CommonFuction.convertToInt(CommonFuction.getSharedPreferencesValue(MainViewActivity.this, CommonFuction.CURRENTLINEID),
                    firstLineid);
            //单一线路-------------------------------------
            initLineMap(currentIndex);
            if (CommonFuction.getSharedPreferencesBooleanValue(MainViewActivity.this, CommonFuction.ISREMINDER)) {

                sceneMap.setMardStartState(true, CommonFuction.getSharedPreferencesValue(MainViewActivity.this, CommonFuction.STARTSTATIONNAME));
                sceneMap.setMardEndState(true, CommonFuction.getSharedPreferencesValue(MainViewActivity.this, CommonFuction.ENDSTATIONNAME));
                sceneMap.setCurrentStation(CommonFuction.getSharedPreferencesValue(MainViewActivity.this, CommonFuction.CURRENTSTATIONNAME));
            }

            Message msg = new Message();
            msg.what = 1;
            mHandler.sendMessage(msg);
        }
    }

    public Map<Integer,Integer> getLineAllLined(List<StationInfo> list){
        Map<Integer,Integer> listStr = new HashMap<Integer,Integer>();
        if(list == null && list.size() <=0 )
            return listStr;
        StringBuffer str = new StringBuffer();
        for(StationInfo stationInfo:list){
            String lineList[] = stationInfo.getTransfer().split(CommonFuction.TRANSFER_SPLIT);
            int size = lineList.length;
            for(int i = 0;i < size;i++){
                int line = CommonFuction.convertToInt(lineList[i],-1);
                if(!listStr.containsKey(line) && line != stationInfo.lineid){
                    listStr.put(line,line);
                    str.append(lineList[i]+"  ");
                }
            }
        }
        Log.d(TAG,"getLineAllLined lineid = "+list.get(0).lineid+" all lined = "+str);
        return listStr;
    }

    private void initLineColorMap() {
        lineMap.releaseSource();
        int colorRow = mLineInfoList.size() / LineMapColorView.ROWMAXCOUNT + 1;

        int height = MarkObject.rectSizeHeight * colorRow * 3;
        Bitmap bitmap1 = Bitmap.createBitmap((int) lineMap.getViewWidth(), height,
                Bitmap.Config.ARGB_8888);
        bitmap1.eraseColor(this.getResources().getColor(R.color.white));//填充颜色
        lineMap.setBitmap(bitmap1);
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) lineMap.getLayoutParams();
        // 取控件aaa当前的布局参数
        linearParams.height = height;
        lineMap.setLayoutParams(linearParams); // 使设置好的布局参数应用到控件
        float disx = lineMap.getViewWidth() / LineMapColorView.ROWMAXCOUNT;
        //Log.d(TAG, "disx = "+disx+this.getResources().getColor(lineColor[0]));
        float dis = this.getResources().getDimension(R.dimen.btn_size) + this.getResources().getDimension(R.dimen.magin_left);
        for (Map.Entry<Integer,LineInfo> entry : mLineInfoList.entrySet()) {
            entry.getValue().setStationInfoList(mDataHelper.QueryByStationLineNo(entry.getKey(), currentCityNo.getCityNo()));
        }

        int n = 0;
        for (Map.Entry<Integer,LineInfo> entry : mLineInfoList.entrySet()) {
            MarkObject markObject = new MarkObject();
            float row = n / (LineMapColor.ROWMAXCOUNT) + 1;
            float cloume = n % (LineMapColor.ROWMAXCOUNT);
            float x = cloume * disx - MarkObject.rectSizewidth + dis;
            float y = row * MarkObject.ROWHEIGHT - MarkObject.ROWHEIGHT / 2 - MarkObject.rectSizeHeight / 2;
            //Log.d(TAG", "row = "+row+" cloume = "+cloume+" x = "+x+" y = "+y);
            markObject.setLineid(entry.getKey());
            markObject.setX(x);
            markObject.setY(y);
            markObject.setName(entry.getValue().getLinename());
            markObject.setCurrentsize(MarkObject.rectSizeHeight);
            markObject.setColorId(entry.getValue().colorid);
            markObject.setmBitmap(BitmapUtil.drawNewColorBitmap(MarkObject.rectSizewidth, MarkObject.rectSizeHeight, markObject.getColorId()));
            markObject.setMarkListener(new MarkClickListener() {

                @Override
                public void onMarkClick(int x, int y, final MarkObject markObject) {
                    // TODO Auto-generated method stub
                    if (currentIndex != markObject.getLineid() || sceneMap.getFullScree()) {
                        currentIndex = markObject.getLineid();
                        currentLineInfoText.setBackgroundColor(markObject.getColorId());

                        new Thread() {
                            @Override
                            public void run() {
                                initLineMap(markObject.getLineid());
                            }
                        }.start();
                    }
                }
            });
            lineMap.addMark(markObject);
            n++;
        }

        LinearLayout.LayoutParams linearParams1 = (LinearLayout.LayoutParams) btnLinearLayout.getLayoutParams(); //取控件textView当前的布局参数
        linearParams1.topMargin = (int) (MarkObject.rectSizeHeight * 1.5f);// 控件的高强制设成20

        linearParams1.leftMargin = (int) this.getResources().getDimension(R.dimen.magin_left);// 控件的宽强制设成30

        btnLinearLayout.setLayoutParams(linearParams1); //使设置好的布局参数应用到控件</pre>
    }

    private void initLineMap(int lineId) {

        LineInfo lineInfo = getLineInfoByLineid(mLineInfoList,lineId);
        if(lineInfo != null){
            mStationInfoList = lineInfo.getStationInfoList();
        }else{
            return;
        }
        sceneMap.setFullScree(false);
        sceneMap.clearMark();
        int countRow = mStationInfoList.size() / LineMapView.ROWMAXCOUNT + extraRow;
        float initX = 1f / LineMapView.ROWMAXCOUNT / 2f;
        float initY = 1f / countRow / 2;
        boolean isReminder = CommonFuction.getSharedPreferencesBooleanValue(MainViewActivity.this, CommonFuction.ISREMINDER);
        String currentStationName = CommonFuction.getSharedPreferencesValue(MainViewActivity.this, CommonFuction.INITCURRENTLINEID);
        final int mStationInfoList_size = mStationInfoList.size();// Moved  mStationInfoList.size() call out of the loop to local variable mStationInfoList_size
        for (int n = 0; n < mStationInfoList_size; n++) {
            MarkObject markObject = new MarkObject();
            float row = n / (LineMapView.ROWMAXCOUNT) + extraRow;
            float cloume = n % (LineMapView.ROWMAXCOUNT);

            float x = cloume / (LineMap.ROWMAXCOUNT) + initX;
            float y = row / countRow - initY;
            markObject.setMapX(x);
            markObject.setMapY(y);
            markObject.mStationInfo = mStationInfoList.get(n);
            markObject.setName(markObject.mStationInfo.getCname().split(" ")[0]);
            //Log.d(TAG,"initLineMap currentStationName = "+currentStationName+" markObject.getName() = "+markObject.getName());
            if (!TextUtils.isEmpty(currentStationName) && !TextUtils.isEmpty(markObject.getName()) &&
                    currentStationName.equals(markObject.getName())) {
                markObject.isCurrentLocationStation = true;
            }
            markObject.mStationInfo.colorId = lineInfo.colorid;
            markObject.setColorId(markObject.mStationInfo.colorId);
            markObject.setRadius(MarkObject.size / 3);
            markObject.setCurrentsize(MarkObject.size + MarkObject.DIFF);
            if(isReminder){
                if(startStationInfo != null)
                    markObject.isStartStation = isSameStationInfo(startStationInfo,markObject.mStationInfo);
                if(endStationInfo != null)
                    markObject.isEndStation = isSameStationInfo(endStationInfo,markObject.mStationInfo);
                if(currentStationInfo != null)
                    markObject.isCurrentStation = isSameStationInfo(currentStationInfo,markObject.mStationInfo);
            }
            if(nerstStationInfo != null && nerstStationInfo.getCname().equals(markObject.mStationInfo.getCname())){
                markObject.isCurrentLocationStation = true;
            }

            boolean canTransfer = markObject.mStationInfo.canTransfer();
            if (canTransfer) {
                markObject.setmBitmap(CommonFuction.getbitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.cm_route_map_pin_dottransfer), MarkObject.size + MarkObject.DIFF, MarkObject.size + MarkObject.DIFF));
            } else {
                markObject.setmBitmap(BitmapUtil.drawNewBitmap(this, MarkObject.size / 3, MarkObject.size + MarkObject.DIFF, MarkObject.size + MarkObject.DIFF, markObject.getColorId(), canTransfer));
            }
            markObject.setMarkListener(new MarkClickListener() {

                @Override
                public void onMarkClick(int x, int y, MarkObject markObject) {
                    // TODO Auto-generated method stub
                    //Toast.makeText(MainViewActivity.this, markObject.getName(), Toast.LENGTH_SHORT).show();
                    //showSettingStationDialog(markObject.mStationInfo);
                    showDialog(markObject);
                }
            });
            sceneMap.addMark(markObject);
        }

        Message msg = new Message();
        msg.what = 2;
        mHandler.sendMessage(msg);

    }

    private boolean isSameStationInfo(StationInfo obj1,StationInfo obj2){
        if(obj1.lineid  == obj2.lineid &&
                obj1.getCname().equals(obj2.getCname())){
            return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        RemonderLocationService.state = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        sceneMap.setPauseState(false);
        sceneMap.postInvalidate();
        if (mRemonderLocationService != null) {
            mRemonderLocationService.cancleNotification();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sceneMap.setPauseState(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        RemonderLocationService.state = false;
        if (mRemonderLocationService != null) {
            mRemonderLocationService.setNotification(true);
        }
    }

    public void dbWriteFinishNotif() {
        initData();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CommonFuction.writeBooleanSharedPreferences(this, CommonFuction.ISREMINDER, false);
        ReadExcelDataUtil.getInstance().removeDbWriteFinishListener(this);
        lineMap.releaseSource();
        sceneMap.releaseSource();
        mLineInfoList.clear();
        mStationInfoList.clear();
        if (mRemonderLocationService != null) {
            unbindService(connection);
            mRemonderLocationService = null;
        }
    }

    private void showDialog(final MarkObject markObject) {
        List<ExitInfo> existInfoList = mDataHelper.QueryByExitInfoCname(markObject.mStationInfo.getCname(), currentCityNo.getCityNo());
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
        }
        mSettingReminderDialog = new SettingReminderDialog(this,
                R.style.Dialog, new NoticeDialogListener() {
            @Override
            public void onClick(View view) {
                try {
                    switch (view.getId()) {
                        case R.id.start:
                            mSettingReminderDialog.dismiss();
                            sceneMap.setMardStartState(true, markObject.mStationInfo.getCname());
                            sceneMap.postInvalidate();
                            canSetReminder++;
                            startStationInfo = markObject.mStationInfo;
                            break;
                        case R.id.end:
                            mSettingReminderDialog.dismiss();
                            sceneMap.setMardEndState(true, markObject.mStationInfo.getCname());
                            sceneMap.postInvalidate();
                            if (mRemonderLocationService != null) {
                                mRemonderLocationService.setEndStation(markObject.mStationInfo);
                            }
                            canSetReminder++;
                            endStationInfo = markObject.mStationInfo;
                            break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, markObject.mStationInfo.getTransfer(), existInfostr,
                "" + markObject.mStationInfo.getLineid(), currentCityNo.getCityName(), markObject.mStationInfo.getCname());
        mSettingReminderDialog.setContentView(R.layout.setting_reminder_dialog);
        mSettingReminderDialog.show();
    }

    private StationInfo getNerastStation(BDLocation location){
        double min = Double.MAX_VALUE;
        double longitude = 0;
        double latitude = 0;
        double dis = 0;
        StationInfo nerstStationInfo = null;
        if (location != null && mLineInfoList != null) {
            for (Map.Entry<Integer, LineInfo> entry : mLineInfoList.entrySet()) {
                for (StationInfo stationInfo : entry.getValue().getStationInfoList()) {
                    longitude = CommonFuction.convertToDouble(stationInfo.getLot(), 0);
                    latitude = CommonFuction.convertToDouble(stationInfo.getLat(), 0);
                    dis = CommonFuction.getDistanceLat(longitude, latitude, location.getLongitude(), location.getLatitude());
                    if (min > dis) {
                        min = dis;
                        nerstStationInfo = stationInfo;
                    }
                }
            }
        }
        return nerstStationInfo;
    }

    //查询站台是否与目标线路有相同线路
    private int getSameLine(StationInfo start,int lined){
        final String lines[] = start.getTransfer().split(CommonFuction.TRANSFER_SPLIT);
        int size = lines.length;
        for(int i = 0;i < size;i++){//找出和终点站相同点不用换乘
            if(CommonFuction.convertToInt(lines[i],0) == lined){//找到相同线路站不用换乘
                return CommonFuction.convertToInt(lines[i],0);
            }
        }
        return -1;
    }

    public void getReminderLines(StationInfo start,final StationInfo end){
        if(end == null){
            return;
        }
        currentAllStationList.clear();
        final Integer allLineNodes[] = new Integer[maxLineid];
        int n = 0;
        for(int i = 0;i<maxLineid;i++){
            //Log.d(TAG,"getReminderLines entry.key ="+entry.getKey()+" entry.value = "+entry.getValue());
            allLineNodes[i] = i;
        }
        if(start == null && mRemonderLocationService != null){
            start = getNerastStation(mRemonderLocationService.getCurrentLocation());
            if(start != null){
                Log.d(TAG,"getReminderLines start.getTransfer() = "+start.getTransfer()+" start.lineid = "+start.lineid+" start.name = "+start.getCname()+" end.lineid = "+end.lineid+" end.name = "+end.getCname());
                if(start.lineid == end.lineid){//可以直达
                    startStationInfo = start;
                }else if(start.canTransfer()){//可以换乘
                    int lined = getSameLine(start,end.lineid);
                    if(lined != -1){
                        LineInfo lineInfo = getLineInfoByLineid(mLineInfoList,lined);//先找线路
                        if(lineInfo != null){
                            StationInfo stationInfo= getStationInfoByLineidAndName(lineInfo.getStationInfoList(),start.getCname());//再找相同站台
                            if(stationInfo != null){
                                start = startStationInfo = stationInfo;
                                Log.d(TAG,"getReminderLines findStation lineid = "+startStationInfo.lineid+" name = "+startStationInfo.getCname());
                            }
                        }
                    }
                }
                if(startStationInfo == null){//需要换乘才能找到路线,最近路线方案,最少换乘方案
                    GrfAllEdge.createGraph(allLineNodes,allLineCane,start.lineid,end.lineid);
                }else{
                    LineInfo currentLineInfo = getLineInfoByLineid(mLineInfoList,startStationInfo.lineid);
                    if(currentLineInfo != null){
                        Log.d(TAG,"getReminderLines current line = "+currentLineInfo.lineid+" line name = "+currentLineInfo.linename);
                        findLinedStation(currentLineInfo,startStationInfo,end,currentAllStationList);
                    }
                }
            }else{//找不到任何站台
                return;
            }
        }else{
            //在一条线路上
            if(start.lineid == end.lineid){
                LineInfo currentLineInfo = null;
                currentLineInfo = getLineInfoByLineid(mLineInfoList,startStationInfo.lineid);
                if(currentLineInfo != null){
                    Log.d(TAG,"getReminderLines current line = "+currentLineInfo.lineid+" line name = "+currentLineInfo.linename);
                    findLinedStation(currentLineInfo,start,end,currentAllStationList);
                }
            }else{
                Log.d(TAG,"getReminderLines need transfer start.lineid = "+start.lineid+" start.name = "+start.getCname()+" end.lineid = "+end.lineid+" end.name = "+end.getCname());
                GrfAllEdge.createGraph(allLineNodes,allLineCane,start.lineid,end.lineid);
            }
        }

        StringBuffer str = new StringBuffer();
        for(StationInfo mStationInfo:currentAllStationList){
            str.append(mStationInfo.getCname()+" -> ");

        }
        Log.d(TAG,"getReminderLines str = "+str);
    }

    public void findLinedStation(LineInfo lineInfo,StationInfo start,StationInfo end,List<StationInfo> currentAllStationList){
        List<StationInfo> stationInfoList = lineInfo.getStationInfoList();
        if(!currentAllStationList.contains(start))
            currentAllStationList.add(start);
        if(start.pm < end.pm){
            for (StationInfo mStationInfo:stationInfoList){
                int next = currentAllStationList.get(currentAllStationList.size()-1).pm+1;
                if(next == mStationInfo.pm && !currentAllStationList.contains(mStationInfo)){
                    currentAllStationList.add(mStationInfo);
                }
                if(next == end.pm){
                    break;
                }
            }
        }else{
            StationInfo mStationInfo = null;
            for (int i = stationInfoList.size()-1;i>=0;i--){
                mStationInfo = stationInfoList.get(i);
                int next = currentAllStationList.get(currentAllStationList.size()-1).pm-1;
                if(next == mStationInfo.pm && !currentAllStationList.contains(mStationInfo)){
                    currentAllStationList.add(mStationInfo);
                }
                if(next == end.pm){
                    break;
                }
            }
        }
    }

    public LineInfo getLineInfoByLineid(Map<Integer,LineInfo> lineInfoList,int lineid){
        return lineInfoList.get(lineid);
    }

    public StationInfo getStationInfoByLineidAndName(List<StationInfo> stationInfoList,String name) {
        for (StationInfo mStationInfo:stationInfoList){//查询最站台和当前路线相同站
            if(mStationInfo.getCname().equals(name)){
                return mStationInfo;
            }
        }
        return null;
    }

    private void selectLocationLine(StationInfo nerstStationInfo) {
        if (nerstStationInfo != null) {
            if (nerstStationInfo.canTransfer()) {
                if (mSelectLineDialog != null) {
                    mSelectLineDialog.dismiss();
                }
                mSelectLineDialog = new SelectLineDialog(MainViewActivity.this, R.style.Dialog, new NoticeDialogListener() {
                    @Override
                    public void onClick(View view) {
                        initData();
                    }
                }, nerstStationInfo, mLineInfoList);
                mSelectLineDialog.show();
            } else {
                if (nerstStationInfo != null) {
                    currentIndex = nerstStationInfo.getLineid();
                    CommonFuction.writeSharedPreferences(MainViewActivity.this, CommonFuction.CURRENTLINEID, "" + currentIndex);
                    initData();
                }
            }
        }
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
            // TODO Auto-generated method stub
            mUpdateBinder = (RemonderLocationService.UpdateBinder) service;
            mRemonderLocationService = mUpdateBinder.getService();
            if (mRemonderLocationService != null) {
                if (mRemonderLocationService != null) {
                    mRemonderLocationService.startLocationService();
                }
                mRemonderLocationService.setCallback(new RemonderLocationService.Callback() {

                    @Override
                    public void setCurrentStation(String startCname, String endName, String current) {
                        // TODO Auto-generated method stub
                        //Log.d(TAG,"====StationInfo===="+num.getStationInfo());
                        sceneMap.setCurrentStation(current);
                        sceneMap.setMardStartState(true, startCname);
                        sceneMap.setMardEndState(true, endName);
                    }

                    @Override
                    public void arriaved(boolean state) {
                        currentLineInfoText.setText(MainViewActivity.this.getResources().getString(R.string.arrived));
                        sceneMap.resetMardEndState();
                    }

                    @Override
                    public void loactionStation(BDLocation location) {
                        if(!CommonFuction.isvalidLocation(location))
                            return;
                        StationInfo stationInfo = getNerastStation(location);
                        nerstStationInfo = stationInfo;

                        //Log.d(TAG,"loactionStation nearststationInfo.getCname = "+stationInfo.getCname());
                        if(stationInfo != null){
                            if(endStationInfo != null && endStationInfo.getCname().equals(stationInfo.getCname())){
                                Log.d(TAG,"loactionStation has arrived target");
                                currentLineInfoText.setText(MainViewActivity.this.getResources().getString(R.string.arrived));
                                sceneMap.resetMardEndState();
                                mRemonderLocationService.setCancleReminder();
                                return;
                            }
                            if(startStationInfo != null && endStationInfo != null){
                                if(startStationInfo.lineid == endStationInfo.lineid){//在一条线路上
                                    LineInfo currentLineInfo = getLineInfoByLineid(mLineInfoList,startStationInfo.lineid);

                                    if(currentLineInfo != null){
                                        StationInfo mStationInfo = getStationInfoByLineidAndName(currentLineInfo.getStationInfoList(),stationInfo.getCname());
                                        if(mStationInfo != null){
                                            sceneMap.setCurrentStation(stationInfo.getCname());
                                            currentStationInfo = mStationInfo;
                                            //Log.d(TAG,"loactionStation current line = "+currentStationInfo.lineid+" current station name = "+currentStationInfo.getCname());
                                        }
                                    }
                                }else{//不是一条线路需要换乘
                                    if(startStationInfo.lineid == sceneMap.getCurrentLineId()){

                                    }
                                }

                            }
                        }
                    }

                    @Override
                    public void errorHint(String error) {
                        ToastUitl.showText(MainViewActivity.this, error, Toast.LENGTH_LONG, true);
                    }
                });
                if (ReadExcelDataUtil.getInstance().hasWrite) {
                    locationCurrentStation();
                }
            }

        }

    };
}
