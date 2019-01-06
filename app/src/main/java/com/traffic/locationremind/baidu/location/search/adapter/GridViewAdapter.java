package com.traffic.locationremind.baidu.location.search.adapter;

import android.content.Context;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.search.util.CommonAdapter;
import com.traffic.locationremind.baidu.location.search.util.ViewHolder;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.manager.bean.StationInfo;

import java.util.List;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GridViewAdapter extends BaseAdapter {
    private Context context;
    private List<String> listitem;

    public GridViewAdapter(Context context,List<String> listitem) {
        this.context = context;
        this.listitem = listitem;
    }

    @Override
    public int getCount() {
        return listitem.size();
    }

    @Override
    public Object getItem(int position) {
        return listitem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_item, null);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.title);
        if(textView != null) {
            textView.setText(listitem.get(position));
        }
        return convertView;
    }

}