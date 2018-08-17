package com.traffic.locationremind.manager.bean;


import android.text.TextUtils;

public class StationInfo {

    public static String ID = "id";
    public static String LINEID = "lineid";
    public static String PM = "pm";
    public static String CNAME = "cname";
    public static String PNAME = "pname";
    public static String ANAME = "aname";
    public static String LOT = "lot";
    public static String LAT = "lat";
    /*public static String PRESTATION = "preStation";
    public static String NEXTSTATION = "nextStation";*/
    public static String STATIONINFO = "stationinfo";
    public static String TRANSFER = "transfer";

    public int id;
    public int lineid;//线路id
    public int pm;
    public String cname;//站台名
    public String pname;//站台英文名
    public String aname;//站台名简称
    public String stationinfo;//站台时间
    public String lot;//进度
    public String lat;//纬度
    /*public String preStation;//下一站
    public String nextStation;//上一站*/
    public String transfer;
    public int colorId;

    //public String cityNo;//城市编码
/*
    public void setCityNo(String cityno){
        this.cityNo = cityno;
    }

    public String getCityNo(){
        return cityNo;
    }*/

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLineid() {
        return lineid;
    }

    public void setLineid(int lineid) {
        this.lineid = lineid;
    }

    public int getPm() {
        return pm;
    }

    public void setPm(int pm) {
        this.pm = pm;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public String getAname() {
        return aname;
    }

    public void setAname(String aname) {
        this.aname = aname;
    }

    public String getLot() {
        return lot;
    }

    public void setLot(String lot) {
        this.lot = lot;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    /*public void setPreStation(String preStation){
        this.preStation = preStation;
    }

    public String getPreStation(){
        return preStation;
    }

    public void setNextStation(String nextStation){
        this.nextStation = nextStation;
    }

    public String getNextStation(){
        return nextStation;
    }
*/
    public void setStationInfo(String stationinfo){
        this.stationinfo = stationinfo;
    }

    public String getStationInfo(){
        return stationinfo;
    }

    public void setTransfer(String transfer){
        this.transfer = transfer;
    }

    public String getTransfer(){
        return transfer;
    }

    public boolean canTransfer(){
        if(TextUtils.isEmpty(transfer) || transfer.equals("0")){
            return false;
        }
        return true;
    }

}
