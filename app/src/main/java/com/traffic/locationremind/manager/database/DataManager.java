package com.traffic.locationremind.manager.database;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.*;
import com.traffic.locationremind.baidu.location.item.LineSearchItem;
import com.traffic.locationremind.baidu.location.listener.LoadDataListener;
import com.traffic.locationremind.baidu.location.listener.SearchResultListener;
import com.traffic.locationremind.baidu.location.object.LineObject;
import com.traffic.locationremind.baidu.location.utils.SearchPath;
import com.traffic.locationremind.common.util.*;
import com.traffic.locationremind.manager.AsyncTaskManager;
import com.traffic.locationremind.manager.bean.CityInfo;
import com.traffic.locationremind.manager.bean.ExitInfo;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;

import java.lang.ref.WeakReference;
import java.util.*;

public class DataManager{

	private static final String TAG = "DataManager";

	private List<LoadDataListener> mLoadDataListener = new ArrayList<>();
	private DataHelper mDataHelper;//数据库
	private Map<String,CityInfo> cityInfoList;//所有城市信息
	private Map<Integer,LineInfo> mLineInfoList;//地图线路
	private CityInfo currentCityNo = null;
	private Map<Integer, Map<Integer,Integer>> allLineCane = new HashMap<Integer, Map<Integer,Integer>>();//用于初始化路线矩阵
	private Map<Integer,Integer> lineColor = new HashMap<>();//路线对应颜色
	private Object mLock = new Object();
	private Map<String, StationInfo> allstations = new HashMap<>();
    private int[][] matirx ,nodeRalation;
    private Integer allLineNodes[];

	private int maxLineid = 0;
	private int minLineid = Integer.MAX_VALUE;
	GeoCoder mSearch ;
	OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
		public void onGetGeoCodeResult(GeoCodeResult result) {
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			}else{
				Log.d(TAG,"longitude  "+result.getLocation().longitude+","+result.getLocation().latitude+" getAddress = "+result.getAddress());
			}
			//获取地理编码结果
		}

		@Override
		public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			}else{
				Log.d(TAG,"22222  "+result.toString());
			}
		}
	};

	private static DataManager mDataManager;

	public int[][] getMatirx(){
	    return matirx;
    }

    public int[][] getNodeRalation(){
		return nodeRalation;
	}

    public Integer[] getAllLineNodes(){
		return allLineNodes;
	}

	public void releaseResource(){
		mDataHelper.Close();
		mDataHelper = null;
		if(allLineCane != null)
			allLineCane.clear();
		if(lineColor != null)
			lineColor.clear();
		if(allstations != null)
			allstations.clear();
		if(cityInfoList != null)
			cityInfoList.clear();
		if(mLineInfoList != null)
			mLineInfoList.clear();
		if(mLoadDataListener != null)
			mLoadDataListener.clear();
		mDataManager = null;
		if(mSearch != null) {
			mSearch.destroy();
		}
	}

	public CityInfo getCurrentCityNo(){
		return currentCityNo;
	}

	public DataManager(Context context){

	}

	public void loadData(Context context){
		if(cityInfoList != null){
			cityInfoList.clear();
		}
		if(mLineInfoList != null){
			mLineInfoList.clear();
		}
		if(allLineCane != null){
			allLineCane.clear();
		}
		if(mLineInfoList != null)
			mLineInfoList.clear();
		if(lineColor != null)
			lineColor.clear();
		new MyAsyncTask().execute(context);
		if(mSearch == null) {
			//mSearch = GeoCoder.newInstance();
			//mSearch.setOnGetGeoCodeResultListener(listener);
		}
	}

	public void addLoadDataListener(LoadDataListener loadDataListener) {
		mLoadDataListener.add(loadDataListener) ;
	}

	public void removeLoadDataListener(LoadDataListener loadDataListener) {
		mLoadDataListener.remove(loadDataListener);
	}

	public void notificationUpdata(){
		Log.d(TAG,"notificationUpdata");
		for(LoadDataListener loadDataListener:mLoadDataListener){
			loadDataListener.loadFinish();
		}
	}

	public static DataManager getInstance(Context context){
		if(mDataManager == null){
			mDataManager = new DataManager(context);
		}
		if(mDataManager.mDataHelper == null)
			mDataManager.mDataHelper = new DataHelper(context);
		return mDataManager;
	}

	public Map<Integer, Map<Integer,Integer>> getAllLineCane(){
		return allLineCane;
	}

	public Map<String,CityInfo> getCityInfoList(){
		return cityInfoList;
	}

	public Map<Integer,LineInfo> getLineInfoList(){
		return mLineInfoList;
	}

	public int getMaxLineid(){
		return maxLineid;
	}

	public int getMinLineid(){
		return minLineid;
	}
	public DataHelper getDataHelper(){
		return mDataHelper;
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
			if (mDataHelper != null){
				cityInfoList = mDataHelper.getAllCityInfo();
			}else{
				return null;
			}
			//merageAllCity((Context) params[0]);
			String shpno = CommonFuction.getSharedPreferencesValue((Context) params[0], CityInfo.CITYNAME);
			maxLineid = 0;
			if (!TextUtils.isEmpty(shpno)) {
				currentCityNo = cityInfoList.get(shpno);
			}else{
				currentCityNo = cityInfoList.get(IDef.DEFAULTCITY);
			}

			if (currentCityNo == null) {
				currentCityNo = CommonFuction.getFirstOrNull(cityInfoList);
			}
            if (currentCityNo == null || !FileUtil.dbIsExist((Context) params[0],currentCityNo)) {
                return null;
            }

            mDataHelper.setCityHelper((Context) params[0],currentCityNo.getPingying());
			Map<Integer,LineInfo> list= mDataHelper.getLineList(LineInfo.LINEID, "ASC");

			for (Map.Entry<Integer,LineInfo> entry : list.entrySet()) {
				entry.getValue().setStationInfoList(mDataHelper.QueryByStationLineNo(entry.getKey()));
				List<StationInfo> canTransferlist = mDataHelper.QueryByStationLineNoCanTransfer(entry.getValue().lineid, currentCityNo.getCityNo());
				allLineCane.put(entry.getKey(), PathSerachUtil.getLineAllLined(canTransferlist));
				if(entry.getKey() > maxLineid){
					maxLineid = entry.getKey();
				}
				if(entry.getKey() < minLineid){
					minLineid = entry.getKey();
				}
			}
			maxLineid+= 1;//找出路线最大编号加一
			mLineInfoList = list;
			setAllstations(mLineInfoList);
			allLineNodes = null;
			allLineNodes = new Integer[maxLineid];
			for (int i = 0; i < maxLineid; i++) {//初始化所有线路
				allLineNodes[i] = i;
			}
            initGrf(allLineCane,maxLineid);
			//getEmityString();
            //getAddr((Context) params[0],shpno);
			//GrfAllEdge.createGraph(allLineNodes, allLineCane);
			return list;
		}

		@Override
		protected void onPostExecute(Map<Integer,LineInfo> list) {
			super.onPostExecute(list);
			notificationUpdata();
			ArrayList<LineInfo> alllist= mDataHelper.getLineLists(LineInfo.LINEID, "ASC");
			int size = alllist.size();
			map.clear();
			for(SearchPath searchPath:allTaskList){
				searchPath.cancel(true);
			}

			allTaskList.clear();
			for(int i = 13;i < size;i++){
				for(int j = 5;j < 12;j++){
					if(i != j && j < size && i < size){
						allTaskList.add(searchThread(alllist.get(i).lineid,alllist.get(j).lineid, nodeRalation));
					}
				}
			}
			if(allTaskList.size() > 0){
				allTaskList.get(0).execute("");;
			}
		}
	}
	List<SearchPath> allTaskList = new ArrayList<>();
	Map<String,String> map = new HashMap<>();
	public SearchPath searchThread(final int startlineid,final int endlineid, int[][] nodeRalation) {
		SearchPath searchPath = new SearchPath(new SearchResultListener(){
			@Override
			public void updateSingleResult(List<Integer> list){

			}
			@Override
			public void updateResultList(List<List<Integer>> list){
				if(allTaskList.size() > 0) {
					allTaskList.remove(0);
				}
				if(allTaskList.size() > 0){
					allTaskList.get(0).execute("");;
				}
				if(list != null && list.size() > 0) {
					Collections.sort(list, new Comparator<List<Integer>>() {
						public int compare(List<Integer> p1, List<Integer> p2) {
							//按照换乘次数
							if (p1.size() > p2.size()) {
								return 1;
							}
							if (p1.size() == p2.size()) {
								return 0;
							}
							return -1;
						}
					});
					int n = 0;
					int preNum = list.get(0).size();
					List<List<Integer>> needRemove = new ArrayList<>();
					for(List<Integer> single:list){
						if(preNum != single.size()){
							n++;
						}
						if(n > 1){
							needRemove.add(single);
						}
						preNum = single.size();
					}
					list.removeAll(needRemove);
					//Log.d(TAG,  "needRemove = "+needRemove.toString());
					for(List<Integer> single:list){
						LineSearchItem item = new LineSearchItem(startlineid,endlineid,single);
						if(!mDataHelper.lineItemExist(item)) {
							mDataHelper.insetLineSearchItem(item);
							Log.d(TAG,"updateResultList startlineid = "+startlineid+" endlineid = "+endlineid+"  "+item.getLineString());
						}
					}
				}
			}
			@Override
			public void updateResult(List<LineObject> lastLinesLast){

			}
			@Override
			public void cancleDialog(List<LineObject> lastLinesLast){

			}
			@Override
			public void setLineNumber(int number){

			}
		}
		,startlineid, endlineid, nodeRalation);
		AsyncTaskManager.getInstance().addGeekRunnable(searchPath);
		//searchPath.execute("");
		return searchPath;
	}

	public void getAddr(Context context,String city){
		List<StationInfo> lists = mDataHelper.QueryAllByStationLineNo();
		for(StationInfo stationInfo:lists) {
			mSearch.geocode(new GeoCodeOption()
					.city(city)
					.address(city+stationInfo.getCname()+"地铁站"));
			//FileUtil.getGeoPointBystr(context,city+stationInfo.getCname()+"地铁站");
		}
	}
	public void getEmityString(){
		Map<String,String> stringBuffer = new HashMap<>();
		List<StationInfo> lists = mDataHelper.QueryAllByStationLineNo();
		for(StationInfo stationInfo:lists) {
			List<ExitInfo> existInfoList = mDataManager.getDataHelper().QueryByExitInfoCname(stationInfo.getCname());
			if(existInfoList.size() <= 0){
				stringBuffer.put(stationInfo.getCname(),stationInfo.getCname()+"  "+stationInfo.getLineid());
			}
		}

		StringBuffer str = new StringBuffer();
		for(Map.Entry<String,String> entry:stringBuffer.entrySet()){
			str.append(entry.getValue()+" -> ");
		}
		Log.d("zxc002",str.toString());
		str.delete(0,str.length());
		stringBuffer.clear();
		lists.clear();
	}

	public void merageAllCity(Context context){
		List<CityInfo> cityInfoList = mDataHelper.getAllCityInfoList();
		for(CityInfo cityInfo:cityInfoList){
			mDataHelper.setCityHelper(context,cityInfo.getPingying());
			List<StationInfo> lists = mDataHelper.QueryAllByStationLineNo();
            //Log.d("zxc","city "+cityInfo.getCityName()+" lists.size = "+lists.size());
			mergeExistInfor(lists);
			lists.clear();
			lists = null;
		}
	}

	private void mergeExistInfor(List<StationInfo> lists){
		StringBuffer stringBuffer = new StringBuffer();
		String string = "";
		ExitInfo preExitInfo = null;
		int n = 0;
		int size = 0;
		for(StationInfo stationInfo:lists){
			List<ExitInfo> existInfoList = mDataManager.getDataHelper().QueryByExitInfoCname(stationInfo.getCname());
			stringBuffer.delete(0,stringBuffer.length());
			string = "";
			Collections.sort(existInfoList, new MapComparator());
			n = 0;
			size = existInfoList.size();
			for(ExitInfo exitInfo:existInfoList){
				n++;
				String str = exitInfo.getCname()+exitInfo.getExitname();
				if(size == n){
					if(stringBuffer.length() > 1 && preExitInfo != null){
						stringBuffer.replace(stringBuffer.length()-1, stringBuffer.length(), "");
						preExitInfo.setAddr(stringBuffer.toString());
						mDataManager.getDataHelper().updateExitInfor(preExitInfo);
					}
				}else if(string.equals(str)){
					stringBuffer.append(exitInfo.getAddr()+",");
					preExitInfo = exitInfo;
				}else{
					if(stringBuffer.length() > 1 && preExitInfo != null){
						stringBuffer.replace(stringBuffer.length()-1, stringBuffer.length(), "");
						preExitInfo.setAddr(stringBuffer.toString());
						mDataManager.getDataHelper().updateExitInfor(preExitInfo);
					}
					stringBuffer.delete(0,stringBuffer.length());
					stringBuffer.append(exitInfo.getAddr()+",");
					string = str;
					preExitInfo = exitInfo;
				}

			}
		}
	}

    // 初始化图数据
    private void initGrf(Map<Integer, Map<Integer, Integer>> allLineCane,int total) {
        matirx = null;
        this.matirx = new int[total][total];
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : allLineCane.entrySet()) {
            for (Map.Entry<Integer, Integer> value : entry.getValue().entrySet()) {
                this.matirx[entry.getKey()][value.getValue()] = 1;
            }
        }
		GrfAllEdge.printMatrix(total,matirx);
		nodeRalation = null;
		nodeRalation =new int[total][];
		for(int i =0;i < matirx.length;i++){
			int k = 0;
			List<Integer> list = new ArrayList<>();
			for(int j=0;j<matirx[i].length;j++){
				if(matirx[i][j] ==1){
					list.add(j);
				}
			}
			nodeRalation[i] = new int[list.size()];
			for(int l = 0;l < list.size();l++){
				nodeRalation[i][l] = list.get(l);
			}
		}
		StringBuffer str = new StringBuffer();
		for(int i =0;i<nodeRalation.length;i++) {
			str.delete(0,str.length());
			for (int j = 0; j < nodeRalation[i].length; j++) {
				str.append(nodeRalation[i][j]+" ->");
			}
		}

    }

	WeakReference<Callbacks> mCallbacks;
	public interface Callbacks {

	}

	public Map<String, StationInfo> getAllstations() {
		return allstations;
	}

	private void setAllstations(Map<Integer, LineInfo> allLines) {
		allstations.clear();
		if (allLines != null) {
			for (Map.Entry<Integer, LineInfo> entry : allLines.entrySet()) {
				for (StationInfo stationInfo : entry.getValue().getStationInfoList()) {
					allstations.put(stationInfo.getCname(), stationInfo);
				}
			}
		}
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
