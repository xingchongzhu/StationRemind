package com.traffic.locationremind.baidu.location.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

public class SearchEditView extends EditText {
    ListView listView;

    public SearchEditView(Context context) {
        super(context);
    }

    public SearchEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchEditView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setListView(ListView listView){
        this.listView = listView;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        // Catch the back button on the soft keyboard so that we can just close the activity
        if (event.getKeyCode() == android.view.KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_UP) {
            if(listView != null){
                listView.setVisibility(View.GONE);
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
