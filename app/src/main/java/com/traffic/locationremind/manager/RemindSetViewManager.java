package com.traffic.locationremind.manager;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.listener.GoToFragmentListener;
import com.traffic.locationremind.baidu.location.listener.RemindSetViewListener;
import com.traffic.locationremind.baidu.location.object.MarkObject;
import com.traffic.locationremind.baidu.location.view.SelectlineMap;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataManager;

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
    private String lineTial = "";
    private SelectlineMap mSelectlineMap;

    private GoToFragmentListener mGoToFragmentListener;

    private DataManager dataManager;
    public RemindSetViewManager(){

    }

    public void setGoToFragmentListener(GoToFragmentListener mGoToFragmentListener){
        this.mGoToFragmentListener = mGoToFragmentListener;
    }

    public void initView(Activity activity,DataManager dataManager) {
        this.dataManager = dataManager;
        set_remind_layout = (ViewGroup)activity.findViewById(R.id.set_remind_layout);
        //listView = (ListView) activity.findViewById(R.id.list_view);
        changeNumber = (TextView) activity.findViewById(R.id.change_text);
        start = (TextView) activity.findViewById(R.id.start);
        end = (TextView) activity.findViewById(R.id.end);
        collectionBtn = (TextView) activity.findViewById(R.id.collection_btn);
        setRemindBtn = (TextView) activity.findViewById(R.id.set_remind_btn);
        back_btn = (ImageView)  activity.findViewById(R.id.back_btn);

        mSelectlineMap = (SelectlineMap) activity.findViewById(R.id.item_tv_2);

        lineTial = activity.getResources().getString(R.string.line_tail);

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
                    mGoToFragmentListener.openRemindFragment(mSelectlineMap.getDataList());
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

    public boolean getRemindWindowState(){
        if(set_remind_layout.getVisibility() == View.VISIBLE)
            return true;
        return false;
    }

    public void closeRemindWindow(){
        set_remind_layout.setVisibility(View.GONE);
    }

    @Override
    public void openSetWindow(Map.Entry<List<Integer>,List<StationInfo>> lastLines){
        if(lastLines != null){
            //mRemindLineAdapter.setData(lastLines);
            start.setText(lastLines.getValue().get(0).getCname());
            end.setText(lastLines.getValue().get(lastLines.getValue().size()-1).getCname());
            int size = lastLines.getKey().size();
            StringBuffer str = new StringBuffer();
            for(int i = 0 ; i < size ; i++){
                if(i < size -1){
                    str.append(String.format(lineTial,lastLines.getKey().get(i))+"->");
                }else{
                    str.append(String.format(lineTial,lastLines.getKey().get(i)));
                }
            }
            changeNumber.setText(str.toString());
            set_remind_layout.setVisibility(View.VISIBLE);
            mSelectlineMap.setStationList(lastLines.getValue());
            mSelectlineMap.setLineInfoList(dataManager.getLineInfoList());
        }
    }

    @Override
    public void closeSetWindow(){

    }


}
