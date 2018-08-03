package com.traffic.locationremind.baidu.location.listener;

import android.view.KeyEvent;
import com.traffic.locationremind.manager.bean.StationInfo;

import java.util.List;

public interface ActivityListener {
    boolean onKeyDown(int keyCode, KeyEvent event);

    boolean moveTaskToBack();
}