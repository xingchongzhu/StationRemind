package com.traffic.locationremind.baidu.location.listener;

import com.traffic.locationremind.baidu.location.object.LineObject;
import com.traffic.locationremind.manager.bean.StationInfo;

import java.util.List;
import java.util.Map;

public interface GoToFragmentListener {
    void openRemindFragment(LineObject lineObject);
}