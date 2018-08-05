package com.traffic.locationremind.baidu.location.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.activity.MainActivity;
import com.traffic.locationremind.baidu.location.adapter.AllLineAdapter;
import com.traffic.locationremind.baidu.location.adapter.ColorLineAdapter;
import com.traffic.locationremind.common.util.ReadExcelDataUtil;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataManager;

import java.util.*;

public class LineMapFragment extends Fragment implements ReadExcelDataUtil.DbWriteFinishListener, View.OnClickListener{

    private final static String TAG = "LineMapFragment";

    private final static int INITMAPCOLOR = 1;//初始化当前城市地铁显示
    private final static int SHOWCURRENTLINED = 2;//当前选择路线
    private final static int STARTLOCATION = 3;//开始定位
    private GridView sceneMap;
    private GridView lineMap;
    private ImageView scaleMorebtn;
    private ImageView scaleLessbtn;
    private ImageView button_location;
    private TextView currentLineInfoText;

    private AllLineAdapter sceneMapAdapter;
    private ColorLineAdapter colorLineAdapter;
    private View rootView;
    private DataManager mDataManager;
    private List<LineInfo> list = new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView");
        if (null != rootView) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent) {
                parent.removeView(rootView);
            }
        } else {
            rootView = inflater.inflate(R.layout.line_map_layout,container,false);
            initView(rootView);// 控件初始化
        }
        return rootView;
    }
    private void initView(View rootView){
        sceneMap = (GridView) rootView.findViewById(R.id.sceneMap);
        currentLineInfoText = (TextView) rootView.findViewById(R.id.text);
        lineMap = (GridView) rootView.findViewById(R.id.lineMap);

        mDataManager = ((MainActivity)getActivity()).getDataManager();

        colorLineAdapter = new ColorLineAdapter(this.getActivity());
        lineMap.setAdapter(colorLineAdapter);

        sceneMapAdapter = new AllLineAdapter(this.getActivity());
        sceneMap.setAdapter(sceneMapAdapter);

        lineMap.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setCurrentLine(position);
            }
        });
    }

    public void upadaData(){
        if(mDataManager.getLineInfoList() != null){
            list.clear();
            for(Map.Entry<Integer,LineInfo> entry:mDataManager.getLineInfoList().entrySet()){
                list.add(entry.getValue());
            }
        }

        Collections.sort(list, new Comparator<LineInfo>() {
                @Override
                public int compare(LineInfo o1, LineInfo o2) {
                   if(o1.getLineid() < o1.getLineid()){
                       return -1;
                   }else if(o1.getLineid() == o1.getLineid()){
                       return 0;
                   }else{
                       return 1;
                   }
                }
            });
        setCurrentLine(0);
        colorLineAdapter.setData(list);

    }

    private void setCurrentLine(int index){
        if(index >= list.size()){
            return;
        }
        String string = String.format(getResources().getString(R.string.line_tail),list.get(index).lineid+"")+" "+list.get(index).linename+" ("+list.get(index).getForwad()+","+list.get(index).getReverse()+")\n"+
                list.get(index).getLineinfo();
        currentLineInfoText.setText(string);
        currentLineInfoText.setBackgroundColor(list.get(index).colorid);
        sceneMapAdapter.setData(mDataManager.getLineInfoList().get(list.get(index).lineid));

        int height = (int)getActivity().getResources().getDimension(R.dimen.count_line_node_rect_height)*list.get(index).getStationInfoList().size()/5+1;
        ViewGroup.LayoutParams linearParams = sceneMap.getLayoutParams();
        linearParams.height = height;
        sceneMap.setLayoutParams(linearParams); //使设置好的布局参数应用到控件
    }

    @Override
    public void onClick(View v) {

    }

    public void dbWriteFinishNotif() {

    }
}
