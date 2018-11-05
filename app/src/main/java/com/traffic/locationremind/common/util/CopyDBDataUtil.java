package com.traffic.locationremind.common.util;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;
import com.traffic.location.remind.R;
import com.traffic.locationremind.manager.bean.CityInfo;
import com.traffic.locationremind.manager.bean.ExitInfo;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DBHelper;
import com.traffic.locationremind.manager.database.DataHelper;
import com.traffic.locationremind.manager.database.SqliteHelper;
//import org.apache.poi.hssf.usermodel.*;
//import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CopyDBDataUtil extends AsyncTask<Application, String, Boolean> {

	private String TAG = "ReadExcelDataUtil";
	static CopyDBDataUtil mCopyDBDataUtil;
	public boolean hasWrite = true;

	static public CopyDBDataUtil getInstance(){
		if(mCopyDBDataUtil == null){
			mCopyDBDataUtil = new CopyDBDataUtil();
		}
		return mCopyDBDataUtil;
	}

	private List<DbWriteFinishListener> mDbWriteFinishListenerList = new ArrayList<>();

	public void addDbWriteFinishListener(DbWriteFinishListener mDbWriteFinishListener){
		mDbWriteFinishListenerList.add(mDbWriteFinishListener);
	}

	public void removeDbWriteFinishListener(DbWriteFinishListener mDbWriteFinishListener){
		mDbWriteFinishListenerList.remove(mDbWriteFinishListener);
	}

	private void notificationAll(){
		final int mDbWriteFinishListenerList_size = mDbWriteFinishListenerList.size();// Moved  mDbWriteFinishListenerList.size() call out of the loop to local variable mDbWriteFinishListenerList_size
		for(int n = 0; n< mDbWriteFinishListenerList_size; n++){
			mDbWriteFinishListenerList.get(n).dbWriteFinishNotif();
		}
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Application... params) {
		/*try {
			*//*DBHelper dbHelper = new DBHelper(params[0],DBHelper.DB_CITY_NAME);
			dbHelper.createDataBase(DBHelper.DB_CITY_NAME);
			dbHelper.close();

			dbHelper = new DBHelper(params[0],DBHelper.DB_METRA_NAME);
			dbHelper.createDataBase(DBHelper.DB_METRA_NAME);
			dbHelper.close();*//*
			//DBHelper.createDataBase(params[0]);
		} catch (IOException e) {
			hasWrite = false;
			e.printStackTrace();
		}*/
		return true;
	}

	@Override
	protected void onProgressUpdate(String... values) {
		super.onProgressUpdate(values);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		Log.d(TAG, "database init finish result = "+result);
		if(result)
			hasWrite = true;
		notificationAll();
	}

	public interface DbWriteFinishListener{
		void dbWriteFinishNotif();
	}
}