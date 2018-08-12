package com.traffic.locationremind.manager.bean;


public class AddInfo {
    public static String  LAT = "lat";
    public static String LOT = "lot";
    public static String NAME = "name";

    private String lat;//经度
    private String lot;//纬度
    private String name;//纬度

    public String getLat() {
        return this.lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLot() {
        return lot;
    }

    public void setLot(String lot) {
        this.lot = lot;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
