package com.traffic.locationremind.baidu.location.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.object.LineObject;
import com.traffic.locationremind.baidu.location.search.util.CommonAdapter;
import com.traffic.locationremind.baidu.location.search.util.ViewHolder;
import com.traffic.locationremind.baidu.location.view.SelectlineMap;
import com.traffic.locationremind.manager.bean.StationInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavouriteAdapter extends CommonAdapter<LineObject> {


    String lineTail = "";
    String lineChange = "";
    String staionsCNumber = "";
    String startStation = "";
    String endstation = "";

    public FavouriteAdapter(Context context,List<LineObject> data, int layoutId) {
        super(context,data, layoutId);
        lineChange = context.getResources().getString(R.string.line_tail);
        lineTail = context.getResources().getString(R.string.change_number);
        staionsCNumber = context.getResources().getString(R.string.station_number);
        startStation = context.getResources().getString(R.string.start_station);
        endstation = context.getResources().getString(R.string.end_station);
    }

    public void clearData() {
        this.mData.clear();
        notifyDataSetChanged();
    }

    public void setData(List<LineObject> data) {
        this.mData = data;
        notifyDataSetChanged();
    }


    @Override
    public LineObject getItem(int i) {
        return super.getItem(i);
    }

    @Override
    public void convert(ViewHolder holder, int position) {
        LineObject data = mData.get(position);
        StringBuffer change = new StringBuffer();
        int n = 0;
        for (Integer i : data.lineidList) {
            if (n < data.lineidList.size() - 1)
                change.append(String.format(lineChange, i + "") + "->");
            else
                change.append(String.format(lineChange, i + ""));
            n++;
        }
        if(data.stationList.size() <= 0){
            return;
        }

        String str = startStation + data.stationList.get(0).getCname() + "  " + endstation + data.stationList.get(data.stationList.size() - 1).getCname();

        holder.setText(R.id.change_numner, String.format(lineTail, data.lineidList.size() + ""))
                .setText(R.id.change_lineid, change.toString())
                .setText(R.id.station_number, String.format(staionsCNumber, data.stationList.size() + "") + "")
                .setText(R.id.station_start_end, str);
    }
}
