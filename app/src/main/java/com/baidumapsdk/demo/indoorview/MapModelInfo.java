package com.baidumapsdk.demo.indoorview;

import java.util.List;

public class MapModelInfo {
    public static int gps = 0;//gps定位成功
    public static int net = 1;//网络定位成功
    public double longtitude;//精度
    public double latitude;//纬度
    public String name;//位置名
    public String country;//国家
    public String city;//城市
    public String street;//街道
    public String addr;//地址
    public String time;//时间
    public String result;//时间
    public List<String> locationName;//当前位置所有信息 = new ArrayList<String>()
    public double radius = 300;//范围
    public int netType = -1;

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<String> getLocationName() {
        return locationName;
    }

    public void setLocationName(List<String> locationName) {
        this.locationName = locationName;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
