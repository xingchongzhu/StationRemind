package com.traffic.locationremind.baidu.location.listener;

import com.baidu.location.BDLocation;
import com.traffic.locationremind.manager.bean.StationInfo;

import java.util.List;

public interface LocationChangerListener {
    void loactionStation(BDLocation location);
}