package com.traffic.locationremind.baidu.location.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.fragment.FullMapFragment;
import com.traffic.locationremind.baidu.location.utils.UWhiteListSettingUtils;
import com.traffic.locationremind.manager.AsyncTaskManager;


public class AutoManagerHintDialog extends Dialog{
    private Context context;
    private SettingReminderDialog.NoticeDialogListener listener;
    public AutoManagerHintDialog(Context context, int theme){
        super(context, theme);
        this.context = context;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auto_manager_dialog);
        //TextView cancle = (TextView) findViewById(R.id.cancle);
        TextView ok = (TextView) findViewById(R.id.ok);
        /*cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });*/
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UWhiteListSettingUtils.enterWhiteListSetting(context);
                dismiss();
            }
        });
        setCanceledOnTouchOutside(false);
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = d.getWidth();
        getWindow().setAttributes(p);
        getWindow().setGravity(Gravity.BOTTOM);
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