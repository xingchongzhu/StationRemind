package com.traffic.locationremind.baidu.location.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.fragment.RemindFragment;
import com.traffic.locationremind.baidu.location.object.LineObject;
import com.traffic.locationremind.baidu.location.search.util.CommonAdapter;
import com.traffic.locationremind.baidu.location.search.util.ViewHolder;
import com.traffic.locationremind.baidu.location.view.SelectlineMap;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.manager.bean.StationInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavouriteAdapter extends CommonAdapter<LineObject> {
    String TAG = "FavouriteAdapter";

    String lineTail = "";
    String lineChange = "";
    String staionsCNumber = "";
    String startStation = "";
    String endstation = "";
    RemindFragment fragment;

    public FavouriteAdapter(RemindFragment fragment, List<LineObject> data, int layoutId) {
        super(fragment.getActivity(), data, layoutId);
        this.fragment = fragment;
        lineChange = fragment.getResources().getString(R.string.line_tail);
        lineTail = fragment.getResources().getString(R.string.change_number);
        staionsCNumber = fragment.getResources().getString(R.string.station_number);
        startStation = fragment.getResources().getString(R.string.start_station);
        endstation = fragment.getResources().getString(R.string.end_station);
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
    public void convert(ViewHolder holder, final int position) {
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
        if (data.stationList.size() <= 0) {
            return;
        }

        String str = startStation + data.stationList.get(0).getCname() + "  " + endstation + data.stationList.get(data.stationList.size() - 1).getCname();

        holder.setText(R.id.change_numner, String.format(lineTail, data.lineidList.size() + ""))
                .setText(R.id.change_lineid, change.toString())
                .setText(R.id.station_number, String.format(staionsCNumber, data.stationList.size() + "") + "")
                .setText(R.id.station_start_end, str)
                .setVisable(R.id.btn_layout, View.VISIBLE)
                .setOnClickListener(R.id.delete, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String lineStr = CommonFuction.convertStationToString(mData.get(position).stationList);
                        String allFavoriteLine = CommonFuction.getSharedPreferencesValue(fragment.getActivity(),CommonFuction.FAVOURITE);
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
                            CommonFuction.writeSharedPreferences(fragment.getActivity(),CommonFuction.FAVOURITE,newLine.toString());
                            Log.d(TAG,"remove newLine = "+newLine);
                        }
                        mData.remove(position);
                        notifyDataSetChanged();
                    }
                })
                .setOnClickListener(R.id.set_remind, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fragment.setData(mData.get(position).stationList);
                    }
                });
    }
}
