package com.traffic.locationremind.baidu.location.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.search.adapter.GridViewAdapter;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.database.DataManager;

import java.util.ArrayList;
import java.util.List;


public class SettingReminderDialog extends Dialog implements OnClickListener{
    Context context;
    private String transferInfo;
    private String exitInfo;
    private int lineId;
    private String station;
    private NoticeDialogListener listener;
    GridViewAdapter mGridViewAdapter;
    //对话框事件监听接口，用于处理回调点击事件
    public interface NoticeDialogListener {
        void onClick(View view);
        void selectLine(int lineid);
    }

    public SettingReminderDialog(Context context, int theme, NoticeDialogListener listener
            , String transferInfo, String exitInfo, String lineId, String city, String station){
        super(context, theme);
        this.context = context;
        this.listener = listener;
        this.transferInfo = transferInfo;
        this.exitInfo = exitInfo;
        this.station = station;
        this.lineId = CommonFuction.convertToInt(lineId,0);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView title = (TextView) findViewById(R.id.title);
        GridView transfer = (GridView) findViewById(R.id.transfer);
        TextView exit_info = (TextView) findViewById(R.id.exit_info);
        TextView transferTitle = (TextView) findViewById(R.id.transfer_title);

        Button start = (Button) findViewById(R.id.start);
        Button end = (Button) findViewById(R.id.end);
        final List<String> list = new ArrayList<>();
        final List<Integer> listLineid = new ArrayList<>();
        String[] st = transferInfo.split(",");
        for(int i = 0;i < st.length;i++){
            int num = CommonFuction.convertToInt(st[i],0);
            LineInfo lineInfo= DataManager.getInstance(context).getLineInfoList().get(num);
            if(lineInfo != null) {
                listLineid.add(num);
                list.add(lineInfo.linename);
            }
        }
        if(list.size() <= 0){
            transfer.setVisibility(View.GONE);
            transferTitle.setVisibility(View.GONE);
        }
        mGridViewAdapter = new GridViewAdapter(context, list);
        title.setText(convertString());
        transfer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.selectLine(listLineid.get(position));
            }
        });
        transfer.setAdapter(mGridViewAdapter);
        //transfer.setText(transferInfo);
        exit_info.setText(exitInfo);

        start.setOnClickListener(this);
        end.setOnClickListener(this);
        setCanceledOnTouchOutside(true);
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = d.getWidth();
        getWindow().setAttributes(p);
    }
    private String convertString() {
        return CommonFuction.getLineNo(DataManager.getInstance(context).getLineInfoList().get(lineId).linename)[0]
                +" " + station;
    }
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        listener.onClick(v);
    }
}