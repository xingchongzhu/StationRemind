package com.traffic.locationremind.baidu.location.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.view.ColorNodeView;
import com.traffic.locationremind.baidu.location.view.SingleNodeView;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @function listviewadapter
 * @auther: Created by yinglan
 * @time: 16/3/16
 */
public class ColorLineAdapter extends BaseAdapter {

    private final static String TAG = "ColorLineAdapter";
    private Context mContext;
    private int size = 0;

    List<Integer> dataList = new ArrayList<>();

    List<LineInfo> data = new ArrayList<>();
    public ColorLineAdapter(Context context) {
        this.mContext = context;
        this.size = (int)mContext.getResources().getDimension(R.dimen.single_node_bitmap_size);
    }

    public void setData(List<LineInfo> data){
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data == null?0:data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewholder;

        if (null == convertView) {
            convertView = View.inflate(mContext, R.layout.color_node_item, null);
            viewholder = new ViewHolder(convertView);
            convertView.setTag(viewholder);
        } else {
            viewholder = (ViewHolder) convertView.getTag();
        }
        if(data != null && data.get(position) != null ){
            //Log.d(TAG,"getView viewholder.textView ="+viewholder.textView+" name = "+data.get(position).linename);
            viewholder.textView.setText(data.get(position).lineid+data.get(position).linename);
            viewholder.singleNodeView.setColorId(data.get(position).colorid);
        }
        return convertView;
    }

    static class ViewHolder {
        TextView textView;
        ColorNodeView singleNodeView;

        public ViewHolder(View view) {
            textView = (TextView) view.findViewById(R.id.tv_name);
            singleNodeView = (ColorNodeView) view.findViewById(R.id.node);
        }
    }
}
