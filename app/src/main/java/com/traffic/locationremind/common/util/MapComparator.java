package com.traffic.locationremind.common.util;

import com.traffic.locationremind.manager.bean.ExitInfo;

import java.util.Comparator;

public class MapComparator  implements Comparator<ExitInfo> {
    public int compare(ExitInfo lhs, ExitInfo rhs) {
        return lhs.getExitname().compareTo(rhs.getExitname());
    }
}
