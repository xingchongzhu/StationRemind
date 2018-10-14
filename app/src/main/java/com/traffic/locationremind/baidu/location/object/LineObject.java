package com.traffic.locationremind.baidu.location.object;

import com.traffic.locationremind.baidu.location.item.Node;
import com.traffic.locationremind.manager.bean.StationInfo;

import java.util.ArrayList;
import java.util.List;

public class LineObject {
    public List<StationInfo> stationList ;
    public List<Integer> lineidList ;
    public List<StationInfo> transferList ;
    public LineObject(){
        
    }
    public LineObject(List<StationInfo>stationList,List<Integer> lineidList ){
        this.stationList = stationList;
        this.lineidList = lineidList;
    }

    public LineObject(List<StationInfo>stationList,List<Integer> lineidList ,List<StationInfo> transferList){
        this.stationList = stationList;
        this.lineidList = lineidList;
        this.transferList = transferList;
    }
}
