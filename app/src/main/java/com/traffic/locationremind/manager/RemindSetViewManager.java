package com.traffic.locationremind.manager;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.adapter.RemindLineAdapter;
import com.traffic.locationremind.baidu.location.listener.RemindSetViewListener;
import com.traffic.locationremind.baidu.location.view.SelectlineMap;
import com.traffic.locationremind.manager.bean.StationInfo;

import java.util.List;
import java.util.Map;


public class RemindSetViewManager implements RemindSetViewListener {

    private final static String TAG = "RemindSetViewManager";
    //private RemindLineAdapter mRemindLineAdapter;
    private ViewGroup set_remind_layout;
    //private ListView listView;
    private TextView changeNumber;
    private TextView start;
    private TextView end;
    private TextView collectionBtn;
    private TextView setRemindBtn;
    private String lineTial = "";
    private SelectlineMap mSelectlineMap;

    public RemindSetViewManager(){

    }

    public void initView(Activity activity) {
        set_remind_layout = (ViewGroup)activity.findViewById(R.id.set_remind_layout);
        //listView = (ListView) activity.findViewById(R.id.list_view);
        changeNumber = (TextView) activity.findViewById(R.id.change_text);
        start = (TextView) activity.findViewById(R.id.start);
        end = (TextView) activity.findViewById(R.id.end);
        collectionBtn = (TextView) activity.findViewById(R.id.collection_btn);
        setRemindBtn = (TextView) activity.findViewById(R.id.set_remind_btn);

        mSelectlineMap = (SelectlineMap) activity.findViewById(R.id.item_tv_2);

        lineTial = activity.getResources().getString(R.string.line_tail);
       // mRemindLineAdapter = new RemindLineAdapter(activity);
       // listView.setAdapter(mRemindLineAdapter);

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
        }

    }

    @Override
    public void closeSetWindow(){

    }


}
