package com.traffic.locationremind.baidu.location.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.object.LineObject;
import com.traffic.locationremind.baidu.location.view.SelectlineMap;
import com.traffic.locationremind.manager.bean.StationInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @function listviewadapter
 * @auther: Created by yinglan
 * @time: 16/3/16
 */
public class RemindLineAdapter extends BaseAdapter {

    private final static String TAG = "RemindLineAdapter";
    private Context mContext;

    List<Integer> dataList = new ArrayList<>();

    Map<Integer,List<StationInfo>> data1 = new HashMap<>();
    Map.Entry<List<Integer>,List<StationInfo>> data;
    public RemindLineAdapter(Context context) {
        this.mContext = context;
    }

    public void setData(Map.Entry<List<Integer>,List<StationInfo>> data){
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewholder;

        if (null == convertView) {
            convertView = View.inflate(mContext, R.layout.remind_line_item_listview, null);
            viewholder = new ViewHolder(convertView);
            convertView.setTag(viewholder);
        } else {
            viewholder = (ViewHolder) convertView.getTag();

        }

        viewholder.textView.setStationList(new LineObject(data.getValue(),data.getKey()));

        return convertView;
    }

    static class ViewHolder {
        SelectlineMap textView;

        public ViewHolder(View view) {
            textView = (SelectlineMap) view.findViewById(R.id.item_tv_2);
        }
    }
}
