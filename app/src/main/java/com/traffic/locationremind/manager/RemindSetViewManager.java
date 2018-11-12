package com.traffic.locationremind.manager;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.activity.MainActivity;
import com.traffic.locationremind.baidu.location.listener.GoToFragmentListener;
import com.traffic.locationremind.baidu.location.listener.RemindSetViewListener;
import com.traffic.locationremind.baidu.location.object.LineObject;
import com.traffic.locationremind.baidu.location.object.MarkObject;
import com.traffic.locationremind.baidu.location.pagerbottomtabstrip.PageNavigationView;
import com.traffic.locationremind.baidu.location.view.SelectlineMap;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.common.util.ToastUitl;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RemindSetViewManager implements RemindSetViewListener {

    private final static String TAG = "RemindSetViewManager";

    private ViewGroup set_remind_layout;
    private ImageView back_btn;
    private TextView changeNumber;
    private TextView start;
    private TextView end;
    private TextView collectionBtn;
    private TextView setRemindBtn;
    //private String lineTial = "";
    private SelectlineMap mSelectlineMap;
    private GoToFragmentListener mGoToFragmentListener;
    private PageNavigationView pageBottomTabLayout;
    private Activity activity;
    private DataManager dataManager;
    private ViewGroup serachLayoutRoot;
    private LineObject lastLines;
    public RemindSetViewManager(){

    }

    public void setGoToFragmentListener(GoToFragmentListener mGoToFragmentListener){
        this.mGoToFragmentListener = mGoToFragmentListener;
    }

    public void initView(final MainActivity activity, DataManager dataManager) {
        this.activity = activity;
        this.dataManager = dataManager;
        pageBottomTabLayout = (PageNavigationView) activity.findViewById(R.id.tab);
        set_remind_layout = (ViewGroup)activity.findViewById(R.id.set_remind_layout);
        //listView = (ListView) activity.findViewById(R.id.list_view);
        changeNumber = (TextView) activity.findViewById(R.id.change_text);
        start = (TextView) activity.findViewById(R.id.start);
        end = (TextView) activity.findViewById(R.id.end);
        collectionBtn = (TextView) activity.findViewById(R.id.collection_btn);
        setRemindBtn = (TextView) activity.findViewById(R.id.set_remind_btn);
        back_btn = (ImageView)  activity.findViewById(R.id.back_btn);

        mSelectlineMap = (SelectlineMap) activity.findViewById(R.id.item_tv_2);

        //lineTial = activity.getResources().getString(R.string.line_tail);
        serachLayoutRoot = (ViewGroup) activity.findViewById(R.id.serach_layout_manager_root);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeRemindWindow();
            }
        });

        setRemindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mGoToFragmentListener != null){
                    if(!activity.getPersimmions()){
                        ToastUitl.showText(activity,activity.getString(R.string.hint_open_network_gps));
                    }
                    mGoToFragmentListener.openRemindFragment(lastLines);
                }
            }
        });

        collectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lineStr = CommonFuction.convertStationToString(mSelectlineMap.getDataList());
                String allFavoriteLine = CommonFuction.getSharedPreferencesValue(activity,CommonFuction.FAVOURITE);
                Log.d(TAG,"lineStr = "+lineStr+" allFavoriteLine = "+allFavoriteLine);
                if(allFavoriteLine.contains(lineStr)){
                    String string[] = allFavoriteLine.split(CommonFuction.TRANSFER_SPLIT);
                    StringBuffer newLine = new StringBuffer();
                    int size = string.length;
                    for(int i = 0;i < size;i++){
                        if(!lineStr.equals(string[i])) {
                            newLine.append(string[i] + CommonFuction.TRANSFER_SPLIT);
                        }
                    }
                    setCompoundDrawables(activity.getResources().getDrawable(R.drawable.locationbar_fav_btn));
                    CommonFuction.writeSharedPreferences(activity,CommonFuction.FAVOURITE,newLine.toString());
                    Log.d(TAG,"remove newLine = "+newLine);
                }else{
                    String allFavoriteLines = CommonFuction.getSharedPreferencesValue(activity,CommonFuction.FAVOURITE);
                    StringBuffer newLine = new StringBuffer();
                    newLine.append(allFavoriteLines+CommonFuction.TRANSFER_SPLIT+lineStr);
                    CommonFuction.writeSharedPreferences(activity,CommonFuction.FAVOURITE,newLine.toString());
                    setCompoundDrawables(activity.getResources().getDrawable(R.drawable.saveas_fav_btn));
                    Log.d(TAG,"add newLine = "+newLine);
                }
            }
        });

        set_remind_layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

    }

    public void setCompoundDrawables(Drawable drawable){
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), (int) (drawable.getMinimumHeight()));
        collectionBtn.setCompoundDrawables(null,drawable,null,null);
    }

    public boolean getRemindWindowState(){
        if(set_remind_layout.getVisibility() == View.VISIBLE)
            return true;
        return false;
    }

    public void closeRemindWindow(){
        set_remind_layout.setVisibility(View.GONE);
        if(serachLayoutRoot.getVisibility() != View.VISIBLE) {
            pageBottomTabLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void openSetWindow(LineObject lastLines){
        if(lastLines != null){
            this.lastLines = lastLines;
            //mRemindLineAdapter.setData(lastLines);
            start.setText(lastLines.stationList.get(0).getCname());
            end.setText(lastLines.stationList.get(lastLines.stationList.size()-1).getCname());
            int size = lastLines.lineidList.size();
            StringBuffer str = new StringBuffer();
            for(int i = 0 ; i < size ; i++){
                if(i < size -1){
                    str.append(CommonFuction.getLineNo(dataManager.getLineInfoList().get(lastLines.lineidList.get(i)).linename)[0]+"->");
                }else{
                    str.append(CommonFuction.getLineNo(dataManager.getLineInfoList().get(lastLines.lineidList.get(i)).linename)[0]);
                }
            }
            changeNumber.setText(str.toString());
            set_remind_layout.setVisibility(View.VISIBLE);
            mSelectlineMap.setStationList(lastLines);
            mSelectlineMap.setLineInfoList(dataManager.getLineInfoList());
            String lineStr = CommonFuction.convertStationToString(lastLines);
            String allFavoriteLine = CommonFuction.getSharedPreferencesValue(activity,CommonFuction.FAVOURITE);
            if(allFavoriteLine.contains(lineStr)){
                setCompoundDrawables(activity.getResources().getDrawable(R.drawable.saveas_fav_btn));
            }else{
                setCompoundDrawables(activity.getResources().getDrawable(R.drawable.locationbar_fav_btn));
            }
            pageBottomTabLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void closeSetWindow(){

    }


}
