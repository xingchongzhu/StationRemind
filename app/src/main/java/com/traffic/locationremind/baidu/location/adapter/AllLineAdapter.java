package com.traffic.locationremind.baidu.location.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.view.SingleNodeView;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataManager;

/**
 * @function listviewadapter
 * @auther: Created by yinglan
 * @time: 16/3/16
 */
public class AllLineAdapter extends BaseAdapter {

    private final static String TAG = "AllLineAdapter";
    private Context mContext;
    private int size = 0;
    private LineInfo data;
    private DataManager mDataManager;

    public AllLineAdapter(Context context, DataManager mDataManager) {
        this.mContext = context;
        this.mDataManager = mDataManager;
        this.size = (int) mContext.getResources().getDimension(R.dimen.current_bitmap_siez);
    }

    public void setData(LineInfo data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.getStationInfoList().size();
    }

    @Override
    public StationInfo getItem(int position) {
        return data ==null?null:data.getStationInfoList().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewholder;

        if (null == convertView) {
            convertView = View.inflate(mContext, R.layout.single_node_item, null);
            viewholder = new ViewHolder(convertView);
            convertView.setTag(viewholder);
        } else {
            viewholder = (ViewHolder) convertView.getTag();
        }
        if (data != null) {
            viewholder.textView.setText(data.getStationInfoList().get(position).getCname());
            if (data.getStationInfoList().get(position).canTransfer()) {
                Bitmap bitmap = CommonFuction.getbitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.cm_route_map_pin_dottransfer), size / 5 * 3, size / 5 * 3);
                viewholder.singleNodeView.setTransFerBitmap(bitmap);
            } else {
                viewholder.singleNodeView.setTransFerBitmap(null);
            }
            viewholder.singleNodeView.setColor(data.colorid);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView textView;
        SingleNodeView singleNodeView;

        public ViewHolder(View view) {
            textView = (TextView) view.findViewById(R.id.text);
            singleNodeView = (SingleNodeView) view.findViewById(R.id.node);
        }
    }
}
