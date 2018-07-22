package com.traffic.locationremind.baidu.location.object;


public class NotificationObject {

    String lineName;
    String startStation;
    String endStation;
    String nextStation;
    String time;

    public NotificationObject() {

    }

    public NotificationObject(String lineName,
                              String startStation,
                              String endStation,
                              String nextStation,
                              String time) {
        this.lineName = lineName;
        this.startStation = startStation;
        this.endStation = endStation;
        this.nextStation = nextStation;
        this.time = time;

    }

    public String getEndStation() {
        return endStation;
    }

    public String getNextStation() {
        return nextStation;
    }

    public String getLineName() {
        return lineName;
    }

    public String getStartStation() {
        return startStation;
    }

    public String getTime() {
        return time;
    }

    public void setEndStation(String endStation) {
        this.endStation = endStation;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    public void setNextStation(String nextStation) {
        this.nextStation = nextStation;
    }

    public void setStartStation(String startStation) {
        this.startStation = startStation;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
