

package com.traffic.locationremind.common.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.text.TextUtils;

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

	public static final double RANDDIS = 0.5;
	private static final double EARTH_RADIUS = 6378137.0;
	public static final String SHNAME = "subwab_info";
	public static final String CITYNO = "cityno";
	public static final String TRANSFER_SPLIT = ",";
	public final static String HEAD = "head";
	public final static String TAIL = "tail";


	public static void writeSharedPreferences(Context context,String key,String value){
		SharedPreferences sp = context.getSharedPreferences(SHNAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}

    public static void writeBooleanSharedPreferences(Context context,String key,boolean value){
        SharedPreferences sp = context.getSharedPreferences(SHNAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

	public static void writeIntSharedPreferences(Context context,String key,int value){
		SharedPreferences sp = context.getSharedPreferences(SHNAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public static String getSharedPreferencesValue(Context context,String key){
		SharedPreferences sp = context.getSharedPreferences(SHNAME, Context.MODE_PRIVATE);
		String name = sp.getString(key, "");
		return name;
	}

    public static boolean getSharedPreferencesBooleanValue(Context context,String key){
        SharedPreferences sp = context.getSharedPreferences(SHNAME, Context.MODE_PRIVATE);
        boolean state = sp.getBoolean(key, false);
        return state;
    }

	public static int getSharedPreferencesIntValue(Context context,String key){
		SharedPreferences sp = context.getSharedPreferences(SHNAME, Context.MODE_PRIVATE);
		int no = sp.getInt(key, 0);
		return no;
	}

	public static Bitmap getbitmap(Bitmap bm, int newWidth, int newHeight){
		if (bm == null)
		{
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
		if (bm != null & !bm.isRecycled())
		{
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

	public static double getDistanceLat(double lng1,double lat1, double lng2, double lat2)
	{
		double earthRadius = 6367000; //approximate radius of earth in meters
        /*
        Convert these degrees to radians
        to work with the formula
        */
		 lat1 = (lat1 * Math.PI ) / 180;
		lng1 = (lng1 * Math.PI ) / 180;

		lat2 = (lat2 * Math.PI ) / 180;
		lng2 = (lng2 * Math.PI ) / 180;
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
}
