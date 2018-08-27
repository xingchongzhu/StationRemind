package com.traffic.locationremind.manager.database;

import java.io.*;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.traffic.location.remind.R;

public class DBHelper extends SQLiteOpenHelper {
	private static final int DB_VERSION = 3;
	private static String DB_PATH = "/data/data/com.traffic.location.remind/databases/";

	private static String DB_NAME = "metroinfo.db";
	private static String ASSETS_NAME = "metroinfo.db";
	public static final String TB_RECENT_CITY = "recentcity";
	public static String CITY_DB_NAME = "cities.db";
	public static String SHENZHEN_DB_NAME = "shenzhen.db";
	public static String BEIJING_DB_NAME = "beijing.db";
	public static String GUANGZHOU_DB_NAME = "guangzhou.db";
	public static String SHANGHAI_DB_NAME = "shanghai.db";
	public static String NANJING_DB_NAME = "nanjing.db";
	public static String TIANJIN_DB_NAME = "tianjin.db";
	public static String CHONGQING_DB_NAME = "chongqing.db";

	private final Context myContext;

	private static final int ASSETS_SUFFIX_BEGIN = 101;
	private static final int ASSETS_SUFFIX_END = 103;

	public DBHelper(Context context, String name, CursorFactory factory,
					int version) {
		super(context, name, null, version);
		this.myContext = context;
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		//Log.d("info", "create table");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(newVersion > oldVersion){
			imporDatabase();
		}
	}

	public DBHelper(Context context, String name, int version) {
		this(context, name, null, version);
	}

	public DBHelper(Context context, String name) {
		this(context, name, DB_VERSION);
	}

	public DBHelper(Context context) {
		this(context,  CITY_DB_NAME);
	}

	public void imporDatabase() {
		//存放数据库的目录
		String dirPath = "/data/data/" + myContext.getPackageName() + "/databases";
		File dir = new File(dirPath);
		if(!dir.exists()) {
			dir.mkdir();
		}
		//数据库文件
		File file = new File(dir, DB_NAME);
		try {
			if(!file.exists()) {
				file.createNewFile();
			}
			//加载需要导入的数据库
			InputStream is = myContext.getResources().openRawResource(R.raw.metroinfo);
			FileOutputStream fos = new FileOutputStream(file);
			byte[] buffere=new byte[is.available()];
			is.read(buffere);
			fos.write(buffere);
			is.close();
			fos.close();
		}catch(FileNotFoundException  e){
			e.printStackTrace();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void imporCityDatabase(int id,String name) {
		//存放数据库的目录
		String dirPath = "/data/data/" + myContext.getPackageName() + "/databases";
		File dir = new File(dirPath);
		if(!dir.exists()) {
			dir.mkdir();
		}
		//数据库文件
		File file = new File(dir, name);
		try {
			if(!file.exists()) {
				file.createNewFile();
			}
			//加载需要导入的数据库
			InputStream is = myContext.getResources().openRawResource(id);
			FileOutputStream fos = new FileOutputStream(file);
			byte[] buffere=new byte[is.available()];
			is.read(buffere);
			fos.write(buffere);
			is.close();
			fos.close();
		}catch(FileNotFoundException  e){
			e.printStackTrace();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private void copyBigDataBase() throws IOException {
		InputStream myInput;
		String outFileName = DB_PATH + DB_NAME;
		OutputStream myOutput = new FileOutputStream(outFileName);
		for (int i = ASSETS_SUFFIX_BEGIN; i < ASSETS_SUFFIX_END + 1; i++) {
			myInput = myContext.getAssets().open(ASSETS_NAME + "." + i);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = myInput.read(buffer)) > 0) {
				myOutput.write(buffer, 0, length);
			}
			myOutput.flush();
			myInput.close();
		}
		myOutput.close();
	}


}