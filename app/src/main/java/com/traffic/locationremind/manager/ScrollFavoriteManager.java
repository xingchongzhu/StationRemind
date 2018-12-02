package com.traffic.locationremind.manager;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.baidu.location.BDLocation;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.adapter.FavouriteAdapter;
import com.traffic.locationremind.baidu.location.fragment.RemindFragment;
import com.traffic.locationremind.baidu.location.internal.Utils;
import com.traffic.locationremind.baidu.location.listener.RemindSetViewListener;
import com.traffic.locationremind.baidu.location.object.LineObject;
import com.traffic.locationremind.baidu.location.utils.AnimationUtil;
import com.traffic.locationremind.baidu.location.utils.ScreenUtil;
import com.traffic.locationremind.baidu.location.view.ScrollLayout;
import com.traffic.locationremind.manager.bean.AddInfo;

import java.util.ArrayList;
import java.util.List;

public class ScrollFavoriteManager {

    private ScrollLayout mScrollLayout;
    private LinearLayout btn_layout;
    private View Title;
    //private RelativeLayout remind_root;
    private TextView hint_text;
    private TextView collection_btn;
    private FavouriteAdapter mFavouriteAdapter;
    private RemindSetViewListener mRemindSetViewListener;
    private RemindFragment fragment;
    private EditText lat;
    private EditText lot;
    private EditText name;
    private RelativeLayout relativeLayout;
    private int drawableSize = 50;
    private LinearLayout lineLayout;
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
                Title.setVisibility(View.GONE);
                mScrollLayout.setVisibility(View.GONE);
                fragment.updateColloctionView();
            }

        }

        @Override
        public void onChildScroll(int top) {
            Log.d("ScrollFavoriteManager","top = "+top);
            AnimationUtil.rotateClose(Title,300);
        }
    };

    public ScrollFavoriteManager(RemindFragment fragment, View view) {
        initView(fragment, view);
    }

    public void setRemindSetViewListener(RemindSetViewListener mRemindSetViewListener) {
        this.mRemindSetViewListener = mRemindSetViewListener;
    }

    private void initView(final RemindFragment fragment, View view) {
        this.fragment = fragment;
        drawableSize = (int)fragment.getResources().getDimension(R.dimen.drwable_size);
        relativeLayout = (RelativeLayout) view.findViewById(R.id.remind_root);
        mScrollLayout = (ScrollLayout) view.findViewById(R.id.scroll_down_layout);
        lineLayout = (LinearLayout) view.findViewById(R.id.lineLayout);
        Title = (View) view.findViewById(R.id.text_title);
        btn_layout = (LinearLayout) view.findViewById(R.id.btn_layout);
        collection_btn = (TextView) view.findViewById(R.id.collection_btn);
        ListView listView = (ListView) view.findViewById(R.id.list_view);

        lat = (EditText) view.findViewById(R.id.lat);
        lot = (EditText) view.findViewById(R.id.lot);
        name = (EditText) view.findViewById(R.id.name);
        ImageButton save = (ImageButton) view.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AddInfo addInfo = new AddInfo();
                addInfo.setLat(lat.getText().toString());
                addInfo.setLot(lot.getText().toString());
                addInfo.setName(name.getText().toString());
                boolean result = ScrollFavoriteManager.this.fragment.getDataManager().getDataHelper().insetAddExtraInfo(addInfo);
                if (result) {
                    Toast.makeText(fragment.getActivity(), "保存成功", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(fragment.getActivity(), "保存失败", Toast.LENGTH_LONG).show();
                }
            }
        });
        mFavouriteAdapter = new FavouriteAdapter(fragment, new ArrayList<LineObject>(), R.layout.serach_result_item_layout);
        listView.setAdapter(mFavouriteAdapter);

       // setCompoundDrawables(Title,drawable);
        lineLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnimationUtil.rotateOpen(Title,300);
                mScrollLayout.setToExit();
            }
        });

        /**设置 setting*/
        mScrollLayout.setMinOffset(0);
        mScrollLayout.setMaxOffset((int) (ScreenUtil.getScreenHeight(fragment.getActivity()) * 0.5));
        mScrollLayout.setExitOffset(ScreenUtil.dip2px(fragment.getActivity(), 50));
        mScrollLayout.setIsSupportExit(true);
        mScrollLayout.setAllowHorizontalScroll(true);
        mScrollLayout.setOnScrollChangedListener(mOnScrollChangedListener);
        mScrollLayout.setToExit();

        mScrollLayout.getBackground().setAlpha(0);
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScrollLayout.setToExit();
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

    public void setCompoundDrawables(View view, Drawable drawable) {
        drawable.setBounds(0,0,drawableSize,drawableSize);
        //view.setCompoundDrawables(null, drawable, null, null);
        view.setBackgroundDrawable(drawable);
    }

    public void setLocation(BDLocation bDLocation) {
        if(bDLocation != null) {
            lat.setText(bDLocation.getLatitude() + "");
            lot.setText(bDLocation.getLongitude() + "");
        }
    }

    public void openScrollView(List<LineObject> lineObjects) {
        mFavouriteAdapter.setData(lineObjects);
        mScrollLayout.setVisibility(View.VISIBLE);
        btn_layout.setVisibility(View.GONE);
        mScrollLayout.setToOpen();
        Title.setVisibility(View.VISIBLE);

    }

    public void closeScrollView() {
        mScrollLayout.setToExit();
    }

    public boolean isOpen() {
        if (mScrollLayout.getCurrentStatus() == ScrollLayout.Status.OPENED) {
            return true;
        } else {
            return false;
        }
    }
}
