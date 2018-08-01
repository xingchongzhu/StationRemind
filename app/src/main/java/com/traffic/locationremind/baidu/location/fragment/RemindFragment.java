package com.traffic.locationremind.baidu.location.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.activity.MainActivity;
import com.traffic.locationremind.baidu.location.listener.GoToFragmentListener;
import com.traffic.locationremind.baidu.location.view.LineNodeView;
import com.traffic.locationremind.common.util.ToastUitl;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataManager;

import java.util.ArrayList;
import java.util.List;

public class RemindFragment extends Fragment{

    private LinearLayout linearlayout;
    private HorizontalScrollView horizontalScrollView;
    private List<StationInfo> list = null;
    private List<LineNodeView> textViewList = new ArrayList<>();
    private int lineNodeWidth = 0;
    private int lineNodeHeight = 0;
    private DataManager mDataManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.remind_layout,container,false);
    }

    public void setData(List<StationInfo> list){
        this.list = list;
        for(StationInfo stationInfo:list){
            stationInfo.colorId = mDataManager.getLineInfoList().get(stationInfo.lineid).colorid;
        }
        createLine(list);
    }

    public void createLine(List<StationInfo> list){
        linearlayout.removeAllViews();
        for (int i = 0; i < list.size(); i++) {
            //创建textview
            LineNodeView textView = new LineNodeView(getActivity());
            textView.setId(i+1000);

            textView.setStation(list.get(i));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(lineNodeWidth, lineNodeHeight);
            linearlayout.addView(textView,layoutParams);
            //添加到集合
            textViewList.add(textView);
        }
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        linearlayout = (LinearLayout) view.findViewById(R.id.linearlayout);
        horizontalScrollView = (HorizontalScrollView) view.findViewById(R.id.horizontalScrollView);
        lineNodeWidth = (int)getResources().getDimension(R.dimen.line_node_width);
        lineNodeHeight = (int)getResources().getDimension(R.dimen.line_node_height);
        mDataManager = ((MainActivity)getActivity()).getDataManager();
    }

}
