package com.traffic.locationremind.common.util;

import android.content.Context;
import android.location.Geocoder;
import android.text.TextUtils;
import android.util.Log;
import com.traffic.locationremind.manager.bean.CityInfo;
import android.location.Address;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FileUtil {

    public static boolean dbIsExist(Context context,CityInfo city){
        if(city == null){
            return false;
        }
        String dirPath = "/data/data/" + context.getPackageName() + "/databases/"+city.getPingying()+".db";
        File file = new File(dirPath);
        if(!file.exists()){
            return false;
        }
        return true;
    }


    //关键点通过下面方式获取id，drawable资源
    public static int getResIconId(Context context, String iconName){
        return context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
    }


    //开启地址解析

    public static final String KEY_1 = "yZSTYLk9UUvs0ZqXqBbtTp8ViKk5vxLM";

    /**
     * 返回输入地址的经纬度坐标
     * key lng(经度),lat(纬度)
     */
    public static Map<String,String> getGeocoderLatitude(String address){
        BufferedReader in = null;
        try {
            //将地址转换成utf-8的16进制
            address = URLEncoder.encode(address, "UTF-8");
            URL tirc = new URL("http://api.map.baidu.com/geocoder?address="+ address +"&output=json&ak="+ KEY_1);

            in = new BufferedReader(new InputStreamReader(tirc.openStream(),"UTF-8"));
            String res;
            StringBuilder sb = new StringBuilder("");
            while((res = in.readLine())!=null){
                sb.append(res.trim());
            }
            String str = sb.toString();
            Map<String,String> map = null;
            if(!TextUtils.isEmpty(str)){
                int lngStart = str.indexOf("lng\":");
                int lngEnd = str.indexOf(",\"lat");
                int latEnd = str.indexOf("},\"precise");
                if(lngStart > 0 && lngEnd > 0 && latEnd > 0){
                    String lng = str.substring(lngStart+5, lngEnd);
                    String lat = str.substring(lngEnd+7, latEnd);

                    map = new HashMap<String,String>();
                    map.put("lng", lng);
                    map.put("lat", lat);
                    return map;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    static final double disLong = 0.011021;
    static final double disLat = 0.003204;
    public static Address getGeoPointBystr(Context context,String str) {
        Address address_temp = null;
        if (str != null) {
            Geocoder gc = new Geocoder(context, Locale.CHINA);
            List<Address> addressList = null;
            try {
                addressList = gc.getFromLocationName(str, 1);
                if (!addressList.isEmpty()) {
                    address_temp = addressList.get(0);
                    double Latitude = address_temp.getLatitude()+disLat;
                    double Longitude = address_temp.getLongitude()+disLong;
                    Log.d("zxc003",str+" Latitude = "+Latitude+" Longitude = "+Longitude);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return address_temp;
    }


}
