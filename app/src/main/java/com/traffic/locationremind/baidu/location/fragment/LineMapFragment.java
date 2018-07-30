package com.traffic.locationremind.baidu.location.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.view.GifView;
import com.traffic.locationremind.baidu.location.view.LineMapColorView;
import com.traffic.locationremind.baidu.location.view.LineMapView;
import com.traffic.locationremind.common.util.ReadExcelDataUtil;

public class LineMapFragment extends Fragment implements ReadExcelDataUtil.DbWriteFinishListener, View.OnClickListener{

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
    private Button start_location_reminder;
    private TextView currentLineInfoText;
    private TextView hintText;

    private Activity activity;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = getActivity();
        return inflater.inflate(R.layout.line_map_layout,container,false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        sceneMap = (LineMapView) activity.findViewById(R.id.sceneMap);
        currentLineInfoText = (TextView) activity.findViewById(R.id.text);
        lineMap = (LineMapColorView) activity.findViewById(R.id.lineMap);
        scaleMorebtn = (ImageView) activity.findViewById(R.id.button_in);
        scaleLessbtn = (ImageView) activity.findViewById(R.id.button_out);
        button_location = (ImageView) activity.findViewById(R.id.button_location);

        screenbtn = (Button) activity.findViewById(R.id.full_screen);
        start_location_reminder = (Button) activity.findViewById(R.id.start_location_reminder);
        gif = (GifView) activity.findViewById(R.id.gif);
        hintText = (TextView) activity.findViewById(R.id.hint);
        btnLinearLayout = (LinearLayout) activity.findViewById(R.id.mainmap_zoom_area);

        screenbtn.setOnClickListener(this);
        scaleMorebtn.setOnClickListener(this);
        scaleLessbtn.setOnClickListener(this);
        button_location.setOnClickListener(this);
        start_location_reminder.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

    }

    public void dbWriteFinishNotif() {

    }
}
