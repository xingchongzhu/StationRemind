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
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.common.util.NotificationUtil;
import com.traffic.locationremind.common.util.ReadExcelDataUtil;
import com.traffic.locationremind.manager.bean.CityInfo;
import com.traffic.locationremind.manager.bean.ExitInfo;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataHelper;

import java.util.ArrayList;
import java.util.List;

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
    private List<CityInfo> cityInfoList = new ArrayList<CityInfo>();//所有城市信息
    private List<LineInfo> mLineInfoList = new ArrayList<LineInfo>();//地图线路
    private List<StationInfo> mStationInfoList = new ArrayList<StationInfo>();//地图站台信息

    private initDataThread minitDataThread;
    private int extraRow = 1;

    private RemonderLocationService.UpdateBinder mUpdateBinder;
    private RemonderLocationService mRemonderLocationService;
    private boolean isOncreate = false;
    public int canSetReminder = 0;

    SelectLineDialog mSelectLineDialog;

	private Handler mHandler = new Handler() {
        @Override
		public void handleMessage(Message msg) {

			super.handleMessage(msg);
			switch (msg.what){
				case 1:
					if(mLineInfoList != null && mLineInfoList.size() > 0){
						//线路颜色-------------------------------------
						initLineColorMap();

                        final int mLineInfoList_size = mLineInfoList.size();// Moved  mLineInfoList.size() call out of the loop to local variable mLineInfoList_size
                        for (int j = 0; j< mLineInfoList_size; j++){
							if(mLineInfoList.get(j).getLineid() == currentIndex){
								mStationInfoList = mLineInfoList.get(j).getStationInfoList();

								currentLineInfoText.setText(getSubwayShowText(mLineInfoList.get(j)));
								currentLineInfoText.setBackgroundColor(mLineInfoList.get(j).colorid);
								break;
							}
						}
						int countRow = mStationInfoList.size()/LineMap.ROWMAXCOUNT+1;
						Bitmap bitmap = Bitmap.createBitmap((int) sceneMap.getViewWidth(), MarkObject.ROWHEIGHT*countRow,
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
                    final int mLineInfoList_size = mLineInfoList.size();// Moved  mLineInfoList.size() call out of the loop to local variable mLineInfoList_size
                    for (int j = 0; j< mLineInfoList_size; j++){
						if(mLineInfoList.get(j).getLineid() == currentIndex){
							mStationInfoList = mLineInfoList.get(j).getStationInfoList();
							currentLineInfoText.setText(getSubwayShowText(mLineInfoList.get(j)));
							//currentLineInfoText.setText(mLineInfoList.get(j).getLinename()+"\n"+mLineInfoList.get(j).getLineinfo());
							currentLineInfoText.setBackgroundColor(mLineInfoList.get(j).colorid);
							break;
						}
					}
					int countRow = mStationInfoList.size()/LineMap.ROWMAXCOUNT+1;
					Bitmap bitmap = Bitmap.createBitmap((int) sceneMap.getViewWidth(), MarkObject.ROWHEIGHT*countRow,
							Bitmap.Config.ARGB_8888);
					bitmap.eraseColor(MainViewActivity.this.getResources().getColor(R.color.white));//填充颜色
					sceneMap.setBitmap(bitmap);
					sceneMap.postInvalidate();
					//sceneMap.draw();
					break;
            }

        }
    };

    private String getSubwayShowText(LineInfo mLineInfo) {
        String str = "";
        List<CityInfo> cityList = mDataHelper.QueryCityByCityNo(mLineInfo.getCityNo());
        if (cityList != null && cityList.size() > 0) {
            str += cityList.get(0).getCityName();
        }
        str += MainViewActivity.this.getResources().getString(R.string.subway) + mLineInfo.getLineid() +
                MainViewActivity.this.getResources().getString(R.string.subway_tail) +
                "(" + mLineInfo.getLinename() + ")" + "\n" + mLineInfo.getLineinfo();
        return str;
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
        isOncreate = true;
        if (CommonFuction.getSharedPreferencesBooleanValue(MainViewActivity.this, CommonFuction.ISREMINDER)) {
            start_location_reminder.setText(MainViewActivity.this.getResources().getString(R.string.stop_location));
        } else {
            start_location_reminder.setText(MainViewActivity.this.getResources().getString(R.string.start_location));
        }
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
                //text.setVisibility(View.GONE);
                sceneMap.setInitScale();
                Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.shenzhen);
                sceneMap.setBitmap(bitmap2);
            }
        } else if (v.getId() == R.id.button_location) {
            locationCurrentStation();
        } else if (v.getId() == R.id.start_location_reminder) {

            if (mRemonderLocationService != null) {
                if(!getPersimmions()){
                    //Toast.makeText(this,this.getString(R.string.please_set_permission),Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    mRemonderLocationService.startLocationService();
                }
                if (!CommonFuction.getSharedPreferencesBooleanValue(MainViewActivity.this, CommonFuction.ISREMINDER)) {
                    start_location_reminder.setText(MainViewActivity.this.getResources().getString(R.string.start_location));
                    mRemonderLocationService.setCancleReminder();
                    sceneMap.resetMardEndState();
                    canSetReminder = 0;
                } else if (canSetReminder >= 2) {
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
                if(!MainViewActivity.this.getPersimmions()){
                    //Toast.makeText(this,this.getString(R.string.please_set_permission),Toast.LENGTH_SHORT).show();
                    return;
                }else if (mRemonderLocationService != null){
                    mRemonderLocationService.startLocationService();
                }
                if (mRemonderLocationService != null && location != null) {
                    //CityInfo currentCityInfo = null;
                    LineInfo currentLineInfo = null;
                    StationInfo nerstStationInfo = null;
                    Log.d(TAG,"locationCurrentStation location.getCityCode() = "+location.getCityCode()+" lot = "+location.getLatitude()+" lat = "+location.getLongitude());
                    if (location != null) {
                        for (CityInfo cityInfo : cityInfoList) {
                            if (cityInfo.getCityNo().equals(location.getCityCode())) {
                                currentCityNo = cityInfo;
                                break;
                            }
                        }

                        Log.d(TAG,"locationCurrentStation currentCityInfo = "+currentCityNo);
                        //
                        double min = Double.MAX_VALUE;
                        double longitude = 0;
                        double latitude = 0;
                        double dis = 0;
                        if (currentCityNo != null) {
                            String shpno = CommonFuction.getSharedPreferencesValue(MainViewActivity.this, CommonFuction.CITYNO);
                            List<LineInfo> tempList = mLineInfoList;
                            if(!shpno.equals(""+currentCityNo)){
                                CommonFuction.writeSharedPreferences(MainViewActivity.this,CommonFuction.CITYNO,""+currentCityNo);
                                tempList = mDataHelper.getLineList(currentCityNo.getCityNo(), LineInfo.LINEID, "ASC");
                                if(tempList != null) {
                                    for (LineInfo lineInfo : tempList) {
                                        lineInfo.setStationInfoList(mDataHelper.QueryByStationLineNo(lineInfo.getLineid(), currentCityNo.getCityNo()));
                                    }
                                }
                            }
                            Log.d(TAG,"locationCurrentStation mLineInfoList = "+mLineInfoList.size());
                            if(mLineInfoList != null) {
                                for (LineInfo lineInfo:mLineInfoList) {
                                    //lineInfo.setStationInfoList(mDataHelper.QueryByStationLineNo(lineInfo.getLineid(), currentCityNo.getCityNo()));
                                   // Log.d(TAG, "locationCurrentStation lineid = "+lineInfo.lineid+" linename = " + lineInfo.linename);
                                    for (StationInfo stationInfo : lineInfo.getStationInfoList()) {
                                        longitude = CommonFuction.convertToDouble(stationInfo.getLot(), 0);
                                        latitude = CommonFuction.convertToDouble(stationInfo.getLat(), 0);
                                        dis = CommonFuction.getDistanceLat(longitude, latitude, location.getLongitude(),location.getLatitude());
                                        if (min > dis) {
                                            min = dis;
                                            nerstStationInfo = stationInfo;
                                            //currentStationInfo = stationInfo;
                                            //Log.d(TAG,"locationCurrentStation dis = "+dis+" lineid = "+stationInfo.lineid+" name= "+stationInfo.getCname());
                                            currentLineInfo = lineInfo;
                                        }
                                    }
                                }
                            }
                        }


                        Log.d(TAG,"locationCurrentStation currentStationInfo = "+nerstStationInfo);
                        if(nerstStationInfo != null){
                            CommonFuction.writeSharedPreferences(MainViewActivity.this,CommonFuction.INITCURRENTLINEID,nerstStationInfo.getCname());
                            Log.d(TAG,"locationCurrentStation lineid = "+nerstStationInfo.lineid+" name = "+nerstStationInfo.getCname());
                        }
                        if(nerstStationInfo != null){
                            if(nerstStationInfo.canTransfer()){
                                if(mSelectLineDialog != null){
                                    mSelectLineDialog.dismiss();
                                }
                                mSelectLineDialog = new SelectLineDialog(MainViewActivity.this,R.style.Dialog,new NoticeDialogListener() {
                                    @Override
                                    public void onClick(View view) {
                                        initData();
                                    }
                                    },nerstStationInfo,mLineInfoList);
                                mSelectLineDialog.show();
                            }else{
                                if(currentLineInfo != null){
                                    currentIndex = currentLineInfo.getLineid();
                                    CommonFuction.writeSharedPreferences(MainViewActivity.this,CommonFuction.CURRENTLINEID,""+currentIndex);
                                    initData();
                                }
                            }
                        }

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
    public void onStart() {
        super.onStart();
        RemonderLocationService.state = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        RemonderLocationService.state = false;
        if (mRemonderLocationService != null) {
            mRemonderLocationService.setNotification(true);
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
        minitDataThread = new initDataThread();
        minitDataThread.start();
    }

    class initDataThread extends Thread {
        @Override
        public void run() {
            cityInfoList = mDataHelper.getAllCityInfo();
            String shpno = CommonFuction.getSharedPreferencesValue(MainViewActivity.this, CommonFuction.CITYNO);
            if (!TextUtils.isEmpty(shpno)) {
                final int cityInfoList_size = cityInfoList.size();// Moved  cityInfoList.size() call out of the loop to local variable cityInfoList_size
                for (int n = 0; n < cityInfoList_size; n++) {
                    if (cityInfoList.get(n).getCityNo().equals(shpno)) {
                        currentCityNo = cityInfoList.get(n);
                        break;
                    }
                }
            }
            if (currentCityNo == null) {
                currentCityNo = cityInfoList.get(0);
            }
            mLineInfoList = mDataHelper.getLineList(currentCityNo.getCityNo(), LineInfo.LINEID, "ASC");
            Log.d(TAG, "currentCityNo = " + currentCityNo.getCityNo()+" mLineInfoList.size = "+mLineInfoList.size());
            final int mLineInfoList_size = mLineInfoList.size();// Moved  mLineInfoList.size() call out of the loop to local variable mLineInfoList_size
            for (int n = 0; n < mLineInfoList_size; n++) {
                mLineInfoList.get(n).setStationInfoList(mDataHelper.QueryByStationLineNo(mLineInfoList.get(n).getLineid(), currentCityNo.getCityNo()));
                for (int i = 0; i < mLineInfoList.get(n).getStationInfoList().size(); i++) {
                    mLineInfoList.get(n).getStationInfoList().get(i).colorId = mLineInfoList.get(n).colorid;
                }
            }
            currentIndex = CommonFuction.convertToInt(CommonFuction.getSharedPreferencesValue(MainViewActivity.this, CommonFuction.CURRENTLINEID),
                    mLineInfoList.get(0).getLineid());
            //单一线路-------------------------------------
            initLineMap(currentIndex);
            if (CommonFuction.getSharedPreferencesBooleanValue(MainViewActivity.this, CommonFuction.ISREMINDER)) {

                sceneMap.setMardStartState(true, CommonFuction.getSharedPreferencesValue(MainViewActivity.this, CommonFuction.STARTSTATIONNAME));
                sceneMap.setMardEndState(true, CommonFuction.getSharedPreferencesValue(MainViewActivity.this, CommonFuction.ENDSTATIONNAME));
                sceneMap.setCurrentStation(CommonFuction.getSharedPreferencesValue(MainViewActivity.this, CommonFuction.CURRENTSTATIONNAME));
            }
           /* else {
                if(mLineInfoList.size() >0){
                    currentIndex = mLineInfoList.get(0).getLineid();
                    //单一线路-------------------------------------
                    initLineMap(currentIndex);
                }
            }*/
            //isOncreate = false;

            Message msg = new Message();
            msg.what = 1;
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
        //Log.d("zxc", "disx = "+disx+this.getResources().getColor(lineColor[0]));
        float dis = this.getResources().getDimension(R.dimen.btn_size) + this.getResources().getDimension(R.dimen.magin_left);

        final int mLineInfoList_size = mLineInfoList.size();// Moved  mLineInfoList.size() call out of the loop to local variable mLineInfoList_size
        for (int n = 0; n < mLineInfoList_size; n++) {
            MarkObject markObject = new MarkObject();
            float row = n / (LineMapColor.ROWMAXCOUNT) + 1;
            float cloume = n % (LineMapColor.ROWMAXCOUNT);
            float x = cloume * disx - MarkObject.rectSizewidth + dis;
            float y = row * MarkObject.ROWHEIGHT - MarkObject.ROWHEIGHT / 2 - MarkObject.rectSizeHeight / 2;
            //Log.d("zxc", "row = "+row+" cloume = "+cloume+" x = "+x+" y = "+y);
            markObject.setLineid(mLineInfoList.get(n).getLineid());
            markObject.setX(x);
            markObject.setY(y);
            markObject.setName(mLineInfoList.get(n).getLinename());
            markObject.setCurrentsize(MarkObject.rectSizeHeight);
            markObject.setColorId(mLineInfoList.get(n).colorid);
            markObject.setmBitmap(drawNewColorBitmap(MarkObject.rectSizewidth, MarkObject.rectSizeHeight, markObject.getColorId()));
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
                                Message msg = new Message();
                                msg.what = 2;
                                mHandler.sendMessage(msg);
                            }
                        }.start();
                    }
                }
            });
            lineMap.addMark(markObject);
        }

        LinearLayout.LayoutParams linearParams1 = (LinearLayout.LayoutParams) btnLinearLayout.getLayoutParams(); //取控件textView当前的布局参数
        linearParams1.topMargin = (int) (MarkObject.rectSizeHeight * 1.5f);// 控件的高强制设成20

        linearParams1.leftMargin = (int) this.getResources().getDimension(R.dimen.magin_left);// 控件的宽强制设成30

        btnLinearLayout.setLayoutParams(linearParams1); //使设置好的布局参数应用到控件</pre>
    }

    private void initLineMap(int lineId) {

        sceneMap.setFullScree(false);
        sceneMap.clearMark();
        final int mLineInfoList_size = mLineInfoList.size();// Moved  mLineInfoList.size() call out of the loop to local variable mLineInfoList_size
        for (int j = 0; j < mLineInfoList_size; j++) {
            if (mLineInfoList.get(j).getLineid() == lineId) {
                mStationInfoList = mLineInfoList.get(j).getStationInfoList();
                break;
            }
        }
        int countRow = mStationInfoList.size() / LineMapView.ROWMAXCOUNT + extraRow;
        float initX = 1f / LineMapView.ROWMAXCOUNT / 2f;
        float initY = 1f / countRow / 2;

        String currentStationName = CommonFuction.getSharedPreferencesValue(MainViewActivity.this, CommonFuction.INITCURRENTLINEID);
        final int mStationInfoList_size = mStationInfoList.size();// Moved  mStationInfoList.size() call out of the loop to local variable mStationInfoList_size
        for (int n = 0; n < mStationInfoList_size; n++) {
            MarkObject markObject = new MarkObject();
            float row = n / (LineMapView.ROWMAXCOUNT) + extraRow;
            float cloume = n % (LineMapView.ROWMAXCOUNT);

            float x = cloume / (LineMap.ROWMAXCOUNT) + initX;
            float y = row / countRow - initY;
            //Log.d("zxc", "n = "+n+" row = "+row+" colume = "+cloume+" x = "+x+" y = "+y);
            markObject.setMapX(x);
            markObject.setMapY(y);
            markObject.mStationInfo = mStationInfoList.get(n);
            markObject.setName(mStationInfoList.get(n).getCname().split(" ")[0]);
            //Log.d(TAG,"initLineMap currentStationName = "+currentStationName+" markObject.getName() = "+markObject.getName());
            if(!TextUtils.isEmpty(currentStationName) && !TextUtils.isEmpty(markObject.getName()) &&
                    currentStationName.equals(markObject.getName())){
                markObject.isCurrentLocationStation = true;
            }
            markObject.setColorId(mStationInfoList.get(n).colorId);
            markObject.setRadius(MarkObject.size / 3);
            markObject.setCurrentsize(MarkObject.size + MarkObject.DIFF);
            boolean canTransfer = markObject.mStationInfo.canTransfer();
            if (canTransfer) {
                markObject.setmBitmap(CommonFuction.getbitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.cm_route_map_pin_dottransfer), MarkObject.size + MarkObject.DIFF, MarkObject.size + MarkObject.DIFF));
            } else {
                markObject.setmBitmap(drawNewBitmap(MarkObject.size / 3, MarkObject.size + MarkObject.DIFF, MarkObject.size + MarkObject.DIFF, markObject.getColorId(), canTransfer));
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

    }

    /**
     * 往图片上写入文字、图片等内容
     */
    private Bitmap drawNewBitmap(float radius, int width, int hight, int colorId, boolean canTransfer) {
        Bitmap bitmap = Bitmap.createBitmap(width, hight, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(this.getResources().getColor(R.color.white));
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(colorId);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(radius / 2);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(width / 2, hight / 2, radius, paint);
        if (canTransfer) {
            paint.setStrokeWidth(2);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(width / 2, hight / 2, radius / 2, paint);
        }
        return bitmap;
    }

    /**
     * 往图片上写入文字、图片等内容
     */
    private Bitmap drawNewColorBitmap(int width, int hight, int colorId) {
        Bitmap bitmap = Bitmap.createBitmap(width, hight, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(colorId);
        return bitmap;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    public void dbWriteFinishNotif() {
        initData();
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CommonFuction.writeBooleanSharedPreferences(this,CommonFuction.ISREMINDER,false);
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

    SettingReminderDialog mSettingReminderDialog;

    private void showDialog(final MarkObject markObject) {
        List<ExitInfo> existInfoList = mDataHelper.QueryByExitInfoCname(markObject.mStationInfo.getCname(), currentCityNo.getCityNo());
        String existInfostr = "";
        if (existInfoList != null) {
            final int existInfoList_size = existInfoList.size();// Moved  existInfoList.size() call out of the loop to local variable existInfoList_size
            for (int n = 0; n < existInfoList_size; n++) {
                existInfostr += existInfoList.get(n).getExitname() + " " + existInfoList.get(n).getAddr() + "\n";
            }
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
                            break;
                        case R.id.end:
                            mSettingReminderDialog.dismiss();
                            sceneMap.setMardEndState(true, markObject.mStationInfo.getCname());
                            sceneMap.postInvalidate();
                            if (mRemonderLocationService != null) {
                                mRemonderLocationService.setEndStation(markObject.mStationInfo);
                            }
                            canSetReminder++;
                            break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, markObject.mStationInfo.getTransfer(), existInfostr,
                "" + markObject.mStationInfo.getLineid(), currentCityNo.getCityName(), markObject.mStationInfo.getCname());
        mSettingReminderDialog.setContentView(R.layout.setting_reminder_dialog);
        Window window = mSettingReminderDialog.getWindow();
        window.setGravity(Gravity.CENTER);
        // window.setWindowAnimations(R.style.dialog_animation);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        mSettingReminderDialog.show();
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
                if (mRemonderLocationService != null){
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
                });
                locationCurrentStation();
            }

        }

    };
}
