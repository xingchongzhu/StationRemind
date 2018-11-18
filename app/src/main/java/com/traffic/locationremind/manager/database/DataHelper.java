package com.traffic.locationremind.manager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.item.LineSearchItem;
import com.traffic.locationremind.common.util.CommonFuction;
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

    private static final int[] dbRawId = {R.raw.cities,R.raw.shenzhen,R.raw.beijing,R.raw.guangzhou,R.raw.shanghai,R.raw.nanjing,R.raw.tianjin,
            R.raw.chongqing,R.raw.chengdu,R.raw.shenyang,R.raw.xian,R.raw.wuhan,R.raw.hangzhou,R.raw.changchun,R.raw.kunming,R.raw.dalian,
            R.raw.suzhou,R.raw.haerbin,R.raw.zhengzhou,R.raw.changsha,R.raw.ningbo,R.raw.wuxi,R.raw.qingdao,R.raw.nanning,R.raw.hefei,
            R.raw.shijiazhuang,R.raw.nanchang,R.raw.dongguan,R.raw.fuzhou,R.raw.guiyang,R.raw.xiamen,R.raw.taibei,R.raw.gaoxiong,R.raw.xianggang};

    private static final String[] dbName = {DBHelper.CITY_DB_NAME,DBHelper.SHENZHEN_DB_NAME,DBHelper.BEIJING_DB_NAME,DBHelper.GUANGZHOU_DB_NAME,
            DBHelper.SHANGHAI_DB_NAME,DBHelper.NANJING_DB_NAME,DBHelper.TIANJIN_DB_NAME,DBHelper.CHONGQING_DB_NAME,DBHelper.CHENGDU_DB_NAME,
            DBHelper.SHENYANG_DB_NAME,DBHelper.XIAN_DB_NAME,DBHelper.WUHAN_DB_NAME,DBHelper.HANGZHOU_DB_NAME,DBHelper.CHANGCHUN_DB_NAME,
            DBHelper.KUNMING_DB_NAME,DBHelper.DALIAN_DB_NAME,DBHelper.SUZHOU_DB_NAME,DBHelper.HAERBIN_DB_NAME, DBHelper.ZHENGZHOU_DB_NAME,
            DBHelper.CHANGSHA_DB_NAME,DBHelper.NINGBO_DB_NAME,DBHelper.WUXI_DB_NAME,DBHelper.QINGDAO_DB_NAME, DBHelper.NANNING_DB_NAME,
            DBHelper.HEFEI_DB_NAME,DBHelper.SHIJIAZHUANG_DB_NAME,DBHelper.NANCHANG_DB_NAME,DBHelper.DONGGUANG_DB_NAME, DBHelper.FUZHOU_DB_NAME,
            DBHelper.GUIYANG_DB_NAME,DBHelper.XIAMEN_DB_NAME,DBHelper.TAIBEI_DB_NAME,DBHelper.GAOXIONG_DB_NAME,DBHelper.XIANGGANG_DB_NAME};

    public DataHelper(Context context) {
        cityDbHelper = new DBHelper(context);
        for(int i = 0;i < dbRawId.length;i++){
            cityDbHelper.imporCityDatabase(dbRawId[i],dbName[i]);
        }
        cityDb = cityDbHelper.getWritableDatabase();
        cityDb.execSQL("CREATE TABLE IF NOT EXISTS "+DBHelper.TB_RECENT_CITY+" (id integer primary key autoincrement, name varchar(40), date INTEGER)");
    }

    public void setCityHelper(Context context,String dbName){
        if(dbHelper != null){
            dbHelper.close();
        }
        dbHelper = new DBHelper(context,dbName+".db");
        db = dbHelper.getWritableDatabase();
        creaLineSearchTable(db);
    }

    public void creaLineSearchTable(SQLiteDatabase db){
        //cityDb.execSQL("CREATE TABLE IF NOT EXISTS "+DBHelper.TB_RECENT_CITY+" (id integer primary key autoincrement, name varchar(40), date INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + SqliteHelper.TB_LINE_RESULT_INFO + "("
                + LineSearchItem.ID + " integer primary key autoincrement ,"
                + LineSearchItem.STARTLINE + " integer,"
                + LineSearchItem.ENDLINE + " integer,"
                + LineSearchItem.LINELIST + " varchar"
                + ")");
    }

    public void Close() {
        if(db != null){
            db.close();
            dbHelper.close();
            db = null;
        }
        if(cityDb != null){
            cityDb.close();
        }
    }

    public LineInfo getLineListById( int id) {
        if (db == null) {
            return null;
        }
        LineInfo lineInfo = null;
        String selection = LineInfo.LINEID + "=?" ;
        Cursor cursor = db.query(SqliteHelper.TB_LINE, null, selection, new String[]{""+id}, null,
                null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            lineInfo = convertCursorToLineInfo(cursor);
        }
        if (cursor != null)
            cursor.close();
        return lineInfo;
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

    public ArrayList<LineInfo> getLineLists( String sortType, String asc) {
        if (db == null) {
            return null;
        }
        ArrayList<LineInfo> allResourceList;
        Cursor cursor = db.query(SqliteHelper.TB_LINE, null, null, null, null,
                null, sortType + " " + asc);
        allResourceList = convertCursorToLineList(cursor);
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
                lineList.put(cursor.getInt(0),convertCursorToLineInfo(cursor));
            }
        }
        return lineList;
    }

    public LineInfo convertCursorToLineInfo(Cursor cursor){
        LineInfo lineInfo = new LineInfo();
        lineInfo.setLineid(cursor.getInt(0));
        lineInfo.setLinename(cursor.getString(1));
        lineInfo.setLineinfo(cursor.getString(2));
        lineInfo.setRGBCOOLOR(cursor.getString(3));
        lineInfo.setForward(cursor.getString(4));
        lineInfo.setReverse(cursor.getString(5));
        return  lineInfo;
    }

    public ArrayList<LineInfo> convertCursorToLineList(Cursor cursor) {
        ArrayList<LineInfo> lineList = new ArrayList<LineInfo>();
        if (cursor == null)
            return lineList;
        while (cursor.moveToNext()) {
            lineList.add(convertCursorToLineInfo(cursor));
        }
        //Log.e(TAG, "convertCursorToList");
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
        //Log.e(TAG, "convertCursorToList");
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
        //Log.e(TAG, "convertCursorToList");
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
            exitInfoList.add(exitInfo);
        }
        return exitInfoList;

    }

    public ArrayList<CityInfo> convertCursorToCityInfoList(Cursor cursor) {
        ArrayList<CityInfo> cityInfoList = new ArrayList<CityInfo>();
        if (cursor == null)
            return cityInfoList;
        while (cursor.moveToNext()) {
            CityInfo cityInfo = new CityInfo();            cityInfo.setCityName(cursor.getString(1));
            cityInfo.setPingying(cursor.getString(2));
            cityInfo.setCityNo(cursor.getString(4));
            cityInfoList.add(cityInfo);
        }
        //Log.e(TAG, "convertCursorToList");
        return cityInfoList;
    }

    public List<List<Integer>> convertCursorToLineSearchItemList(Cursor cursor) {
        List<List<Integer>> LineSearchItemList = new ArrayList<>();
        if (cursor == null)
            return LineSearchItemList;
        while (cursor.moveToNext()) {
            /*LineSearchItem lineSearchItem = new LineSearchItem();
            lineSearchItem.setStartLine(cursor.getInt(1));
            lineSearchItem.setEndLine(cursor.getInt(2));*/
            String[] lines = cursor.getString(3).split(CommonFuction.TRANSFER_SPLIT);
            List<Integer> list = new ArrayList<>();
            for(String id:lines){
                list.add(Integer.parseInt(id));
            }
            //lineSearchItem.setLineList(list);
            LineSearchItemList.add(list);
        }
        //Log.e(TAG, "convertCursorToList");
        return LineSearchItemList;
    }

    public Map<String,CityInfo> convertCursorToCityInfoMap(Cursor cursor) {
        Map<String,CityInfo> cityInfoList = new HashMap<String,CityInfo>();
        if (cursor == null)
            return cityInfoList;
        while (cursor.moveToNext()) {
            if(!cityInfoList.containsKey(cursor.getString(0))){
                CityInfo cityInfo = new CityInfo();
                cityInfo.setCityName(cursor.getString(1));
                cityInfo.setPingying(cursor.getString(2));
                cityInfo.setCityNo(cursor.getString(4));
                cityInfoList.put(cursor.getString(1),cityInfo);
            }
        }

        Log.e(TAG, "convertCursorToList");
        return cityInfoList;

    }

    public boolean insetLineSearchItem(LineSearchItem item) {
        ContentValues values = new ContentValues();
        values.put(LineSearchItem.STARTLINE, item.getStartLine());
        values.put(LineSearchItem.ENDLINE,item.getEndLine());
        values.put(LineSearchItem.LINELIST,item.getLineString());
        long rowid = db.insert(SqliteHelper.TB_LINE_RESULT_INFO, null, values);
        Log.d(TAG, "insetLineSearchItem rowid = " + rowid);
        if (rowid > 0)
            return true;
        return false;
    }

    public boolean lineItemExist(LineSearchItem item) {
        ContentValues values = new ContentValues();
        values.put(LineSearchItem.STARTLINE, item.getStartLine());
        values.put(LineSearchItem.ENDLINE,item.getEndLine());
        values.put(LineSearchItem.LINELIST,item.getLineString());
        String selection = LineSearchItem.STARTLINE+" =? and "+LineSearchItem.ENDLINE+" =? and "+LineSearchItem.LINELIST+" =?";
        Cursor cursor = db.query(SqliteHelper.TB_LINE_RESULT_INFO, null,  selection
                , new String[]{""+item.getStartLine(),""+item.getEndLine(),item.getLineString()}, null, null,
                null);
        if (cursor != null && cursor.getCount() > 0) {
            Log.d(TAG, "lineItemExist  (" + item.getStartLine() + "," + item.getEndLine());
            cursor.close();
            return true;
        }
        return false;
    }

    public List<List<Integer>> queryLineSearchItemBy(int startId,int endId) {
        List<List<Integer>> lineSearchItemList;
        String selection = LineSearchItem.STARTLINE+"=? and"+LineSearchItem.ENDLINE+"=?";
        Cursor cursor = db.query(SqliteHelper.TB_LINE_RESULT_INFO, null,  selection
                , new String[]{""+startId,""+endId}, null, null,
                null);
        lineSearchItemList = convertCursorToLineSearchItemList(cursor);
        if (cursor != null)
            cursor.close();
        return lineSearchItemList;
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
        Cursor cursor = cityDb.query(SqliteHelper.TB_CITY_INFO, null,  "name =?"
                , new String[]{cityName}, null, null,
                null);
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
        Cursor cursor = cityDb.query(SqliteHelper.TB_CITY_INFO, null, "exist=?"
                , new String[]{"" + 1}, null, null,
                null);

        Log.e(TAG, "QueryCityByCityNo");

        cityList = convertCursorToCityInfoMap(cursor);
        if (cursor != null)
            cursor.close();
        return cityList;
    }

    public List<CityInfo> getAllCityInfoList() {

        List<CityInfo> cityList;
        Cursor cursor = cityDb.query(SqliteHelper.TB_CITY_INFO, null, "exist=?"
                , new String[]{"" + 1}, null, null,
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

    public  List<StationInfo> QueryByStationLineNo(int lineNo) {

        List<StationInfo> StationInfoList;

        Cursor cursor = db.query(SqliteHelper.TB_STATION, null, StationInfo.LINEID + " =?", new String[]{"" + lineNo}, null,
                null, StationInfo.PM + " ASC");

        //Log.e(TAG, "QueryByStationLineNo");

        StationInfoList = convertCursorToStationList(cursor);
        if (cursor != null)
            cursor.close();
        return StationInfoList;
    }

    public  List<StationInfo> QueryAllByStationLineNo() {

        List<StationInfo> StationInfoList;

        Cursor cursor = db.query(SqliteHelper.TB_STATION, null, null, null, null,
                null, StationInfo.PM + " ASC");

        //Log.e(TAG, "QueryByStationLineNo");

        StationInfoList = convertCursorToStationList(cursor);
        if (cursor != null)
            cursor.close();
        return StationInfoList;
    }

    public int updateStation(String name, String lot,String lat) {
        ContentValues values = new ContentValues();
        values.put(StationInfo.LOT, lot);
        values.put(StationInfo.LAT, lat);
        return db.update(SqliteHelper.TB_STATION, values, StationInfo.CNAME+" = ?", new String[] {name});
    }

    public int updateLineInfo(int lineid, String forward,String reverse) {
        ContentValues values = new ContentValues();
        values.put(LineInfo.FORWARD, forward);
        values.put(LineInfo.REVERSE, reverse);
        return db.update(SqliteHelper.TB_LINE, values, LineInfo.LINEID+" = ?", new String[] {""+lineid});
    }

    public void updateLatLotStationEmpty() {
        ContentValues values = new ContentValues();
        values.put(StationInfo.LOT, 0);
        values.put(StationInfo.LAT, 0);
        db.update(SqliteHelper.TB_STATION, values, null,null);
    }

    public List<StationInfo> QueryByStationLineNoCanTransfer(int lineNo, String cityNo) {

        List<StationInfo> StationInfoList;

        Cursor cursor = db.query(SqliteHelper.TB_STATION, null, StationInfo.LINEID + " =?"
                        + " and " +StationInfo.TRANSFER+" != ?", new String[]{"" + lineNo,"0"}, null,
                null, StationInfo.PM + " ASC");

        //Log.e(TAG, "QueryByStationLineNo");

        StationInfoList = convertCursorToStationList(cursor);
        if (cursor != null)
            cursor.close();
        return StationInfoList;
    }

    public Map<String, StationInfo> QueryByStationAllTransfer() {

        Map<String, StationInfo> StationInfoList ;

        Cursor cursor = db.query(SqliteHelper.TB_STATION, null, StationInfo.TRANSFER + " !=?", new String[]{"0"}, null,
                null, StationInfo.PM + " ASC");

        //Log.e(TAG, "QueryByStationLineNo");

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

    public void updateExitInfor(ExitInfo exitInfo){
        long rowid =  db.delete(SqliteHelper.TB_EXIT_INFO,ExitInfo.CNAME + " = ? and " +ExitInfo.EXITNAME+ " = ?",
                new String[]{exitInfo.getCname(),exitInfo.getExitname()});
        insetExitInfo(exitInfo);
        Log.d("zxc","cname = "+exitInfo.getCname()+" exitname = "+exitInfo.getExitname()+" rowid = "+rowid);
    }

    public boolean insetExitInfo(ExitInfo exitInfo) {
        ContentValues values = new ContentValues();
        values.put(ExitInfo.CNAME, exitInfo.getCname());
        values.put(ExitInfo.EXITNAME, exitInfo.getExitname());
        values.put(ExitInfo.ADDR, exitInfo.getAddr());
        long rowid = db.insert(SqliteHelper.TB_EXIT_INFO, null, values);
        Log.d("zxc", "insetExitInfo rowid = " + rowid);
        if (rowid > 0)
            return true;
        return false;
    }

    public List<ExitInfo> QueryByExitInfoCname(String Cname) {

        List<ExitInfo> ExitInfoList;
        Cursor cursor = db.query(SqliteHelper.TB_EXIT_INFO, null, ExitInfo.CNAME + " = ?"
                , new String[]{Cname},  null, null,ExitInfo.CNAME);

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
