/**
 * Copyright (C) 2015~2050 by foolstudio. All rights reserved.
 * 
 * ��Դ�ļ��д��벻����������������ҵ��;�����߱�������Ȩ��
 * 
 * ���ߣ�������
 * 
 * �������䣺foolstudio@qq.com
 * 
*/

package com.traffic.locationremind.manager.database;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.traffic.location.remind.R;
import com.traffic.locationremind.manager.bean.CityInfo;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

public class DataManager{

	private DataHelper mDataHelper;//数据库
	private CityInfo currentCityNo = null;
	private Map<String,CityInfo> cityInfoList;//所有城市信息
	private Map<Integer,LineInfo> mLineInfoList;//地图线路
	private List<StationInfo> mStationInfoList ;//地图站台信息
	private Object mLock = new Object();

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
