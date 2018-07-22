package com.traffic.locationremind.manager.bean;

//CREATE TABLE exitinfo (cname VARCHAR(20),
//exitname VARCHAR(20),addr VARCHAR(300));
public class ExitInfo {
    public static String ID = "id";
    public static String CNAME = "cname";
    public static String EXITNAME = "exitname";
    public static String ADDR = "addr";
    private int id;
    private String cname;//站名
    private String exitname;//出口名
    private String addr;//出口地址
    public String cityNo;//城市编码

    public void setCityNo(String cityno){
        this.cityNo = cityno;
    }

    public String getCityNo(){
        return cityNo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getExitname() {
        return exitname;
    }

    public void setExitname(String exitname) {
        this.exitname = exitname;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }
}
