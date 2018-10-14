package com.traffic.locationremind.baidu.location.search.adapter;

import android.content.Context;
import android.util.Log;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.object.LineObject;
import com.traffic.locationremind.baidu.location.search.util.CommonAdapter;
import com.traffic.locationremind.baidu.location.search.util.ViewHolder;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataManager;

import java.util.List;
import java.util.Map;

/**
 * Created by yetwish on 2015-05-11
 */

public class CardAdapter extends CommonAdapter<LineObject> {


    //String lineTail = "";
    String lineChange = "";
    String staionsCNumber = "";
    String startStation = "";
    String endstation = "";
    DataManager dataManager;
    Object object = new Object();
    public CardAdapter(Context context, List<LineObject> data, int layoutId) {
        super(context, data, layoutId);
        dataManager = DataManager.getInstance(context);
        //lineTail = context.getResources().getString(R.string.line_tail);
        lineChange = context.getResources().getString(R.string.change_number);
        staionsCNumber = context.getResources().getString(R.string.station_number);
        startStation = context.getResources().getString(R.string.start_station);
        endstation =context.getResources().getString(R.string.end_station);
    }

    public void setData(List<LineObject> data) {
        synchronized (object) {
            //Log.d("zxc", "-------------------------------------------");
            this.mData = data;
            if(data != null) {
                StringBuffer str = new StringBuffer();
                for (LineObject entry : data) {
                    for (StationInfo stationInfo : entry.stationList) {
                        str.append("" + stationInfo.lineid + " " + stationInfo.getCname() + " ->");
                    }
                    //Log.d("zxc", entry.getKey() + "---- " + str.toString());
                }

            }
            notifyDataSetChanged();
        }
    }

    @Override
    public LineObject getItem(int i) {
        return super.getItem(i);
    }

    @Override
    public void convert(ViewHolder holder, int position) {
        synchronized (object) {
            if (mData.size() <= 0) {
                return;
            }
            LineObject data = mData.get(position);
            StringBuffer change = new StringBuffer();
            int n = 0;
            for (Integer i : data.lineidList) {
                if(dataManager.getLineInfoList().get(i) != null) {
                    if (n < data.lineidList.size() - 1) {
                        change.append(CommonFuction.getLineNo(dataManager.getLineInfoList().get(i).linename)[0] + "->");
                    } else {
                        change.append(CommonFuction.getLineNo(dataManager.getLineInfoList().get(i).linename)[0]);
                    }
                }
                n++;
            }

            String str = "";

            if(data.lineidList.size() >1 ) {
                holder.setText(R.id.change_numner, String.format(lineChange, data.lineidList.size() - 1 + ""));
            }
            if(data.stationList.size() > 0) {
                str = startStation + data.stationList.get(0).getCname() + "  " + endstation + data.stationList.get(data.stationList.size() - 1).getCname();
            }
            holder.setText(R.id.change_lineid, change.toString())
                    .setText(R.id.station_number, String.format(staionsCNumber, data.stationList.size() + "") + "")
                    .setText(R.id.station_start_end, str);
        }
    }
}
