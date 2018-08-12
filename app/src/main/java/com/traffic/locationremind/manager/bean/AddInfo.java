package com.traffic.locationremind.manager.bean;


public class CityInfo {
    public static String  CITYNO = "cityno";
    public static String CITYNAME = "cityname";

    private String cityNo;//城市编码
    private String cityName;//城市名

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
}
