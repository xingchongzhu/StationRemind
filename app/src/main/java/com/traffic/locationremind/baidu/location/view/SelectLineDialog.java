package com.traffic.locationremind.baidu.location.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.dialog.SettingReminderDialog;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class SelectLineDialog extends Dialog {
    Context context;
    private StationInfo station;
    private Map<Integer, LineInfo> tempList;
    private List<String> selectList = new ArrayList<>();
    private SettingReminderDialog.NoticeDialogListener listener;


    public SelectLineDialog(Context context, int theme, SettingReminderDialog.NoticeDialogListener listener, StationInfo station, Map<Integer, LineInfo> tempList) {
        super(context, theme);
        this.context = context;
        this.station = station;
        this.tempList = tempList;
        this.listener = listener;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_line_dialog);
        TextView stationTextView = (TextView) findViewById(R.id.station);
        ListView listView = (ListView) findViewById(R.id.listview);
        if (station != null) {
            String transfer = station.getTransfer();
            String allLines = "";
            if (station.canTransfer()) {
                final String lines[] = transfer.split(CommonFuction.TRANSFER_SPLIT);
                final int size = lines.length;
                //String lineTail = context.getResources().getString(R.string.line_tail);
                for (int i = 0; i < size; i++) {
                    LineInfo lineInfo = tempList.get(CommonFuction.convertToInt(lines[i], 0));
                    if (lineInfo != null) {
                        List<StationInfo> tmpStationList = lineInfo.getStationInfoList();
                        for (StationInfo stationInfo : tmpStationList) {
                            if (stationInfo.getCname().equals(station.getCname())) {
                                allLines +=  CommonFuction.getLineNo(DataManager.getInstance(context).getLineInfoList().get(lineInfo.lineid).linename)[0]+ "  ";
                                selectList.add(CommonFuction.getLineNo(DataManager.getInstance(context).getLineInfoList().get(lineInfo.lineid).linename)[0] + " ->" +
                                        lineInfo.linename + " :" + lineInfo.lineinfo);
                                break;
                            }
                        }
                    }
                }
                stationTextView.setText(station.getCname() + "(" + allLines + ")");
                String[] strArr = new String[selectList.size()];
                selectList.toArray(strArr);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.item, R.id.tv_name, strArr);
                //设置数据适配器 要会触类旁通
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (lines.length > position) {
                            CommonFuction.writeSharedPreferences(context, CommonFuction.CURRENTLINEID, lines[position]);
                            notificationListern(null);
                            dismiss();
                        }
                    }
                });

            }

        }
        setCanceledOnTouchOutside(true);
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = d.getWidth();
        getWindow().setAttributes(p);
    }

    public void notificationListern(View v) {
        if (listener != null) {
            listener.onClick(v);
        }
    }
}