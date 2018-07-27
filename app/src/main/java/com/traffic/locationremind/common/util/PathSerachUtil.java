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

import java.util.*;

public class PathSerachUtil {
	private static final int MAXLINENUMBER = 20;
	private static final int MAXRECOMENDLINENUMBER = 5;
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
		//Log.d(TAG,"起点 "+start.getCname()+" 终点 "+end.getCname()+" lineInfo.line = "+lineInfo.lineid);
		//Log.d(TAG,"  start.pm  = "+start.pm+" end.pm = "+end.pm);
		if(start.pm < end.pm){
			int next = start.pm;
			for (StationInfo mStationInfo:stationInfoList){
				if(next == mStationInfo.pm ){
					int size = list.size();
					if(size >= 1){
						if(list.get(size-1).getCname().equals(mStationInfo.getCname())){
							list.remove(size-1);
						}
					}
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
				if(next == mStationInfo.pm ){
					int size1 = list.size();
					if(size1 >= 1){
						if(list.get(size1-1).getCname().equals(mStationInfo.getCname())){
							list.remove(size1-1);
						}
					}
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
	}

	public static List<Map.Entry<List<Integer>,List<StationInfo>>> getLastRecomendLines(Map<List<Integer>,List<StationInfo>> currentAllStationList){
		List<Map.Entry<List<Integer>,List<StationInfo>>> infoIds = new ArrayList<Map.Entry<List<Integer>,List<StationInfo>>>(currentAllStationList.entrySet());

		Collections.sort(infoIds, new Comparator<Map.Entry<List<Integer>,List<StationInfo>>>() {
			/*
			 * 返回负数表示：p1 小于p2，
			 * 返回0 表示：p1和p2相等，
			 * 返回正数表示：p1大于p2
			 */
			public int compare(Map.Entry<List<Integer>,List<StationInfo>> o1,
							   Map.Entry<List<Integer>,List<StationInfo>> o2) {
				if(o1.getValue().size() < o2.getValue().size()){
					return -1;
				}else if(o1.getValue().size() == o2.getValue().size()){
					return 0;
				}
				return 1;
			}
		});
		if(infoIds.size() <= MAXRECOMENDLINENUMBER){
			return infoIds;
		}
		List<Map.Entry<List<Integer>,List<StationInfo>>> lastLines = new ArrayList<Map.Entry<List<Integer>,List<StationInfo>>>();
		//Log.d(TAG,"---------------------no filte----------------------------------");
		//printAllRecomindLine(infoIds);//打印所有路线
		//Log.d(TAG,"---------------------filte 20 ----------------------------------");
		if(infoIds.size() > MAXLINENUMBER){//保留前20条路线
			int n = 0;
			for (Map.Entry<List<Integer>,List<StationInfo>> entry:infoIds) {
				if(n < MAXLINENUMBER)
					lastLines.add(entry);
				n++;
			}
			infoIds.clear();
		}else{
			lastLines.addAll(infoIds);
		}
		//printAllRecomindLine(lastLines);//打印所有路线
		//Log.d(TAG,"---------------------filte 20 ----------------------------------");
		// 对HashMap中的key 进行排序
		Collections.sort(lastLines, new Comparator<Map.Entry<List<Integer>,List<StationInfo>>>() {
			/*
			 * 返回负数表示：p1 小于p2，
			 * 返回0 表示：p1和p2相等，
			 * 返回正数表示：p1大于p2
			 */
			public int compare(Map.Entry<List<Integer>,List<StationInfo>> o1,
							   Map.Entry<List<Integer>,List<StationInfo>> o2) {
				if(o1.getKey().size() < o2.getKey().size()){
					return -1;
				}else if(o1.getKey().size() == o2.getKey().size() ){
					return 0;
				}
				return 1;
			}
		});
		List<Map.Entry<List<Integer>,List<StationInfo>>> lastLinesLast = new ArrayList<Map.Entry<List<Integer>,List<StationInfo>>>();
		if(lastLines.size() > MAXRECOMENDLINENUMBER){//最终保留推荐线路不超过5条
			int size = lastLines.size();
			for (int i = 0;i < size;i++) {
				Map.Entry<List<Integer>,List<StationInfo>> entry = lastLines.get(i);
				if(i < MAXRECOMENDLINENUMBER){
					lastLinesLast.add(entry);
				}
				Log.d(TAG,"getReminderLines getKey = "+entry.getKey()+" size = "+entry.getValue().size());
			}
		}else{
			lastLinesLast.addAll(lastLines);
		}
		lastLines.clear();
		return lastLinesLast;
	}

	public static Map<List<Integer>,List<StationInfo>> getAllLineStation(Map<Integer,LineInfo> mLineInfoList,List<List<Integer>> transferLine
			,StationInfo start,final StationInfo end){
		Map<List<Integer>,List<StationInfo>> currentAllStationList = new HashMap<>();//正在导航线路
		//取出一条路线
		for(List<Integer> list:transferLine){
			//Log.d(TAG,(n)+" list = "+list);
			StationInfo startStation = null,  endStation = null;
			final int size = list.size();
			//Log.d(TAG,"---------------------start----------------------------list = "+list);
			List<StationInfo> oneLineMap = new ArrayList<StationInfo>();
			//分段查找所有站
			for(int i = 0 ; i < size ; i++){
				final int lined = list.get(i);
				//Log.d(TAG,lined+"号线");
				LineInfo lineInfo = getLineInfoByLineid(mLineInfoList,lined);
				if(size == 1){//不用换乘
					startStation = start;
					endStation = end;
					startStation = getStationInfoByLineidAndName(lineInfo.getStationInfoList(), startStation.getCname());//再找相同站台
				}else if(i == 0) {//需要换乘，从开始起点走
					startStation = start;
					if(startStation != null)//起点站
						startStation = getStationInfoByLineidAndName(lineInfo.getStationInfoList(), startStation.getCname());//再找相同站台
					if(size > i+1){
						List<StationInfo> stationInfoList = getTwoLineCommonStation(mLineInfoList,lined,list.get(i+1));//找到当前线路和下一条线路交汇站台
						endStation = gitNearestStation(stationInfoList,startStation);//找到与该站台最近的一个站作为结束站
					}else{
						endStation = end;
					}
				}else{
					startStation = endStation;
					if(startStation != null)
						startStation = getStationInfoByLineidAndName(lineInfo.getStationInfoList(), startStation.getCname());//再找相同站台
					if(size > i+1){
						List<StationInfo> stationInfoList = getTwoLineCommonStation(mLineInfoList,lined,list.get(i+1));//找到当前线路和下一条线路交汇站台
						endStation = gitNearestStation(stationInfoList,startStation);//找到与该站台最近的一个站作为结束站
					}else{
						endStation = end;
					}
				}
				PathSerachUtil.findLinedStation(lineInfo,startStation,endStation,oneLineMap);
			}
			currentAllStationList.put(list,oneLineMap);
		}
		return currentAllStationList;
	}

	public static StationInfo gitNearestStation(List<StationInfo> stationInfoList,StationInfo stationInfo){
		if(stationInfoList == null || stationInfoList.size() <=0 ){
			return null;
		}
		StationInfo minStationInfo = stationInfoList.get(0);
		int min = 0;
		for(StationInfo station:stationInfoList){
			int dis = Math.abs(station.pm-stationInfo.pm);
			if(min > dis){
				minStationInfo = station;
			}
		}
		return minStationInfo;
	}

	public static StationInfo getStationInfoByLineidAndName(List<StationInfo> stationInfoList,String name) {
		for (StationInfo mStationInfo:stationInfoList){//查询最站台和当前路线相同站
			if(mStationInfo.getCname().equals(name)){
				return mStationInfo;
			}
		}
		return null;
	}

	public static List<StationInfo> getTwoLineCommonStation(Map<Integer,LineInfo> mLineInfoList,int line1,int line2){
		List<StationInfo> stationlist = new ArrayList<StationInfo>();
		LineInfo lineInfo1 = PathSerachUtil.getLineInfoByLineid(mLineInfoList,line1);
		for(StationInfo stationInfo:lineInfo1.getStationInfoList()){
			if(stationInfo.canTransfer()){
				int lined = PathSerachUtil.isSameLine(stationInfo,line2);
				if(lined > 0){
					stationlist.add(stationInfo);
				}
			}
		}
		return stationlist;
	}

	public static LineInfo getLineInfoByLineid(Map<Integer,LineInfo> lineInfoList,int lineid){
		return lineInfoList.get(lineid);
	}

	public static  void printAllRecomindLine(List<Map.Entry<List<Integer>,List<StationInfo>>> lastLines){
		StringBuffer str = new StringBuffer();
		Log.d(TAG,"------------------------devide-----------------------lastLines.size = "+lastLines.size());
		for(Map.Entry<List<Integer>,List<StationInfo>> entry:lastLines){
			str.delete(0,str.length());
			for(StationInfo stationInfo:entry.getValue()){
				str.append(stationInfo.getCname()+"->");
			}
			Log.d(TAG,"key = "+entry.getKey()+" change = "+entry.getKey().size()+" stationnumber = "+entry.getValue().size()+" station = "+str.toString());
		}
		Log.d(TAG,"------------------------end-----------------------");
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

	//查询站台是否与目标线路有相同线路
	public static int isTwoStationSameLine(StationInfo start,StationInfo end){
		final String lines[] = start.getTransfer().split(CommonFuction.TRANSFER_SPLIT);
		final String lines1[] = end.getTransfer().split(CommonFuction.TRANSFER_SPLIT);
		int size = lines.length;
		int size1 = lines1.length;
		for(int i = 0;i < size;i++){//找出和终点站相同点不用换乘
			for(int j = 0;j< size1;j++){
				if(CommonFuction.convertToInt(lines[i],0) == CommonFuction.convertToInt(lines1[j],-1)){//找到相同线路站不用换乘
					return CommonFuction.convertToInt(lines[i],0);
				}
			}
		}
		return -1;
	}
}
