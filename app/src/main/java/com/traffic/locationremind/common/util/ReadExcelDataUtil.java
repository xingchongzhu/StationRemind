package com.traffic.locationremind.common.util;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.traffic.location.remind.R;
import com.traffic.locationremind.manager.bean.CityInfo;
import com.traffic.locationremind.manager.bean.ExitInfo;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataHelper;
import com.traffic.locationremind.manager.database.SqliteHelper;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ReadExcelDataUtil extends AsyncTask<Application, String, Boolean> {

	private String TAG = "ReadExcelDataUtil";
	static ReadExcelDataUtil mReadExcelDataUtil;
	private DataHelper dbHelper;

	public boolean hasWrite = false;

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

	static public ReadExcelDataUtil getInstance(){
		if(mReadExcelDataUtil == null){
			mReadExcelDataUtil = new ReadExcelDataUtil();
		}
		return mReadExcelDataUtil;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Application... params) {

		//List<CityModel> list = new ArrayList<CityModel>();
		dbHelper = new DataHelper(params[0]);

		if(dbHelper.getCount(SqliteHelper.TB_LINE) > 0) {
			return true;
		}
        Log.d(TAG,"load data.....");
        InputStream in = null;
		//in =  params[0].getResources().openRawResource(R.raw.subway);
		if(in != null){
			int rowSize = 0;
			String str = "";

			// 打开HSSFWorkbook
			POIFSFileSystem fs = null;
			try {
				fs = new POIFSFileSystem(in);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			HSSFWorkbook wb = null;
			try {
				wb = new HSSFWorkbook(fs);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			HSSFCell cell = null;
			ExitInfo mExitInfo = null;
			LineInfo mLineInfo = null;
			StationInfo mStationInfo = null;
			CityInfo mCityInfo = null;
			for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
				HSSFSheet st = wb.getSheetAt(sheetIndex);
				str = "第  " + (sheetIndex + 1) + "页"+st.getLastRowNum();
				//Log.d(TAG,str);
				//Log.d(TAG,"------------------------"+str+"-----------------------------\n");
				//publishProgress(str);
				// 第一行为标题，不取
				for (int rowIndex = 0; rowIndex <= st.getLastRowNum(); rowIndex++) {
					HSSFRow row = st.getRow(rowIndex);
					if (row == null || rowIndex == 0) {
						continue;
					}
					int tempRowSize = row.getLastCellNum() + 1;
					if (tempRowSize > rowSize) {
						rowSize = tempRowSize;
					}
					String[] values = new String[rowSize];
					Arrays.fill(values, "");
					str = "第 " + (rowIndex + 1) + "行"+row.getLastCellNum();
					//Log.d(TAG,str);
					//publishProgress(str);
					//CityModel mCityModel = new CityModel();
					str = "";
					switch (sheetIndex) {//申明表对象
						case 0:
							if(mLineInfo == null)
								mLineInfo = new LineInfo();
							break;
						case 1:
							if(mStationInfo == null)
								mStationInfo = new StationInfo();
							break;
						case 2:
							if(mExitInfo == null)
								mExitInfo = new ExitInfo();
							break;
						default:
							if(mCityInfo == null)
								mCityInfo = new CityInfo();
							break;
					}
					for (short columnIndex = 0; columnIndex <= row.getLastCellNum(); columnIndex++) {
						String value = "";
						cell = row.getCell(columnIndex);
						if (cell != null) {
							// 注意：一定要设成这个，否则可能会出现乱码
							cell.setEncoding(HSSFCell.ENCODING_UTF_16);
							switch (cell.getCellType()) {
							case HSSFCell.CELL_TYPE_STRING:
								value = cell.getStringCellValue();
								break;
							case HSSFCell.CELL_TYPE_NUMERIC:
								if (HSSFDateUtil.isCellDateFormatted(cell)) {
									Date date = cell.getDateCellValue();
									if (date != null) {
										value = new SimpleDateFormat("yyyy-MM-dd").format(date);
									} else {
										value = "";
									}
								} else {
									value = new DecimalFormat("0").format(cell.getNumericCellValue());
								}
								break;
							case HSSFCell.CELL_TYPE_FORMULA:
								// 导入时如果为公式生成的数据则无值
								if (!cell.getStringCellValue().equals("")) {
									value = cell.getStringCellValue();
								} else {
									value = cell.getNumericCellValue() + "";
								}
								break;
							case HSSFCell.CELL_TYPE_BLANK:
								break;
							case HSSFCell.CELL_TYPE_ERROR:
								value = "";
								break;
							case HSSFCell.CELL_TYPE_BOOLEAN:
								value = (cell.getBooleanCellValue() == true ? "Y" : "N");
								break;
							default:
								value = "";
							}
						}
						if (value.trim().equals("")) {
							continue;
						}
						str += "第 " + (columnIndex + 1) + "列  " + FooFileUtil.rightTrim(value)+"  ";
						//publishProgress(str);
						switch (sheetIndex) {//表
							case 0:
								switch (columnIndex) {//地铁线路表
									case 0:
										mLineInfo.setLineid(Integer.parseInt(FooFileUtil.rightTrim(value)));
										break;
									case 1:
										mLineInfo.setLinename(FooFileUtil.rightTrim(value));
										break;
									case 2:
										mLineInfo.setLineinfo(FooFileUtil.rightTrim(value));
										break;
									case 3:
										mLineInfo.setRGBCOOLOR(FooFileUtil.rightTrim(value));
										break;
									case 4:
										mLineInfo.setForward(FooFileUtil.rightTrim(value));
										break;
									case 5:
										mLineInfo.setReverse(FooFileUtil.rightTrim(value));
										break;
								}
								break;
							case 1:
								switch (columnIndex) {//地铁站台表
									case 0:
										mStationInfo.setLineid(Integer.parseInt(FooFileUtil.rightTrim(value)));
										break;
									case 1:
										mStationInfo.setPm(Integer.parseInt(FooFileUtil.rightTrim(value)));
										break;
									case 2:
										mStationInfo.setCname(FooFileUtil.rightTrim(value));
										break;
									case 3:
										mStationInfo.setPname(FooFileUtil.rightTrim(value));
										break;
									case 4:
										mStationInfo.setStationInfo(FooFileUtil.rightTrim(value));
										break;
									case 5:
										mStationInfo.setLot(FooFileUtil.rightTrim(value));
										break;
									case 6:
										mStationInfo.setLat(FooFileUtil.rightTrim(value));
										break;
									case 7:
										mStationInfo.setTransfer(FooFileUtil.rightTrim(value));
										break;
									case 8:
										mStationInfo.setAname(FooFileUtil.rightTrim(value));
										break;
									/*case 9:
										mStationInfo.setPreStation(FooFileUtil.rightTrim(value));
										break;
									case 10:
										mStationInfo.setNextStation(FooFileUtil.rightTrim(value));
										break;*/
									/*default:
										mStationInfo.setCityNo(FooFileUtil.rightTrim(value));
										break;*/
								}
								break;
							case 2:
								switch (columnIndex) {//地铁站台出口信息表
									case 0:
										mExitInfo.setCname(FooFileUtil.rightTrim(value));
										break;
									case 1:
										mExitInfo.setExitname(FooFileUtil.rightTrim(value));
										break;
									case 2:
										mExitInfo.setAddr(FooFileUtil.rightTrim(value));
										break;
									/*default:
										mExitInfo.setCityNo(FooFileUtil.rightTrim(value));
										break;*/
								}
								break;
							default:
								switch (columnIndex) {//地铁站台出口信息表
									case 0:
										mCityInfo.setCityName(FooFileUtil.rightTrim(value));
										break;
									default :
										mCityInfo.setCityName(FooFileUtil.rightTrim(value));
										break;
								}
								break;
						}

					}
					if(dbHelper != null){
						switch (sheetIndex) {//申明表对象
							case 0:
								if(mLineInfo != null)
									dbHelper.insetLineInfo(mLineInfo);
								break;
							case 1:
								if(mStationInfo != null)
									dbHelper.insetStationInfo(mStationInfo);
								break;
							case 2:
								if(mExitInfo != null)
									dbHelper.insetExitInfo(mExitInfo);
								break;
							default:
								if(mCityInfo != null)
									dbHelper.insetCityInfo(mCityInfo);
								break;
						}
					}
					//Log.d(TAG,str);
				}
			}
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dbHelper.Close();
		}else{
			return false;
			//publishProgress("数据源excel表格不存在");
		}
		
		return true;
	}

	private void saveTable(int tableid){

	}

	@Override
	protected void onProgressUpdate(String... values) {
		super.onProgressUpdate(values);
		//txtLog.append(values[0] + "\n");
		//sv.smoothScrollTo(0, 1000000);
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