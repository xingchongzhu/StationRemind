package com.traffic.locationremind.baidu.location.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.traffic.locationremind.manager.AsyncTaskManager;


public class SearchLoadingDialog extends Dialog{
    public SearchLoadingDialog(Context context, int theme){
        super(context, theme);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCanceledOnTouchOutside(false);
    }

    public void cancel(){
        super.dismiss();
    }
    @Override
    public void dismiss() {
        super.dismiss();
        AsyncTaskManager.getInstance().stopAllGeekRunable();
    }
}