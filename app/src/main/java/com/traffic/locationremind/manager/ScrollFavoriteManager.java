package com.traffic.locationremind.manager;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.adapter.FavouriteAdapter;
import com.traffic.locationremind.baidu.location.listener.RemindSetViewListener;
import com.traffic.locationremind.baidu.location.object.LineObject;
import com.traffic.locationremind.baidu.location.utils.ScreenUtil;
import com.traffic.locationremind.baidu.location.view.ScrollLayout;

import java.util.ArrayList;
import java.util.List;

public class ScrollFavoriteManager {

    private ScrollLayout mScrollLayout;
    private CardView btn_layout;
    private TextView textTitle;
    //private RelativeLayout remind_root;
    private TextView hint_text;
    private FavouriteAdapter mFavouriteAdapter;
    RemindSetViewListener mRemindSetViewListener;

    private ScrollLayout.OnScrollChangedListener mOnScrollChangedListener = new ScrollLayout.OnScrollChangedListener() {
        @Override
        public void onScrollProgressChanged(float currentProgress) {
            if (currentProgress >= 0) {
                float precent = 255 * currentProgress;
                if (precent > 255) {
                    precent = 255;
                } else if (precent < 0) {
                    precent = 0;
                }
                mScrollLayout.getBackground().setAlpha(255 - (int) precent);
            }
        }

        @Override
        public void onScrollFinished(ScrollLayout.Status currentStatus) {
            if (currentStatus.equals(ScrollLayout.Status.EXIT)) {
                btn_layout.setVisibility(View.VISIBLE);
                textTitle.setVisibility(View.GONE);
            }
        }

        @Override
        public void onChildScroll(int top) {
        }
    };

    public ScrollFavoriteManager(Activity activity, View view){
        initView( activity, view);
    }

    public void setRemindSetViewListener(RemindSetViewListener mRemindSetViewListener){
        this.mRemindSetViewListener = mRemindSetViewListener;
    }

    private void initView(Activity activity,View view) {
        RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.remind_root);
        mScrollLayout = (ScrollLayout) view.findViewById(R.id.scroll_down_layout);
        textTitle = (TextView)view.findViewById(R.id.text_title);
        btn_layout = (CardView)view.findViewById(R.id.btn_layout);
        //remind_root = (RelativeLayout)view.findViewById(R.id.remind_root);
        //hint_text = (TextView)view.findViewById(R.id.hint_text);
        ListView listView = (ListView) view.findViewById(R.id.list_view);

        mFavouriteAdapter = new FavouriteAdapter(activity,new ArrayList<LineObject>(),R.layout.serach_result_item_layout);
        listView.setAdapter(mFavouriteAdapter);

        /**设置 setting*/
        mScrollLayout.setMinOffset(0);
        mScrollLayout.setMaxOffset((int) (ScreenUtil.getScreenHeight(activity) * 0.5));
        mScrollLayout.setExitOffset(ScreenUtil.dip2px(activity, 50));
        mScrollLayout.setIsSupportExit(true);
        mScrollLayout.setAllowHorizontalScroll(true);
        mScrollLayout.setOnScrollChangedListener(mOnScrollChangedListener);
        mScrollLayout.setToExit();

        mScrollLayout.getBackground().setAlpha(0);
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScrollLayout.scrollToExit();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mRemindSetViewListener != null) {
                    mRemindSetViewListener.openSetWindow(mFavouriteAdapter.getItem(position));
                }
            }
        });
    }

    public void openScrollView(List<LineObject> lineObjects){
        mFavouriteAdapter.setData(lineObjects);
        btn_layout.setVisibility(View.GONE);
        mScrollLayout.setToOpen();
        textTitle.setVisibility(View.VISIBLE);
    }

    public void closeScrollView(){
        mScrollLayout.setToExit();
    }

    public boolean isOpen(){
        if(mScrollLayout.getCurrentStatus() == ScrollLayout.Status.OPENED){
            return true;
        }else{
            return false;
        }
    }
}
