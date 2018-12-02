

package com.traffic.locationremind.common.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.text.TextUtils;
import android.util.Log;
import com.baidu.location.BDLocation;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.activity.MainViewActivity;
import com.traffic.locationremind.baidu.location.item.Node;
import com.traffic.locationremind.baidu.location.object.LineObject;
import com.traffic.locationremind.manager.bean.CityInfo;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataHelper;
import com.traffic.locationremind.manager.database.DataManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonFuction {
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String CURRENTCITYNO = "currentcityno";//城市号
    public static final String ISREMINDER = "isreminder";//正在后台
    public static final String CURRENTLINEID = "currentLineId";//线路名
    public static final String INITCURRENTLINEID = "initcurrentLineId";//线路名
    public static final String CURRENTSTATIONNAME = "currentstationname";//站名
    public static final String STARTSTATIONNAME = "startstationname";//起始站
    public static final String ENDSTATIONNAME = "endstationname";//目标站
    public static final String FAVOURITE = "favourite";//目标站

    public static final String RECENTSERACHHISTORY = "recent_serach_history";//目标站

    public static final double RANDDIS = 0.5;
    private static final double EARTH_RADIUS = 6378137.0;
    public static final String SHNAME = "subwab_info";
    public static final String CITYNO = "cityno";
    public static final String TRANSFER_SPLIT = ",";
    public static final String TRANSFER_STATION_SPLIT = "@";
    public static final String TRANSFER_NUM_SPLIT = "&";
    public static final String TRANSFER_STATION_LAST_SPLIT = "!";
    public static final String FAVOURITE_STATION_SPLIT = "/";
    public static final String FAVOURITE_LINE = "#";
    public final static String HEAD = "head";
    public final static String TAIL = "tail";

    public final static int MAXRECENTSERACHHISTORY = 10;

    public final static double INVALE_DATA = 4.9E-324D;

    // 获得状态栏高度
    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            return context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
            return 75;
        }
    }

    public static String convertStationToString(LineObject lineObject) {
        if (lineObject == null || lineObject.stationList == null) {
            return "";
        }
        StringBuffer line = new StringBuffer();
        for (StationInfo stationInfo : lineObject.stationList) {
            String str = stationInfo.getCname() + FAVOURITE_LINE + stationInfo.lineid + FAVOURITE_STATION_SPLIT;
            line.append(str);
        }
        StringBuffer transfer = new StringBuffer();
        if(lineObject.transferList != null) {
            for (StationInfo stationInfo : lineObject.transferList) {
                String str = stationInfo.getCname() + TRANSFER_STATION_LAST_SPLIT;
                transfer.append(str);
            }
            line.append(TRANSFER_STATION_SPLIT + transfer.toString());
        }
        /*transfer.delete(0,transfer.length());
        for (Integer id : lineObject.lineidList) {
            String str = id + TRANSFER_NUM_SPLIT;
            transfer.append(str);
        }
        line.append(TRANSFER_STATION_SPLIT + transfer.toString());*/
        return line.toString();
    }

    public static boolean isEmptyColloctionFolder(Context context) {
        String allFavoriteLine = CommonFuction.getSharedPreferencesValue(context, CommonFuction.FAVOURITE);
        String string[] = allFavoriteLine.split(CommonFuction.TRANSFER_SPLIT);
        if (TextUtils.isEmpty(allFavoriteLine) || string.length <= 0) {
            return true;
        } else {
            return false;
        }
    }

    public static List<LineObject> getAllFavourite(Context context, DataManager mDataManager) {
        String allFavoriteLine = CommonFuction.getSharedPreferencesValue(context, CommonFuction.FAVOURITE);
        String string[] = allFavoriteLine.split(CommonFuction.TRANSFER_SPLIT);
        int size = string.length;
        List<LineObject> lastLinesLast = new ArrayList<>();
        List<StationInfo> transferList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            LineObject lineObject = CommonFuction.convertStringToStation(mDataManager.getAllstations(), string[i]);
            if (lineObject != null)
                lastLinesLast.add(lineObject);
        }
        return lastLinesLast;
    }

    public static LineObject convertStringToStation(Map<String, StationInfo> allStation, String line) {
        LineObject LineObject = new LineObject();
        List<StationInfo> list = new ArrayList<>();
        List<StationInfo> transferList = new ArrayList<>();
        List<Integer> listLine = new ArrayList<>();
        String stationTrans[] = line.split(TRANSFER_STATION_SPLIT);
        String strList[] = stationTrans[0].split(FAVOURITE_STATION_SPLIT);

        int size = strList.length;
        int lineid = -1;
        for (int i = 0; i < size; i++) {
            String str[] = strList[i].split(FAVOURITE_LINE);
            if (str.length > 0) {
                StationInfo stationInfo = allStation.get(str[0]);
                if (stationInfo != null) {
                    int tempid = convertToInt(str[1], 0);
                    if (lineid != tempid) {
                        listLine.add(tempid);
                    }
                    lineid = tempid;
                    stationInfo.lineid = tempid;
                    list.add(stationInfo);
                }
            }
        }
        StringBuffer stringBuffer = new StringBuffer();
        if (stationTrans.length > 1) {
            String stringList[] = stationTrans[1].split(TRANSFER_STATION_LAST_SPLIT);
            size = stringList.length;
            for (int i = 0; i < size; i++) {
                StationInfo stationInfo = allStation.get(stringList[i]);
                if (stationInfo != null) {
                    transferList.add(stationInfo);
                    stringBuffer.append(stationInfo.getCname()+" ->");

                }
            }
        }
        Log.d("zxc000",stringBuffer.toString());
        LineObject.stationList = list;
        LineObject.lineidList = listLine;
        LineObject.transferList = transferList;
        if (LineObject.stationList.size() <= 0) {
            return null;
        }
        return LineObject;
    }

    public static void writeSharedPreferences(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(SHNAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static boolean twoSameLineid(Map<Integer, Integer> map, StationInfo stationInfo) {
        int result = 0;
        for (Integer lineid : stationInfo.getTransferLineid()) {
            if (map.containsKey(lineid)) {
                result++;
            }
        }
        return result > 1 ? true : false;
    }

    public static List<StationInfo> getTransFerList(LineObject lineObject) {
        List<StationInfo> transferlist = new ArrayList<>();
        if(lineObject == null){
            return transferlist;
        }
        return lineObject.transferList;
    }

    public static boolean containTransfer(List<StationInfo> list, StationInfo stationInfo) {
        if(list == null){
            return false;
        }
        for (StationInfo info : list) {
            if (info.getCname().equals(stationInfo.getCname())) {
                return true;
            }
        }
        return false;
    }

    public static boolean saveNewKeyToRecentSerach(Context context, String str) {
        String string[] = getRecentSearchHistory(context);
        if (string == null || string.length <= 0) {
            writeSharedPreferences(context, RECENTSERACHHISTORY, str);
            return true;
        } else {
            boolean hasExist = false;
            for (int i = 0; i < string.length; i++) {
                if (string[i].equals(str)) {
                    hasExist = true;
                }
            }
            if (!hasExist) {
                if (string.length < MAXRECENTSERACHHISTORY) {
                    StringBuffer newStr = new StringBuffer();
                    for (int i = 0; i < string.length; i++) {
                        newStr.append(string[i] + TRANSFER_SPLIT);
                    }
                    newStr.append(str);
                    writeSharedPreferences(context, RECENTSERACHHISTORY, newStr.toString());
                } else {
                    StringBuffer newStr = new StringBuffer();
                    for (int i = 1; i < string.length; i++) {
                        newStr.append(string[i] + TRANSFER_SPLIT);
                    }
                    newStr.append(str);
                    writeSharedPreferences(context, RECENTSERACHHISTORY, newStr.toString());
                }
                return true;
            }
            return false;
        }
    }

    public static void writeBooleanSharedPreferences(Context context, String key, boolean value) {
        SharedPreferences sp = context.getSharedPreferences(SHNAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void writeIntSharedPreferences(Context context, String key, int value) {
        SharedPreferences sp = context.getSharedPreferences(SHNAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static String getSharedPreferencesValue(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(SHNAME, Context.MODE_PRIVATE);
        String name = sp.getString(key, "");
        return name;
    }

    public static String[] getRecentSearchHistory(Context context) {
        return getSharedPreferencesValue(context, RECENTSERACHHISTORY).split(TRANSFER_SPLIT);
    }

    public static boolean getSharedPreferencesBooleanValue(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(SHNAME, Context.MODE_PRIVATE);
        boolean state = sp.getBoolean(key, false);
        return state;
    }

    public static int getSharedPreferencesIntValue(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(SHNAME, Context.MODE_PRIVATE);
        int no = sp.getInt(key, 0);
        return no;
    }

    public static Bitmap getbitmap(Bitmap bm, int newWidth, int newHeight) {
        if (bm == null) {
            return null;
        }
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
                true);
        if (bm != null & !bm.isRecycled()) {
            bm.recycle();
            bm = null;
        }
        return newbm;
    }

    public static double getDistance(double longitude1, double latitude1, double longitude2, double latitude2) {
        double Lat1 = rad(latitude1);
        double Lat2 = rad(latitude2);
        double a = Lat1 - Lat2;
        double b = rad(longitude1) - rad(longitude2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(Lat1)
                * Math.cos(Lat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    public static double getDistanceLat(double lng1, double lat1, double lng2, double lat2) {
        double earthRadius = 6367000; //approximate radius of earth in meters
        /*
        Convert these degrees to radians
        to work with the formula
        */
        lat1 = (lat1 * Math.PI) / 180;
        lng1 = (lng1 * Math.PI) / 180;

        lat2 = (lat2 * Math.PI) / 180;
        lng2 = (lng2 * Math.PI) / 180;
        /*
        Using the
        Haversine formula
        http://en.wikipedia.org/wiki/Haversine_formula
        calculate the distance
        */
        double calcLongitude = lng2 - lng1;
        double calcLatitude = lat2 - lat1;
        double stepOne = Math.pow(Math.sin(calcLatitude / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(calcLongitude / 2), 2);
        double stepTwo = 2 * Math.asin(Math.min(1, Math.sqrt(stepOne)));
        double calculatedDistance = earthRadius * stepTwo;
        return calculatedDistance;

    }

    //把String转化为float
    public static float convertToFloat(String number, float defaultValue) {
        if (TextUtils.isEmpty(number)) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(number);
        } catch (Exception e) {
            return defaultValue;
        }

    }

    //把String转化为double
    public static double convertToDouble(String number, double defaultValue) {
        if (TextUtils.isEmpty(number)) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(number);
        } catch (Exception e) {
            return defaultValue;
        }

    }

    //把String转化为int
    public static int convertToInt(String number, int defaultValue) {
        if (TextUtils.isEmpty(number)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(number);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String getSubwayShowText(Context context, DataHelper mDataHelper, LineInfo mLineInfo) {
        String str = "";
		/*List<CityInfo> cityList = mDataHelper.QueryCityByCityNo();
		if (cityList != null && cityList.size() > 0) {
			str += cityList.get(0).getCityName();
		}*/

        str += context.getResources().getString(R.string.subway) +
                DataManager.getInstance(context).getLineInfoList().get(mLineInfo.getLineid()).linename +
                "(" + mLineInfo.getLinename() + ")" + "\n" + mLineInfo.getLineinfo();
        return str;
    }

    public static String[] getLineNo(String str) {
        String tmp[] = {""};
        return TextUtils.isEmpty(str) ? tmp : str.split("/");
    }

    public static boolean isvalidLocation(BDLocation location) {
        if (location == null || location.getLongitude() == CommonFuction.INVALE_DATA ||
                location.getLatitude() == CommonFuction.INVALE_DATA) {
            return false;
        }
        return true;
    }


    /**
     * 获取map中第一个key值
     *
     * @param map 数据源
     * @return
     */
    public static String getKeyOrNull(Map<String, CityInfo> map) {
        String obj = null;
        for (Map.Entry<String, CityInfo> entry : map.entrySet()) {
            obj = entry.getKey();
            if (obj != null) {
                break;
            }
        }
        return obj;
    }


    /**
     * 获取map中第一个数据值
     *
     * @param map 数据源
     * @return
     */
    public static CityInfo getFirstOrNull(Map<String, CityInfo> map) {
        CityInfo obj = null;
        for (Map.Entry<String, CityInfo> entry : map.entrySet()) {
            obj = entry.getValue();
            if (obj != null) {
                break;
            }
        }
        return obj;
    }
}
