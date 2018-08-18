package com.traffic.locationremind.baidu.location.search.adapter;

import android.content.Context;
import android.view.View;


import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.search.model.Bean;
import com.traffic.locationremind.baidu.location.search.util.CommonAdapter;
import com.traffic.locationremind.baidu.location.search.util.ViewHolder;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataManager;

import java.util.List;

/**
 * Created by yetwish on 2015-05-11
 */

public class SearchAdapter extends CommonAdapter<StationInfo> {

    //String lineTail = "";
    DataManager dataManager;
    public SearchAdapter(Context context, List<StationInfo> data, int layoutId) {
        super(context, data, layoutId);
        dataManager = DataManager.getInstance(context);
        //lineTail = context.getResources().getString(R.string.line_tail);
    }

    public void clearData() {
        this.mData.clear();
        notifyDataSetChanged();
    }

    public void setData(List<StationInfo> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    @Override
    public StationInfo getItem(int i) {
        return super.getItem(i);
    }

    @Override
    public void convert(ViewHolder holder, int position) {

        StringBuffer transfer = new StringBuffer( CommonFuction.getLineNo(dataManager.getLineInfoList().get(mData.get(position).lineid).linename)[0]);
        if (mData.get(position).canTransfer()) {
            transfer.delete(0, transfer.length());
            String lists[] = mData.get(position).getTransfer().split(CommonFuction.TRANSFER_SPLIT);
            for (int i = 0; i < lists.length; i++) {
                if (i != lists.length - 1) {
                    transfer.append( CommonFuction.getLineNo(dataManager.getLineInfoList().get(mData.get(position).lineid).linename)[0] + ",");
                } else {
                    transfer.append( CommonFuction.getLineNo(dataManager.getLineInfoList().get(mData.get(position).lineid).linename)[0]);
                }
            }
        }
        holder.setText(R.id.item_search_tv_title, mData.get(position).getCname())
                .setText(R.id.item_search_tv_content, transfer.toString());
    }
}
