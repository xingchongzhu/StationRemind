package com.traffic.locationremind.baidu.location.search.adapter;

import android.content.Context;
import com.traffic.location.remind.R;
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

public class CardAdapter extends CommonAdapter<Map.Entry<List<Integer>,List<StationInfo>>> {


    //String lineTail = "";
    String lineChange = "";
    String staionsCNumber = "";
    String startStation = "";
    String endstation = "";
    DataManager dataManager;
    public CardAdapter(Context context, List<Map.Entry<List<Integer>,List<StationInfo>>> data, int layoutId) {
        super(context, data, layoutId);
        dataManager = DataManager.getInstance(context);
        //lineTail = context.getResources().getString(R.string.line_tail);
        lineChange = context.getResources().getString(R.string.change_number);
        staionsCNumber = context.getResources().getString(R.string.station_number);
        startStation = context.getResources().getString(R.string.start_station);
        endstation =context.getResources().getString(R.string.end_station);
    }

    public void clearData() {
        this.mData.clear();
        notifyDataSetChanged();
    }

    public void setData(List<Map.Entry<List<Integer>,List<StationInfo>>> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    public void updateData(List<Map.Entry<List<Integer>,List<StationInfo>>> data) {
        this.mData = data;
    }

    @Override
    public Map.Entry<List<Integer>,List<StationInfo>> getItem(int i) {
        return super.getItem(i);
    }

    @Override
    public void convert(ViewHolder holder, int position) {
        Map.Entry<List<Integer>,List<StationInfo>> data = mData.get(position);
        StringBuffer change = new StringBuffer();
        int n = 0;
        for(Integer i:data.getKey()){
            if(n < data.getKey().size()-1) {
                change.append( CommonFuction.getLineNo(dataManager.getLineInfoList().get(i).linename)[0]+ "->");
            }
            else {
                change.append(CommonFuction.getLineNo(dataManager.getLineInfoList().get(i).linename)[0]);
            }
            n++;
        }

        String str = startStation+data.getValue().get(0).getCname()+"  "+endstation+data.getValue().get(data.getValue().size()-1).getCname();

        holder.setText(R.id.change_numner, String.format(lineChange,data.getKey().size()-1+""))
                .setText(R.id.change_lineid, change.toString())
                .setText(R.id.station_number, String.format(staionsCNumber,data.getValue().size()-1+"")+"")
                .setText(R.id.station_start_end, str);
    }
}
