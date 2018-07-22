package com.traffic.locationremind.manager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.traffic.locationremind.manager.bean.CityInfo;
import com.traffic.locationremind.manager.bean.ExitInfo;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;

import java.util.ArrayList;
import java.util.List;

public class DataHelper {

    private static String DB_NAME = "shenzhenmetro.db";
    private String TAG = "DataHelper";

    private static int DB_VERSION = 1;

    private SQLiteDatabase db;
    private SqliteHelper dbHelper;
    private static DataHelper mDataHelper;

    public static DataHelper getInstance(Context context){
        if(mDataHelper == null){
            mDataHelper = new DataHelper(context);
        }
        return mDataHelper;
    }

    public DataHelper(Context context) {
        dbHelper = new SqliteHelper(context, DB_NAME, null, DB_VERSION);
        db = dbHelper.getWritableDatabase();
    }

    public void Close() {
        db.close();
        dbHelper.close();
        db = null;
    }

    public ArrayList<LineInfo> getLineList(String cityNo,String sortType, String asc) {
        if (db == null) {
            return null;
        }
        ArrayList<LineInfo> allResourceList = new ArrayList<LineInfo>();
        Cursor cursor = db.query(SqliteHelper.TB_LINE, null, CityInfo.CITYNO+" =?", new String[]{cityNo}, null,
                null, sortType+" " + asc);
        allResourceList = convertCursorToLineList(cursor);
        if (allResourceList != null)
            Log.d(TAG, TAG + " length " + allResourceList.size()
                    + " cursorcount = " + cursor.getCount() + " orderby = "
                    + sortType + asc);
        if(cursor != null)
            cursor.close();
        return allResourceList;
    }

    public ArrayList<LineInfo> convertCursorToLineList(Cursor cursor) {
        ArrayList<LineInfo> lineList = new ArrayList<LineInfo>();
        if (cursor == null)
            return lineList;
        while (cursor.moveToNext()) {
            LineInfo lineInfo = new LineInfo();
            lineInfo.setLineid(cursor.getInt(0));
            lineInfo.setLinename(cursor.getString(1));
            lineInfo.setLineinfo(cursor.getString(2));
            lineInfo.setRGBCOOLOR(cursor.getString(3));
            lineInfo.setCityNo(cursor.getString(4));
            lineList.add(lineInfo);
        }

        Log.e(TAG, "convertCursorToList");
        return lineList;

    }

    public ArrayList<StationInfo> getStationInfoList(String sortType, String asc) {
        if (db == null) {
            return null;
        }
        ArrayList<StationInfo> allResourceList = new ArrayList<StationInfo>();
        Cursor cursor = db.query(SqliteHelper.TB_STATION, null, null, null, null,
                null, sortType + asc);
        allResourceList = convertCursorToStationList(cursor);
        if (allResourceList != null)
            Log.d(TAG, TAG + " length " + allResourceList.size()
                    + " cursorcount = " + cursor.getCount() + " orderby = "
                    + sortType + asc);
        cursor.close();
        return allResourceList;
    }

    public ArrayList<StationInfo> convertCursorToStationList(Cursor cursor) {
        ArrayList<StationInfo> stationInfoList = new ArrayList<StationInfo>();
        if (cursor == null)
            return stationInfoList;
        while (cursor.moveToNext()) {
            StationInfo stationInfo = new StationInfo();
            stationInfo.setLineid(cursor.getInt(0));
            stationInfo.setPm(cursor.getInt(1));
            stationInfo.setCname(cursor.getString(2));
            stationInfo.setPname(cursor.getString(3));
            stationInfo.setAname(cursor.getString(4));
            stationInfo.setLot(cursor.getString(5));
            stationInfo.setLat(cursor.getString(6));
            stationInfo.setPreStation(cursor.getString(7));
            stationInfo.setNextStation(cursor.getString(8));
            stationInfo.setStationInfo(cursor.getString(9));
            stationInfo.setTransfer(cursor.getString(10));
            stationInfoList.add(stationInfo);
        }

        Log.e(TAG, "convertCursorToList");
        return stationInfoList;

    }

    public ArrayList<ExitInfo> getExitInfoList(String sortType, String asc) {
        if (db == null) {
            return null;
        }
        ArrayList<ExitInfo> allResourceList = new ArrayList<ExitInfo>();
        Cursor cursor = db.query(SqliteHelper.TB_EXIT_INFO, null, null, null, null,
                null, sortType + asc);
        allResourceList = convertCursorToExitInfoList(cursor);
        if (allResourceList != null)
            Log.d(TAG, TAG + "  length " + allResourceList.size()
                    + " cursorcount = " + cursor.getCount() + " orderby = "
                    + sortType + asc);
        cursor.close();
        return allResourceList;
    }

    public ArrayList<ExitInfo> convertCursorToExitInfoList(Cursor cursor) {
        ArrayList<ExitInfo> exitInfoList = new ArrayList<ExitInfo>();
        if (cursor == null)
            return exitInfoList;
        while (cursor.moveToNext()) {
            ExitInfo exitInfo = new ExitInfo();
            exitInfo.setCname(cursor.getString(0));
            exitInfo.setExitname(cursor.getString(1));
            exitInfo.setAddr(cursor.getString(2));
            exitInfo.setCityNo(cursor.getString(3));
            exitInfoList.add(exitInfo);
        }

        Log.e(TAG, "convertCursorToList");
        return exitInfoList;

    }

    public ArrayList<CityInfo> convertCursorToCityInfoList(Cursor cursor) {
        ArrayList<CityInfo> cityInfoList = new ArrayList<CityInfo>();
        if (cursor == null)
            return cityInfoList;
        while (cursor.moveToNext()) {
            CityInfo cityInfo = new CityInfo();
            cityInfo.setCityNo(cursor.getString(0));
            cityInfo.setCityName(cursor.getString(1));
            cityInfoList.add(cityInfo);
        }

        Log.e(TAG, "convertCursorToList");
        return cityInfoList;

    }

    public boolean insetCityInfo(CityInfo cityInfo) {
        ContentValues values = new ContentValues();

        values.put(CityInfo.CITYNO, cityInfo.getCityNo());
        values.put(CityInfo.CITYNAME, cityInfo.getCityName());
        long rowid = db.insert(SqliteHelper.TB_CITY_INFO, null, values);
        Log.d(TAG, "insetCityInfo rowid = " + rowid);
        if (rowid > 0)
            return true;
        return false;
    }

    public List<CityInfo> QueryCityByCityNo(String cityNo) {

        List<CityInfo> cityList = new ArrayList<CityInfo>();
        Cursor cursor = db.query(SqliteHelper.TB_CITY_INFO, null, CityInfo.CITYNO+" =?"
                , new String[]{cityNo}, null, null,
                null);

        Log.e(TAG, "QueryCityByCityNo");

        cityList = convertCursorToCityInfoList(cursor);
        if (cursor != null)
            cursor.close();
        return cityList;
    }

    public List<CityInfo> getAllCityInfo() {

        List<CityInfo> cityList = new ArrayList<CityInfo>();
        Cursor cursor = db.query(SqliteHelper.TB_CITY_INFO, null, null
                , null, null, null,
                null);

        Log.e(TAG, "QueryCityByCityNo");

        cityList = convertCursorToCityInfoList(cursor);
        if (cursor != null)
            cursor.close();
        return cityList;
    }


    public int getCount(String tbName) {
        Cursor cursor = db.query(tbName, null, null, null, null,
                null, null);
        int n = 0;
        if (cursor != null) {
            n = cursor.getCount();
            cursor.close();
        }
        return n;
    }

    public boolean insetLineInfo(LineInfo lineInfo) {
        ContentValues values = new ContentValues();

        values.put(LineInfo.LINEID, lineInfo.getLineid());
        values.put(LineInfo.LINENAME, lineInfo.getLinename());
        values.put(LineInfo.LINEINFO, lineInfo.getLineinfo());
        values.put(LineInfo.RGBCOOLOR,lineInfo.getRGBCOOLOR());
        values.put(CityInfo.CITYNO,lineInfo.getCityNo());
        long rowid = db.insert(SqliteHelper.TB_LINE, null, values);
        Log.d(TAG, "insetLineInfo rowid = " + rowid);
        if (rowid > 0)
            return true;
        return false;
    }

    public List<LineInfo> QueryByLineNo(String lineNo) {

        List<LineInfo> lineList = new ArrayList<LineInfo>();
        Cursor cursor = db.query(SqliteHelper.TB_LINE, null, LineInfo.LINEID
                , new String[]{lineNo}, null, null,
                null);

        Log.e(TAG, "QueryByProvince");

        lineList = convertCursorToLineList(cursor);
        if (cursor != null)
            cursor.close();
        return lineList;
    }

    public List<LineInfo> QueryByLinename(String linename) {

        List<LineInfo> lineList = new ArrayList<LineInfo>();
        Cursor cursor = db.query(SqliteHelper.TB_LINE, null, LineInfo.LINENAME
                , new String[]{linename}, null, null,
                null);

        Log.e(TAG, "QueryByProvince");

        lineList = convertCursorToLineList(cursor);
        if (cursor != null)
            cursor.close();
        return lineList;
    }


    public boolean insetStationInfo(StationInfo stationInfo) {
        ContentValues values = new ContentValues();
        values.put(StationInfo.LINEID, stationInfo.getLineid());
        values.put(StationInfo.PM, stationInfo.getPm());
        values.put(StationInfo.CNAME, stationInfo.getCname());
        values.put(StationInfo.PNAME, stationInfo.getPname());
        values.put(StationInfo.ANAME, stationInfo.getAname());
        values.put(StationInfo.LOT, stationInfo.getLot());
        values.put(StationInfo.LAT, stationInfo.getLat());
        values.put(StationInfo.PRESTATION, stationInfo.getPreStation());
        values.put(StationInfo.NEXTSTATION, stationInfo.getNextStation());
        values.put(StationInfo.STATIONINFO, stationInfo.getStationInfo());
        values.put(StationInfo.TRANSFER, stationInfo.getTransfer());
        values.put(CityInfo.CITYNO,stationInfo.getCityNo());
        long rowid = db.insert(SqliteHelper.TB_STATION, null, values);
        Log.d(TAG, "insetStationInfo rowid = " + rowid);
        if (rowid > 0)
            return true;
        return false;
    }

    public List<StationInfo> QueryByStationLineNo(int lineNo,String cityNo) {

        List<StationInfo> StationInfoList = new ArrayList<StationInfo>();

        Cursor cursor = db.query(SqliteHelper.TB_STATION, null, StationInfo.LINEID+" =?"+" and "+CityInfo.CITYNO+" = ?", new String[]{""+lineNo,cityNo}, null,
                null, StationInfo.PM+" ASC");

        Log.e(TAG, "QueryByStationLineNo");

        StationInfoList = convertCursorToStationList(cursor);
        if (cursor != null)
            cursor.close();
        return StationInfoList;
    }

    public List<LineInfo> QueryByStationname(String stationName) {

        List<LineInfo> lineList = new ArrayList<LineInfo>();
        Cursor cursor = db.query(SqliteHelper.TB_STATION, null, StationInfo.CNAME
                , new String[]{stationName}, null, null,
                null);

        Log.e(TAG, "QueryByStationname");

        lineList = convertCursorToLineList(cursor);
        if (cursor != null)
            cursor.close();
        return lineList;
    }

    public boolean insetExitInfo(ExitInfo exitInfo) {
        ContentValues values = new ContentValues();

        values.put(ExitInfo.CNAME, exitInfo.getCname());
        values.put(ExitInfo.EXITNAME, exitInfo.getExitname());
        values.put(ExitInfo.ADDR, exitInfo.getAddr());
        values.put(CityInfo.CITYNO,exitInfo.getCityNo());
        long rowid = db.insert(SqliteHelper.TB_EXIT_INFO, null, values);
        Log.d(TAG, "insetExitInfo rowid = " + rowid);
        if (rowid > 0)
            return true;
        return false;
    }

    public List<ExitInfo> QueryByExitInfoCname(String Cname,String cityNo) {

        List<ExitInfo> ExitInfoList = new ArrayList<ExitInfo>();
        Cursor cursor = db.query(SqliteHelper.TB_EXIT_INFO, null, ExitInfo.CNAME+" = ?"+" and "+CityInfo.CITYNO+" =?"
                , new String[]{Cname,cityNo}, null, null,
                ExitInfo.CNAME);

        Log.e(TAG, "QueryByExitInfoCname");

        ExitInfoList = convertCursorToExitInfoList(cursor);
        if (cursor != null)
            cursor.close();
        return ExitInfoList;
    }

    public List<ExitInfo> QueryByExitInfoCnameAndExitName(String stationName, String exitName,String cityNo) {

        List<ExitInfo> ExitInfoList = new ArrayList<ExitInfo>();
        String selection = ExitInfo.CNAME + "=? and " + ExitInfo.EXITNAME + "=?"+" and "+CityInfo.CITYNO+" =?";
        Cursor cursor = db.query(SqliteHelper.TB_EXIT_INFO, null, selection
                , new String[]{stationName, exitName,cityNo}, null, null,
                ExitInfo.CNAME);

        Log.e(TAG, "QueryByExitInfoCnameAndExitName");

        ExitInfoList = convertCursorToExitInfoList(cursor);
        if (cursor != null)
            cursor.close();
        return ExitInfoList;
    }


}
