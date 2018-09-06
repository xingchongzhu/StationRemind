package com.traffic.locationremind.baidu.location.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
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
import com.traffic.locationremind.baidu.location.dialog.SettingReminderDialog;
import com.traffic.locationremind.baidu.location.dialog.SettingReminderDialog.NoticeDialogListener;
import com.traffic.locationremind.common.util.*;
import com.traffic.locationremind.manager.bean.CityInfo;
import com.traffic.locationremind.manager.bean.ExitInfo;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataHelper;

import java.util.*;

public class MainViewActivity extends CommonActivity implements ReadExcelDataUtil.DbWriteFinishListener, View.OnClickListener {

    private final static String TAG = "MainViewActivity";

    private final static int INITMAPCOLOR = 1;//初始化当前城市地铁显示
    private final static int SHOWCURRENTLINED = 2;//当前选择路线
    private final static int STARTLOCATION = 3;//开始定位
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

    private initDataThread minitDataThread;
    private int extraRow = 1;

    private RemonderLocationService.UpdateBinder mUpdateBinder;
    private RemonderLocationService mRemonderLocationService;
    public int canSetReminder = 0;

    SelectLineDialog mSelectLineDialog;
    SettingReminderDialog mSettingReminderDialog;

    private DataHelper mDataHelper;//数据库
    private CityInfo currentCityNo = null;
    private Map<String,CityInfo> cityInfoList;//所有城市信息
    private Map<Integer,LineInfo> mLineInfoList;//地图线路

    private List<StationInfo> mStationInfoList ;//地图站台信息
    StationInfo startStationInfo, currentStationInfo, endStationInfo,nerstStationInfo;
    List<Map.Entry<List<Integer>,List<StationInfo>>> lastLinesLast;//最终查询得到路线
    private Map<Integer, Map<Integer,Integer>> allLineCane = new HashMap<Integer, Map<Integer,Integer>>();//用于初始化路线矩阵
    private int maxLineid = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            switch (msg.what) {
                case INITMAPCOLOR:
                    if (mLineInfoList != null && mLineInfoList.size() > 0) {
                        //线路颜色-------------------------------------
                        initLineColorMap();
                        showSelectLine();
                        //----------------------------------------
                        gif.setVisibility(View.GONE);
                        setViewVisible(View.VISIBLE);
                        hintText.setVisibility(View.GONE);
                    }
                    break;
                case SHOWCURRENTLINED:
                    showSelectLine();
                    break;

                case STARTLOCATION:
                   // mRemonderLocationService.setStartReminder();
                   // mRemonderLocationService.setStationInfoList(sceneMap.getMarkList());
                    start_location_reminder.setText(MainViewActivity.this.getResources().getString(R.string.stoplocation));
                    currentLineInfoText.setText(PathSerachUtil.printAllRecomindLine(lastLinesLast));//打印所有路线);
                    break;
            }

        }
    };

    private void showSelectLine(){
        mStationInfoList = mLineInfoList.get(currentIndex).getStationInfoList();
        currentLineInfoText.setText(CommonFuction.getSubwayShowText(MainViewActivity.this, mDataHelper, mLineInfoList.get(currentIndex)));
        currentLineInfoText.setBackgroundColor(mLineInfoList.get(currentIndex).colorid);
        int countRow = mStationInfoList.size() / LineMap.ROWMAXCOUNT + 1;
        Bitmap bitmap = Bitmap.createBitmap((int) sceneMap.getViewWidth(), MarkObject.ROWHEIGHT * countRow,
                Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(MainViewActivity.this.getResources().getColor(R.color.white));//填充颜色
        sceneMap.setBitmap(bitmap);
        sceneMap.postInvalidate();
    }

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

        mDataHelper = new DataHelper(this);

        ReadExcelDataUtil.getInstance().addDbWriteFinishListener(this);
        if (ReadExcelDataUtil.getInstance().hasWrite) {
            initData();
            gif.setVisibility(View.GONE);
            setViewVisible(View.VISIBLE);
            hintText.setVisibility(View.GONE);
        } else {
            // 设置背景gif图片资源
            //gif.setMovieResource(R.raw.two);
            gif.setVisibility(View.VISIBLE);
            hintText.setVisibility(View.VISIBLE);
            setViewVisible(View.GONE);
        }
        Intent bindIntent = new Intent(MainViewActivity.this, RemonderLocationService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);
        start_location_reminder.setText(MainViewActivity.this.getResources().getString(R.string.startlocation));
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
                    start_location_reminder.setText(MainViewActivity.this.getResources().getString(R.string.startlocation));
                    mRemonderLocationService.setCancleReminder();
                    sceneMap.resetMardEndState();
                    canSetReminder = 0;
                    startStationInfo = null;
                    endStationInfo = null;
                    currentStationInfo = null;
                } else if (endStationInfo != null) {
                    new Thread() {
                        @Override
                        public void run() {
                            getReminderLines(startStationInfo,endStationInfo);
                        }
                    }.start();
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
                                tempList = mDataHelper.getLineList( LineInfo.LINEID, "ASC");
                                if (tempList != null) {
                                    for (Map.Entry<Integer,LineInfo> entry : tempList.entrySet()) {
                                        entry.getValue().setStationInfoList(mDataHelper.QueryByStationLineNo(entry.getKey(), currentCityNo.getCityNo()));
                                    }
                                }
                                mLineInfoList.clear();
                            }
                            mLineInfoList = tempList;
                            Log.d(TAG, "locationCurrentStation mLineInfoList = " + mLineInfoList.size());
                            nerstStationInfo = PathSerachUtil.getNerastStation(location,mLineInfoList);
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
            if (!TextUtils.isEmpty(shpno)) {
                currentCityNo = cityInfoList.get(shpno);
            }
            if (currentCityNo == null) {
                currentCityNo = CommonFuction.getFirstOrNull(cityInfoList);
            }
            if(currentCityNo == null){
                return;
            }
            mLineInfoList = mDataHelper.getLineList(LineInfo.LINEID, "ASC");
            Log.d(TAG, "currentCityNo = " + currentCityNo.getCityNo() + " mLineInfoList.size = " + mLineInfoList.size());
            int firstLineid = -1;
            for (Map.Entry<Integer,LineInfo> entry : mLineInfoList.entrySet()) {
                if(firstLineid < 0)
                    firstLineid = entry.getKey();
                List<StationInfo> list = mDataHelper.QueryByStationLineNoCanTransfer(entry.getValue().lineid, currentCityNo.getCityNo());
                allLineCane.put(entry.getKey(),PathSerachUtil.getLineAllLined(list));
                if(entry.getKey() > maxLineid){
                    maxLineid = entry.getKey();
                }
                entry.getValue().setStationInfoList(mDataHelper.QueryByStationLineNo(entry.getKey(), currentCityNo.getCityNo()));
            }
            maxLineid+= 1;//找出路线最大编号加一
            currentIndex = CommonFuction.convertToInt(CommonFuction.getSharedPreferencesValue(MainViewActivity.this, CommonFuction.CURRENTLINEID), firstLineid);
            //单一线路-------------------------------------
            initLineMap(currentIndex);
            if (CommonFuction.getSharedPreferencesBooleanValue(MainViewActivity.this, CommonFuction.ISREMINDER)) {

                sceneMap.setMardStartState(true, CommonFuction.getSharedPreferencesValue(MainViewActivity.this, CommonFuction.STARTSTATIONNAME));
                sceneMap.setMardEndState(true, CommonFuction.getSharedPreferencesValue(MainViewActivity.this, CommonFuction.ENDSTATIONNAME));
                sceneMap.setCurrentStation(CommonFuction.getSharedPreferencesValue(MainViewActivity.this, CommonFuction.CURRENTSTATIONNAME));
            }

            Message msg = new Message();
            msg.what = INITMAPCOLOR;
            mHandler.sendMessage(msg);
        }
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

        LineInfo lineInfo = PathSerachUtil.getLineInfoByLineid(mLineInfoList,lineId);
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
        msg.what = SHOWCURRENTLINED;
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
           // mRemonderLocationService.setNotification(true);
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
        mDataHelper.Close();
    }

    private void showDialog(final MarkObject markObject) {
        List<ExitInfo> existInfoList = mDataHelper.QueryByExitInfoCname(markObject.mStationInfo.getCname());
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
                                //mRemonderLocationService.setEndStation(markObject.mStationInfo);
                            }
                            canSetReminder++;
                            endStationInfo = markObject.mStationInfo;
                            break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void selectLine(int num){

            }
        }, markObject.mStationInfo.getTransfer(), existInfostr,
                "" + markObject.mStationInfo.getLineid(), currentCityNo.getCityName(), markObject.mStationInfo.getCname());
        mSettingReminderDialog.setContentView(R.layout.setting_reminder_dialog);
        mSettingReminderDialog.show();
    }

    public void getReminderLines(StationInfo start,final StationInfo end){
        if(end == null){
            return;
        }
        List<List<Integer>> transferLine = new ArrayList<List<Integer>>();//所有换乘路线
        final Integer allLineNodes[] = new Integer[maxLineid];
        for(int i = 0;i<maxLineid;i++){//初始化所有线路
            allLineNodes[i] = i;
        }
        if(start == null && mRemonderLocationService != null) {
            start = PathSerachUtil.getNerastStation(mRemonderLocationService.getCurrentLocation(),mLineInfoList);
        }
        if(start!= null){
            if(!start.canTransfer() && !end.canTransfer()) {//都不能换乘
                if(start.lineid == end.lineid){//在一条线路
                    List<Integer> list = new ArrayList<Integer>();//将要查询路线放入
                    list.add(start.lineid);
                    transferLine.add(list);
                }else{
                    transferLine = GrfAllEdge.createGraph(allLineNodes, allLineCane, start.lineid, end.lineid);
                }
            }else if(start.canTransfer() && !end.canTransfer()){
                int lined = PathSerachUtil.isSameLine(start,end.lineid);
                if(lined > 0){//在一条线路
                    List<Integer> list = new ArrayList<Integer>();//将要查询路线放入
                    list.add(lined);
                    transferLine.add(list);
                }else{
                    String lines[] = start.getTransfer().split(CommonFuction.TRANSFER_SPLIT);
                    for(int i = 0;i < lines.length;i++){
                        int startid= CommonFuction.convertToInt(lines[i],-1);
                        if(startid >= 0) {
                            List<List<Integer>> temp = GrfAllEdge.createGraph(allLineNodes, allLineCane, startid, end.lineid);
                            transferLine.addAll(temp);
                        }
                    }
                }
            }else if(!start.canTransfer() && end.canTransfer()){
                int lined = PathSerachUtil.isSameLine(end,start.lineid);
                if(lined > 0){//在一条线路
                    List<Integer> list = new ArrayList<Integer>();//将要查询路线放入
                    list.add(lined);
                    transferLine.add(list);
                }else{
                    String lines[] = end.getTransfer().split(CommonFuction.TRANSFER_SPLIT);
                    for(int i = 0;i < lines.length;i++){
                        int startid= CommonFuction.convertToInt(lines[i],-1);
                        if(startid >= 0) {
                            List<List<Integer>> temp = GrfAllEdge.createGraph(allLineNodes, allLineCane,start.lineid , startid);
                            transferLine.addAll(temp);
                        }
                    }
                }
            }else{
                int lined = PathSerachUtil.isTwoStationSameLine(start,end);
                if(lined > 0){//在一条线路
                    List<Integer> list = new ArrayList<Integer>();//将要查询路线放入
                    list.add(lined);
                    transferLine.add(list);
                }else{
                    String startLines[] = start.getTransfer().split(CommonFuction.TRANSFER_SPLIT);
                    String endLines[] = end.getTransfer().split(CommonFuction.TRANSFER_SPLIT);
                    for(int i = 0;i < startLines.length;i++){//遍历所有情况
                        int startid= CommonFuction.convertToInt(startLines[i],-1);
                        if(startid > 0){
                            for(int j = 0;j < endLines.length;j++){
                                int endid= CommonFuction.convertToInt(endLines[j],-1);
                                if(endid > 0){
                                    List<List<Integer>> temp = GrfAllEdge.createGraph(allLineNodes, allLineCane, startid, endid);
                                    transferLine.addAll(temp);
                                }
                            }
                        }
                    }
                }
            }
        }
        Collections.sort(transferLine, new Comparator<List<Integer>>(){
            /*
             * int compare(Person p1, Person p2) 返回一个基本类型的整型，
             * 返回负数表示：p1 小于p2，
             * 返回0 表示：p1和p2相等，
             * 返回正数表示：p1大于p2
             */
            public int compare(List<Integer> p1, List<Integer> p2) {
                //按照换乘次数
                if(p1.size() > p2.size()){
                    return 1;
                }
                if(p1.size() == p2.size()){
                    return 0;
                }
                return -1;
            }
        });

        if(start != null || end != null){
            //找出所有路径
            Log.d(TAG,"getReminderLines find all line = "+transferLine.size()+" start = "+start.getCname()+" end = "+end.getCname());
            lastLinesLast = PathSerachUtil.getLastRecomendLines(PathSerachUtil.getAllLineStation(mLineInfoList,transferLine,start,end));//查询最终线路
            Message msg = new Message();
            msg.what = STARTLOCATION;
            mHandler.sendMessage(msg);
        }else{

        }

        transferLine.clear();
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
                    @Override
                    public void selectLine(int lineid) {
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
                        StationInfo stationInfo = PathSerachUtil.getNerastStation(location,mLineInfoList);
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
                                    LineInfo currentLineInfo = PathSerachUtil.getLineInfoByLineid(mLineInfoList,startStationInfo.lineid);

                                    if(currentLineInfo != null){
                                        StationInfo mStationInfo = PathSerachUtil.getStationInfoByLineidAndName(currentLineInfo.getStationInfoList(),stationInfo.getCname());
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
