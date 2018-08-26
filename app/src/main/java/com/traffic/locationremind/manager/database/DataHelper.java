package com.traffic.locationremind.manager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.traffic.location.remind.R;
import com.traffic.locationremind.manager.bean.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataHelper {

    private String TAG = "DataHelper";

    private SQLiteDatabase db;
    private SQLiteDatabase cityDb;
    private DBHelper cityDbHelper;
    private DBHelper dbHelper;
    private static DataHelper mDataHelper;

    public static DataHelper getInstance(Context context) {
        if (mDataHelper == null) {
            mDataHelper = new DataHelper(context);
        }
        return mDataHelper;
    }

    public DataHelper(Context context) {
        cityDbHelper = new DBHelper(context);
        cityDbHelper.imporCityDatabase(R.raw.cities,DBHelper.CITY_DB_NAME);
        cityDbHelper.imporCityDatabase(R.raw.shenzhen,DBHelper.SHENZHEN_DB_NAME);
        cityDbHelper.imporCityDatabase(R.raw.beijing,DBHelper.BEIJING_DB_NAME);
        cityDbHelper.imporCityDatabase(R.raw.guangzhou,DBHelper.GUANGZHOU_DB_NAME);
        cityDbHelper.imporCityDatabase(R.raw.shanghai,DBHelper.SHANGHAI_DB_NAME);
        cityDbHelper.imporCityDatabase(R.raw.nanjing,DBHelper.NANJING_DB_NAME);
        cityDbHelper.imporCityDatabase(R.raw.tianjin,DBHelper.TIANJIN_DB_NAME);
        cityDb = cityDbHelper.getWritableDatabase();
        cityDb.execSQL("CREATE TABLE IF NOT EXISTS "+DBHelper.TB_RECENT_CITY+" (id integer primary key autoincrement, name varchar(40), date INTEGER)");
    }

    public void setCityHelper(Context context,String dbName){
        if(dbHelper != null){
            dbHelper.close();
        }
        dbHelper = new DBHelper(context,dbName+".db");
        db = dbHelper.getWritableDatabase();
    }

    public void Close() {
        db.close();
        dbHelper.close();
        db = null;
    }

    public Map<Integer,LineInfo> getLineList( String sortType, String asc) {
        if (db == null) {
            return null;
        }
        Map<Integer,LineInfo> allResourceList;
        Cursor cursor = db.query(SqliteHelper.TB_LINE, null, null, null, null,
                null, sortType + " " + asc);
        allResourceList = convertCursorToLineMap(cursor);
        if (allResourceList != null)
            Log.d(TAG, TAG + " length " + allResourceList.size()
                    + " cursorcount = " + cursor.getCount() + " orderby = "
                    + sortType + asc);
        if (cursor != null)
            cursor.close();
        return allResourceList;
    }

    public Map<Integer,LineInfo> convertCursorToLineMap(Cursor cursor) {
        Map<Integer,LineInfo> lineList = new HashMap<Integer,LineInfo>();
        if (cursor == null)
            return lineList;
        while (cursor.moveToNext()) {
            if(!lineList.containsKey(cursor.getInt(0))){
                LineInfo lineInfo = new LineInfo();
                lineInfo.setLineid(cursor.getInt(0));
                lineInfo.setLinename(cursor.getString(1));
                lineInfo.setLineinfo(cursor.getString(2));
                lineInfo.setRGBCOOLOR(cursor.getString(3));
                lineInfo.setForward(cursor.getString(4));
                lineInfo.setReverse(cursor.getString(5));
                lineList.put(cursor.getInt(0),lineInfo);
            }

        }

        Log.e(TAG, "convertCursorToList");
        return lineList;

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
            lineInfo.setForward(cursor.getString(4));
            lineInfo.setReverse(cursor.getString(5));
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

    public Map<String, StationInfo> convertCursorToStationMap(Cursor cursor) {
        Map<String, StationInfo> stationInfoList = new HashMap<String, StationInfo>();
        if (cursor == null)
            return stationInfoList;
        while (cursor.moveToNext()) {
            if (!stationInfoList.containsKey(cursor.getString(2))) {
                StationInfo stationInfo = new StationInfo();
                stationInfo.setLineid(cursor.getInt(0));
                stationInfo.setPm(cursor.getInt(1));
                stationInfo.setCname(cursor.getString(2));
                stationInfo.setPname(cursor.getString(3));
                stationInfo.setAname(cursor.getString(4));
                stationInfo.setLot(cursor.getString(5));
                stationInfo.setLat(cursor.getString(6));
                //stationInfo.setPreStation(cursor.getString(7));
                //stationInfo.setNextStation(cursor.getString(8));
                stationInfo.setStationInfo(cursor.getString(7));
                stationInfo.setTransfer(cursor.getString(8));
                stationInfoList.put(stationInfo.getCname(), stationInfo);
            }

        }
        Log.e(TAG, "convertCursorToList");
        return stationInfoList;
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
            //stationInfo.setPreStation(cursor.getString(7));
            //stationInfo.setNextStation(cursor.getString(8));
            stationInfo.setStationInfo(cursor.getString(7));
            stationInfo.setTransfer(cursor.getString(8));
           // stationInfo.setCityNo(cursor.getString(11));
            stationInfoList.add(stationInfo);

        }
        Log.e(TAG, "convertCursorToList");
        return stationInfoList;

    }

    public ArrayList<ExitInfo> getExitInfoList(String sortType, String asc) {
        if (db == null) {
            return null;
        }
        ArrayList<ExitInfo> allResourceList ;
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
            //exitInfo.setCityNo(cursor.getString(3));
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
            cityInfo.setCityNo(cursor.getInt(0)+"");
            cityInfo.setCityName(cursor.getString(1));
            cityInfo.setPingying(cursor.getString(2));
            cityInfoList.add(cityInfo);
        }

        Log.e(TAG, "convertCursorToList");
        return cityInfoList;

    }

    public Map<String,CityInfo> convertCursorToCityInfoMap(Cursor cursor) {
        Map<String,CityInfo> cityInfoList = new HashMap<String,CityInfo>();
        if (cursor == null)
            return cityInfoList;
        while (cursor.moveToNext()) {
            if(!cityInfoList.containsKey(cursor.getString(0))){
                CityInfo cityInfo = new CityInfo();
                cityInfo.setCityNo(cursor.getInt(0)+"");
                cityInfo.setCityName(cursor.getString(1));
                cityInfo.setPingying(cursor.getString(2));
                cityInfoList.put(cursor.getString(1),cityInfo);
            }
        }

        Log.e(TAG, "convertCursorToList");
        return cityInfoList;

    }

    public boolean insetCityInfo(CityInfo cityInfo) {
        ContentValues values = new ContentValues();

        //values.put(CityInfo.CITYNO, cityInfo.getCityNo());
        values.put(CityInfo.CITYNAME, cityInfo.getCityName());
        values.put(CityInfo.PINGYING,cityInfo.getPingying());
        long rowid = cityDb.insert(SqliteHelper.TB_CITY_INFO, null, values);
        Log.d(TAG, "insetCityInfo rowid = " + rowid);
        if (rowid > 0)
            return true;
        return false;
    }

    public boolean insetAddExtraInfo(AddInfo addInfo) {
        ContentValues values = new ContentValues();

        values.put(AddInfo.LAT, addInfo.getLat());
        values.put(AddInfo.LOT, addInfo.getLot());
        values.put(AddInfo.NAME, addInfo.getName());
        long rowid = db.insert(SqliteHelper.TB_ADD_EXTRA_INFO, null, values);
        Log.d(TAG, "insetAddExtraInfo rowid = " + rowid);
        if (rowid > 0)
            return true;
        return false;
    }

    public List<CityInfo> QueryCityByCityNo(String cityName) {

        List<CityInfo> cityList;
        Cursor cursor = cityDb.query(SqliteHelper.TB_CITY_INFO, null, CityInfo.CITYNAME + " =?"
                , new String[]{cityName}, null, null,
                null);

        Log.e(TAG, "QueryCityByCityNo");

        cityList = convertCursorToCityInfoList(cursor);
        if (cursor != null)
            cursor.close();
        return cityList;
    }

    public SQLiteDatabase getSQLiteDatabase(){
        return db;
    }

    public SQLiteDatabase getCitySQLiteDatabase(){
        return cityDb;
    }

    public Map<String,CityInfo> getAllCityInfo() {

        Map<String,CityInfo> cityList;
        Cursor cursor = cityDb.query(SqliteHelper.TB_CITY_INFO, null, null
                , null, null, null,
                null);

        Log.e(TAG, "QueryCityByCityNo");

        cityList = convertCursorToCityInfoMap(cursor);
        if (cursor != null)
            cursor.close();
        return cityList;
    }

    public List<CityInfo> getAllCityInfoList() {

        List<CityInfo> cityList;
        Cursor cursor = cityDb.query(SqliteHelper.TB_CITY_INFO, null, null
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
        values.put(LineInfo.RGBCOOLOR, lineInfo.getRGBCOOLOR());
        //values.put(CityInfo.CITYNO, lineInfo.getCityNo());
        values.put(LineInfo.FORWARD, lineInfo.getForwad());
        values.put(LineInfo.REVERSE, lineInfo.getReverse());
        long rowid = db.insert(SqliteHelper.TB_LINE, null, values);
        Log.d(TAG, "insetLineInfo rowid = " + rowid);
        if (rowid > 0)
            return true;
        return false;
    }

    public List<LineInfo> QueryByLineNo(String lineNo) {

        List<LineInfo> lineList;
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

        List<LineInfo> lineList;
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
        //values.put(StationInfo.PRESTATION, stationInfo.getPreStation());
        //values.put(StationInfo.NEXTSTATION, stationInfo.getNextStation());
        values.put(StationInfo.STATIONINFO, stationInfo.getStationInfo());
        values.put(StationInfo.TRANSFER, stationInfo.getTransfer());
        //values.put(CityInfo.CITYNO, stationInfo.getCityNo());
        long rowid = db.insert(SqliteHelper.TB_STATION, null, values);
        Log.d(TAG, "insetStationInfo rowid = " + rowid);
        if (rowid > 0)
            return true;
        return false;
    }

    public  List<StationInfo> QueryByStationLineNo(int lineNo, String cityNo) {

        List<StationInfo> StationInfoList;

        Cursor cursor = db.query(SqliteHelper.TB_STATION, null, StationInfo.LINEID + " =?", new String[]{"" + lineNo}, null,
                null, StationInfo.PM + " ASC");

        Log.e(TAG, "QueryByStationLineNo");

        StationInfoList = convertCursorToStationList(cursor);
        if (cursor != null)
            cursor.close();
        return StationInfoList;
    }

    public List<StationInfo> QueryByStationLineNoCanTransfer(int lineNo, String cityNo) {

        List<StationInfo> StationInfoList;

        Cursor cursor = db.query(SqliteHelper.TB_STATION, null, StationInfo.LINEID + " =?"
                        + " and " +StationInfo.TRANSFER+" != ?", new String[]{"" + lineNo,"0"}, null,
                null, StationInfo.PM + " ASC");

        Log.e(TAG, "QueryByStationLineNo");

        StationInfoList = convertCursorToStationList(cursor);
        if (cursor != null)
            cursor.close();
        return StationInfoList;
    }

    public Map<String, StationInfo> QueryByStationAllTransfer() {

        Map<String, StationInfo> StationInfoList ;

        Cursor cursor = db.query(SqliteHelper.TB_STATION, null, StationInfo.TRANSFER + " !=?", new String[]{"0"}, null,
                null, StationInfo.PM + " ASC");

        Log.e(TAG, "QueryByStationLineNo");

        StationInfoList = convertCursorToStationMap(cursor);
        if (cursor != null)
            cursor.close();
        return StationInfoList;
    }

    public List<LineInfo> QueryByStationname(String stationName) {

        List<LineInfo> lineList ;
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
        //values.put(CityInfo.CITYNO, exitInfo.getCityNo());
        long rowid = db.insert(SqliteHelper.TB_EXIT_INFO, null, values);
        Log.d(TAG, "insetExitInfo rowid = " + rowid);
        if (rowid > 0)
            return true;
        return false;
    }

    public List<ExitInfo> QueryByExitInfoCname(String Cname) {

        List<ExitInfo> ExitInfoList;
        Cursor cursor = db.query(SqliteHelper.TB_EXIT_INFO, null, ExitInfo.CNAME + " = ?"
                , new String[]{Cname}, null, null,
                ExitInfo.CNAME);

        Log.e(TAG, "QueryByExitInfoCname");

        ExitInfoList = convertCursorToExitInfoList(cursor);
        if (cursor != null)
            cursor.close();
        return ExitInfoList;
    }

    public List<ExitInfo> QueryByExitInfoCnameAndExitName(String stationName, String exitName) {

        List<ExitInfo> ExitInfoList;
        String selection = ExitInfo.CNAME + "=? and " + ExitInfo.EXITNAME + "=?" ;
        Cursor cursor = db.query(SqliteHelper.TB_EXIT_INFO, null, selection
                , new String[]{stationName, exitName}, null, null,
                ExitInfo.CNAME);

        Log.e(TAG, "QueryByExitInfoCnameAndExitName");

        ExitInfoList = convertCursorToExitInfoList(cursor);
        if (cursor != null)
            cursor.close();
        return ExitInfoList;
    }


}
