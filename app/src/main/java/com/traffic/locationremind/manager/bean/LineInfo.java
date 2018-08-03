package com.traffic.locationremind.manager.bean;


import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LineInfo {
    public static String ID = "id";
    public static String LINEID = "lineid";
    public static String LINENAME = "linename";
    public static String LINEINFO = "lineinfo";
    public static String RGBCOOLOR = "rgbcolor";
    public static String FORWARD = "forward";
    public static String REVERSE = "reverse";

    public int lineid;//线路id
    public String linename;//线路名
    public String lineinfo;//线路信息
    public String rgbColor;
    public int colorid;

    public String forward;
    public String reverse;

    public String cityNo;//城市编码

    public void setCityNo(String cityno){
        this.cityNo = cityno;
    }

    public String getCityNo(){
        return cityNo;
    }

    private List<StationInfo> mStationInfoList;//地图站台信息

    public void setStationInfoList(List<StationInfo> mStationInfoList){
        if(this.mStationInfoList != null){
            this.mStationInfoList.clear();
        }
        this.mStationInfoList = mStationInfoList;
    }

    public void setRGBCOOLOR(String rgbColor){
        this.rgbColor = rgbColor;
        String color[]=rgbColor.split(",");
        int cc[]={0,0,0};
        //Log.d("ttxx","setRGBCOOLOR rgbColor = "+rgbColor);
        for(int n = 0;n<color.length;n++){
            String str2 = color[n].replaceAll(" ", "");
            //Log.d("ttxx","setRGBCOOLOR color[n] = "+color[n]);
            cc[n]= Integer.parseInt(str2.equals("")?"0":color[n]);
        }
        colorid = Color.rgb(cc[0],cc[1],cc[2]);
    }

    public String getRGBCOOLOR(){
        return rgbColor;
    }

    public List<StationInfo> getStationInfoList(){
        return mStationInfoList;
    }

    public int getLineid() {
        return lineid;
    }

    public void setLineid(int lineid) {
        this.lineid = lineid;
    }

    public String getLinename() {
        return linename;
    }

    public void setLinename(String linename) {
        this.linename = linename;
    }

    public String getLineinfo() {
        return lineinfo;
    }

    public void setLineinfo(String lineinfo) {
        this.lineinfo = lineinfo;
    }

    public void setForward(String forward) {
        this.forward = forward;
    }

    public String getForwad() {
        return this.forward;
    }

    public void setReverse(String reverse){
        this.reverse = reverse;
    }

    public String getReverse(){
        return reverse;
    }
}
