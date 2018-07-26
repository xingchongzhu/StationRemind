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

package com.traffic.locationremind.common.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import com.traffic.location.remind.R;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathSerachUtil {
	static String TAG = "PathSerachUtil";
	static boolean debug = false;
	/**
	 * 往图片上写入文字、图片等内容
	 */
	public static void findLinedStation(LineInfo lineInfo, StationInfo start, StationInfo end, List<StationInfo> list){
		List<StationInfo> stationInfoList = lineInfo.getStationInfoList();
		if(start == null || end == null){
			return;
		}
		Map<String,StationInfo> currentAllStationList = new HashMap<>();
		for(StationInfo stationInfo:list){
			currentAllStationList.put(stationInfo.getCname(),stationInfo);
		}
		Log.d(TAG,"  start.pm  = "+start.pm+" end.pm = "+end.pm);
		if(start.pm < end.pm){
			int next = start.pm;
			for (StationInfo mStationInfo:stationInfoList){
				if(next == mStationInfo.pm && !currentAllStationList.containsKey(mStationInfo.getCname())){
					currentAllStationList.put(mStationInfo.getCname(),mStationInfo);
					list.add(mStationInfo);
					if(debug)
						Log.d(TAG,mStationInfo.getCname()+"   ");
					if(next == end.pm){
						break;
					}
					next++;
				}
			}
		}else{
			int next = start.pm;
			int size = stationInfoList.size()-1;
			for (int i = size ; i >= 0;i--){
				StationInfo mStationInfo = stationInfoList.get(i);
				if(next == mStationInfo.pm && !currentAllStationList.containsKey(mStationInfo.getCname())){
					currentAllStationList.put(mStationInfo.getCname(),mStationInfo);
					list.add(mStationInfo);
					if(debug)
						Log.d(TAG,mStationInfo.getCname()+"   ");
					if(next == end.pm){
						break;
					}
					next--;
				}
			}
		}
		currentAllStationList.clear();
	}


	//查询站台是否与目标线路有相同线路
	public static int isSameLine(StationInfo start,int lined){
		final String lines[] = start.getTransfer().split(CommonFuction.TRANSFER_SPLIT);
		int size = lines.length;
		for(int i = 0;i < size;i++){//找出和终点站相同点不用换乘
			if(CommonFuction.convertToInt(lines[i],0) == lined){//找到相同线路站不用换乘
				return CommonFuction.convertToInt(lines[i],0);
			}
		}
		return -1;
	}
}
