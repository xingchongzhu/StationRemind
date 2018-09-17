package com.traffic.locationremind.baidu.location.search.widge;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.search.adapter.SearchAdapter;
import com.traffic.locationremind.baidu.location.view.SearchEditView;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.manager.bean.CityInfo;
import com.traffic.locationremind.manager.bean.StationInfo;


/**
 * Created by yetwish on 2015-05-11
 */

public class SearchView extends LinearLayout implements View.OnClickListener {
    private final static String TAG= "SearchView";

    /**
     * 输入框
     */
    private SearchEditView startInput, endInput;

    /**
     * 删除键
     */
    private ImageView startDelete, endDelete,change,serach;
    private View loading;
    /**
     * 返回按钮
     */
    //private Button btnBack;

    /**
     * 上下文对象
     */
    private Context mContext;

    /**
     * 弹出列表
     */
    private ListView lvTips;
    private ListView result;

    /*
     *最近搜索记录
     */
    private GridView recentSerachGrid;
    /**
     * 提示adapter （推荐adapter）
     */

    /**
     * 自动补全adapter 只显示名字
     */
    private SearchAdapter mAutoCompleteAdapter;

    /**
     * 搜索回调接口
     */
    private SearchViewListener mListener;

    /**
     * 设置搜索回调接口
     *
     * @param listener 监听者
     */
    private TextView city_select;
    public void setSearchViewListener(SearchViewListener listener) {
        mListener = listener;
    }

    public ListView getLvTips() {
        return lvTips;
    }
    public ListView getResultListview() {
        return result;
    }

    public GridView getRecentSerachGrid() {
        return recentSerachGrid;
    }

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.search_layout, this);
        initViews();
    }

    public View getLoading(){
        return loading;
    }
    private void initViews() {
        startInput = (SearchEditView) findViewById(R.id.search_start_input);
        startDelete = (ImageView) findViewById(R.id.search_start_delete);

        city_select = (TextView) findViewById(R.id.city_select);
        endInput = (SearchEditView) findViewById(R.id.search_end_input);
        endDelete = (ImageView) findViewById(R.id.search_end_delete);

        lvTips = (ListView) findViewById(R.id.main_lv_search_results);
        recentSerachGrid = (GridView) findViewById(R.id.recent_grid);

        change = (ImageView) findViewById(R.id.change);
        //serach = (ImageView) findViewById(R.id.serach);
        result = (ListView) findViewById(R.id.result);
        startInput.addTextChangedListener(new StartEditChangedListener());
        startInput.setOnClickListener(this);
        startInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (lvTips != null)
                        lvTips.setVisibility(GONE);
                    endInputNotifyStartSearching(startInput.getText().toString());
                }
                return true;
            }
        });
        startDelete.setOnClickListener(this);

        endInput.addTextChangedListener(new EndEditChangedListener());
        endInput.setOnClickListener(this);
        endInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (lvTips != null)
                        lvTips.setVisibility(GONE);
                    endInputNotifyStartSearching(endInput.getText().toString());
                }
                return true;
            }
        });
        endDelete.setOnClickListener(this);

        change.setOnClickListener(this);
        //serach.setOnClickListener(this);
    }

    boolean isComplete = false;

    public void setStartInput(String text){
        startInput.setText(text);
    }

    public void setendInput(String text){
        endInput.setText(text);
    }

    public void setSelectStation(int position) {
        String text = ((StationInfo) lvTips.getAdapter().getItem(position)).getCname();
        isComplete = true;
        if (startInput.hasFocus()) {
            startInput.setText(text);
            startInput.setSelection(text.length());
        } else {
            endInput.setText(text);
            endInput.setSelection(text.length());
        }
        //hint list view gone and result list view show
        if (lvTips != null) {
            ((SearchAdapter) lvTips.getAdapter()).clearData();
        }

        hideSoftInput();
        if (mListener != null) {
            if(CommonFuction.saveNewKeyToRecentSerach(getContext(), text)){
                mListener.notificationRecentSerachChange(getContext());
            }
        }
        notifyStartSearching(null);
    }

    public void setRecentSelectStation(String text) {
        isComplete = true;
        if (startInput.hasFocus()) {
            startDelete.setVisibility(VISIBLE);
            startInput.setText(text);
            startInput.setSelection(text.length());
        } else {
            endDelete.setVisibility(VISIBLE);
            endInput.setText(text);
            endInput.setSelection(text.length());
        }
        //hint list view gone and result list view show
        if (lvTips != null) {
            ((SearchAdapter) lvTips.getAdapter()).clearData();
        }
    }


    /**
     * 通知监听者 进行搜索操作
     *
     * @param text
     */
    private void notifyStartSearching(String text) {
        if (mListener != null) {
            mListener.onSearch(getContext(), startInput.getText().toString() , endInput.getText().toString());
        }
    }

    /**
     * 通知监听者 进行搜索操作
     *
     * @param text
     */
    private void endInputNotifyStartSearching(String text) {
        if (mListener != null) {
            mListener.onSearch(getContext(), startInput.getText().toString() , endInput.getText().toString());
        }
        hideSoftInput();
    }

    public void setStartCurrentLocation(){
        /*if(endInput != null){
            endInput.setText("");
        }
        if(startInput != null) {
            startInput.setText(getResources().getString(R.string.current_location));
            startInput.setSelection(startInput.getText().toString().length());
        }*/

        if(city_select != null)
            city_select.setText( CommonFuction.getSharedPreferencesValue(getContext(), CityInfo.CITYNAME));
    }

    public void hideSoftInput() {
        //隐藏软键盘
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(startInput.getWindowToken(), 0); //强制隐藏键盘
        imm.hideSoftInputFromWindow(endInput.getWindowToken(), 0); //强制隐藏键盘
    }

    /**
     * 设置自动补全adapter
     */
    public void setAutoCompleteAdapter(SearchAdapter adapter) {
        this.mAutoCompleteAdapter = adapter;
    }

    private class StartEditChangedListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            if (isComplete) {
                isComplete = false;
                notifyStartSearching(null);
                return;
            }
            if (!"".equals(charSequence.toString())) {
                startDelete.setVisibility(VISIBLE);
                //更新autoComplete数据
                if (mListener != null) {
                    mListener.onRefreshAutoComplete(getContext(), charSequence + "");
                }
                notifyStartSearching(null);
            } else {
                startDelete.setVisibility(GONE);
                if (lvTips != null) {
                    mAutoCompleteAdapter.clearData();
                }
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }

    private class EndEditChangedListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            if (isComplete) {
                isComplete = false;
                notifyStartSearching(null);
                return;
            }
            if (!"".equals(charSequence.toString())) {
                endDelete.setVisibility(VISIBLE);
                //更新autoComplete数据
                if (mListener != null) {
                    mListener.onRefreshAutoComplete(getContext(), charSequence + "");
                }
                notifyStartSearching(null);
            } else {
                endDelete.setVisibility(GONE);
                if (lvTips != null) {
                    mAutoCompleteAdapter.clearData();
                }
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_start_input:
                if (lvTips != null) {
                    lvTips.setVisibility(VISIBLE);
                    ((SearchAdapter) lvTips.getAdapter()).clearData();
                }
                if(!TextUtils.isEmpty(startInput.getText().toString())){
                    startDelete.setVisibility(VISIBLE);
                }
                break;
            case R.id.search_end_input:
                if (lvTips != null) {
                    lvTips.setVisibility(VISIBLE);
                    ((SearchAdapter) lvTips.getAdapter()).clearData();
                }
                if(!TextUtils.isEmpty(endInput.getText().toString())){
                    endDelete.setVisibility(VISIBLE);
                }
                break;
            case R.id.search_start_delete:
                startInput.setText("");
                startDelete.setVisibility(GONE);
                break;
            case R.id.search_end_delete:
                endInput.setText("");
                endDelete.setVisibility(GONE);
                break;
            case R.id.change:
                String str = startInput.getText().toString();
                String str1 = endInput.getText().toString();
                startInput.setText("");
                endInput.setText("");
                startInput.setText(str1);
                endInput.setText(str);
                startInput.setSelection(startInput.getText().toString().length());
                endInput.setSelection(endInput.getText().toString().length());
                break;
            /*case R.id.serach:
                notifyStartSearching("");
                break;*/
        }
    }

    /**
     * search view回调方法
     */
    public interface SearchViewListener {

        /**
         * 更新自动补全内容
         *
         * @param text 传入补全后的文本
         */
        void onRefreshAutoComplete(Context context, String text);

        /**
         * 开始搜索
         *
         */
        void onSearch(Context context,String start,String end);

        void notificationRecentSerachChange(Context context);

    }

}