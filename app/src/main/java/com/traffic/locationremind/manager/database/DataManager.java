package com.traffic.locationremind.manager.database;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.activity.MainViewActivity;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.manager.bean.CityInfo;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

public class DataManager{

	private DataHelper mDataHelper;//数据库
	private Map<String,CityInfo> cityInfoList;//所有城市信息
	private Map<Integer,LineInfo> mLineInfoList;//地图线路
	private CityInfo currentCityNo = null;
	private Object mLock = new Object();

	private static DataManager mDataManager;

	public DataManager(Context context){
		this.mDataHelper = DataHelper.getInstance(context);
		new MyAsyncTask().execute(context);
	}

	public static DataManager getInstance(Context context){
		if(mDataManager == null){
			mDataManager = new DataManager(context);
		}
		return mDataManager;
	}

	class MyAsyncTask extends AsyncTask<Context,Void,Map<Integer,LineInfo>> {

		//onPreExecute用于异步处理前的操作
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		//在doInBackground方法中进行异步任务的处理.
		@Override
		protected Map<Integer,LineInfo> doInBackground(Context... params) {
			cityInfoList = mDataHelper.getAllCityInfo();
			//获取传进来的参数
			Context contexts = params[0];
			String shpno = CommonFuction.getSharedPreferencesValue((Context) params[0], CommonFuction.CITYNO);
			if (!TextUtils.isEmpty(shpno)) {
				currentCityNo = cityInfoList.get(shpno);
			}
			if (currentCityNo == null) {
				currentCityNo = CommonFuction.getFirstOrNull(cityInfoList);
			}
			Map<Integer,LineInfo> list= mDataHelper.getLineList(currentCityNo.getCityNo(), LineInfo.LINEID, "ASC");
			for (Map.Entry<Integer,LineInfo> entry : list.entrySet()) {
				entry.getValue().setStationInfoList(mDataHelper.QueryByStationLineNo(entry.getKey(), currentCityNo.getCityNo()));
			}
			return list;
		}

		//onPostExecute用于UI的更新.此方法的参数为doInBackground方法返回的值.
		@Override
		protected void onPostExecute(Map<Integer,LineInfo> list) {
			super.onPostExecute(list);
			mLineInfoList = list;
		}
	}

	WeakReference<Callbacks> mCallbacks;
	public interface Callbacks {

	}

	/**
	 * Set this as the current Launcher activity object for the loader.
	 */
	public void initialize(Callbacks callbacks) {
		synchronized (mLock) {
			mCallbacks = new WeakReference<>(callbacks);
		}
	}

}
