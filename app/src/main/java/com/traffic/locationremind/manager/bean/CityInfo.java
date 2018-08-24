package com.traffic.locationremind.manager.bean;


public class CityInfo {
    public static String  CITYNO = "cityno";
    public static String CITYNAME = "cityname";
    public static String LOCATIONNAME = "locationname";
    public static String PINGYING = "pingying";

    private String cityNo;//城市编码
    private String cityName;//城市名
    private String pingying;//城市名

    public CityInfo(){

    }
    public CityInfo(String name,String pingying){
        this.cityName = name;
        this.pingying = pingying;
    }
    public void setCityNo(String cityno){
        this.cityNo = cityno;
    }

    public String getCityNo(){
        return cityNo;
    }

    public void setCityName(String cityName){
        this.cityName = cityName;
    }

    public String getCityName(){
        return cityName;
    }

    public String getPingying() {
        return pingying;
    }

    public void setPingying(String pingying) {
        this.pingying = pingying;
    }
}
