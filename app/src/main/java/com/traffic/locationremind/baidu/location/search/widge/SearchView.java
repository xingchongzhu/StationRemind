package com.traffic.locationremind.baidu.location.search.widge;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.traffic.location.remind.R;


/**
 * Created by yetwish on 2015-05-11
 */

public class SearchView extends LinearLayout implements View.OnClickListener {

    /**
     * 输入框
     */
    private EditText startInput,endInput;

    /**
     * 删除键
     */
    private ImageView startDelete,endDelete;

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

    /**
     * 提示adapter （推荐adapter）
     */
    private ArrayAdapter<String> mHintAdapter;

    /**
     * 自动补全adapter 只显示名字
     */
    private ArrayAdapter<String> mAutoCompleteAdapter;

    /**
     * 搜索回调接口
     */
    private SearchViewListener mListener;

    public boolean isStart = true;
    /**
     * 设置搜索回调接口
     *
     * @param listener 监听者
     */
    public void setSearchViewListener(SearchViewListener listener) {
        mListener = listener;
    }

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.search_layout, this);
        initViews();
    }

    public void setListView(ListView lvTip){
        this.lvTips = lvTip;
        lvTips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //set edit text
                String text = lvTips.getAdapter().getItem(i).toString();
                if(isStart){
                    startInput.setText(text);
                    startInput.setSelection(text.length());
                }else{
                    endInput.setText(text);
                    endInput.setSelection(text.length());
                }
                //hint list view gone and result list view show
                lvTips.setVisibility(View.GONE);
                notifyStartSearching(text);
            }
        });
    }

    private void initViews() {
        startInput = (EditText) findViewById(R.id.search_start_input);
        startDelete = (ImageView) findViewById(R.id.search_start_delete);

        endInput = (EditText) findViewById(R.id.search_end_input);
        endDelete = (ImageView) findViewById(R.id.search_end_delete);
        
        //btnBack = (Button) findViewById(R.id.search_btn_back);
        //lvTips = (ListView) findViewById(R.id.search_lv_tips);

        startDelete.setOnClickListener(this);
        endDelete.setOnClickListener(this);
        //btnBack.setOnClickListener(this);

        startInput.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                System.out.print("starthasFocus = "+hasFocus);
                if(hasFocus && lvTips != null){
                    lvTips.setVisibility(View.VISIBLE);
                }else if(!hasFocus && lvTips != null){
                    lvTips.setVisibility(View.GONE);
                }
            }
        });

        endInput.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                System.out.print("endhasFocus = "+hasFocus);
                if(hasFocus && lvTips != null){
                    lvTips.setVisibility(View.VISIBLE);
                }else if(!hasFocus && lvTips != null){
                    lvTips.setVisibility(View.GONE);
                }
            }
        });
        startInput.addTextChangedListener(new StartEditChangedListener());
        startInput.setOnClickListener(this);
        startInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if(lvTips != null)
                        lvTips.setVisibility(GONE);
                    notifyStartSearching(startInput.getText().toString());
                }
                return true;
            }
        });

        endInput.addTextChangedListener(new EndEditChangedListener());
        endInput.setOnClickListener(this);
        endInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if(lvTips != null)
                        lvTips.setVisibility(GONE);
                    notifyStartSearching(endInput.getText().toString());
                }
                return true;
            }
        });
    }

    /**
     * 通知监听者 进行搜索操作
     * @param text
     */
    private void notifyStartSearching(String text){
        if (mListener != null) {
            String str =  isStart?startInput.getText().toString():endInput.getText().toString();
            mListener.onSearch(getContext(),str);
        }
        //隐藏软键盘
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 设置热搜版提示 adapter
     */
    public void setTipsHintAdapter(ArrayAdapter<String> adapter) {
        this.mHintAdapter = adapter;
        if (lvTips != null && lvTips.getAdapter() == null) {
            lvTips.setAdapter(mHintAdapter);
        }
    }

    /**
     * 设置自动补全adapter
     */
    public void setAutoCompleteAdapter(ArrayAdapter<String> adapter) {
        this.mAutoCompleteAdapter = adapter;
    }

    private class StartEditChangedListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            if (!"".equals(charSequence.toString())) {
                startDelete.setVisibility(VISIBLE);
                if(lvTips != null){
                    lvTips.setVisibility(VISIBLE);
                    if (mAutoCompleteAdapter != null && lvTips.getAdapter() != mAutoCompleteAdapter) {
                        lvTips.setAdapter(mAutoCompleteAdapter);
                    }
                }

                //更新autoComplete数据
                if (mListener != null) {
                    mListener.onRefreshAutoComplete(getContext(),charSequence + "");
                }
            } else {
                startDelete.setVisibility(GONE);
                if(lvTips != null){
                    if (mHintAdapter != null) {
                        lvTips.setAdapter(mHintAdapter);
                    }
                    lvTips.setVisibility(GONE);
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
            if (!"".equals(charSequence.toString())) {
                endDelete.setVisibility(VISIBLE);
                if(lvTips != null){
                    lvTips.setVisibility(VISIBLE);
                    if (mAutoCompleteAdapter != null && lvTips.getAdapter() != mAutoCompleteAdapter) {
                        lvTips.setAdapter(mAutoCompleteAdapter);
                    }
                }

                //更新autoComplete数据
                if (mListener != null) {
                    mListener.onRefreshAutoComplete(getContext(),charSequence + "");
                }
            } else {
                endDelete.setVisibility(GONE);
                if(lvTips != null){
                    if (mHintAdapter != null) {
                        lvTips.setAdapter(mHintAdapter);
                    }
                    lvTips.setVisibility(GONE);
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
                /*if(lvTips != null)
                    lvTips.setVisibility(VISIBLE);*/
                isStart = true;
                break;
            case R.id.search_end_input:
               /* if(lvTips != null)
                    lvTips.setVisibility(VISIBLE);*/
                isStart = false;
                break;
            case R.id.search_start_delete:
                startInput.setText("");
                startDelete.setVisibility(GONE);
                break;
            case R.id.search_end_delete:
                endInput.setText("");
                endDelete.setVisibility(GONE);
                break;
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
        void onRefreshAutoComplete(Context context,String text);

        /**
         * 开始搜索
         *
         * @param text 传入输入框的文本
         */
        void onSearch(Context context,String text);

//        /**
//         * 提示列表项点击时回调方法 (提示/自动补全)
//         */
//        void onTipsItemClick(String text);
    }

}