package com.traffic.locationremind.baidu.location.listener;

import com.traffic.locationremind.manager.bean.StationInfo;

import java.util.List;
import java.util.Map;

public interface RemindSetViewListener{
    void openSetWindow(Map.Entry<List<Integer>,List<StationInfo>> lastLines);
    void closeSetWindow();
}