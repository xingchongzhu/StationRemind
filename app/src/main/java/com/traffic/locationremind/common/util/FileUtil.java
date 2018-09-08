package com.traffic.locationremind.common.util;

import android.content.Context;
import android.util.Log;
import com.traffic.locationremind.manager.bean.CityInfo;

import java.io.File;
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

}
