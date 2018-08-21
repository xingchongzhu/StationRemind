package com.traffic.locationremind.pinying;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.*;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.activity.AppCommonActivity;
import com.traffic.locationremind.baidu.location.activity.LocationApplication;
import com.traffic.locationremind.baidu.location.activity.MainActivity;
import com.traffic.locationremind.baidu.location.service.LocationService;
import com.traffic.locationremind.manager.bean.CityInfo;
import com.traffic.locationremind.manager.database.DataHelper;

public class LocationCityActivity extends AppCommonActivity implements OnScrollListener {
	private BaseAdapter adapter;
	private ResultListAdapter resultListAdapter;
	private ListView personList;
	private ListView resultList;
	private TextView overlay; // 对话框首字母textview
	private MyLetterListView letterListView; // A-Z listview
	private HashMap<String, Integer> alphaIndexer;// 存放存在的汉语拼音首字母和与之对应的列表位置
	private String[] sections;// 存放存在的汉语拼音首字母
	private Handler handler;
	private OverlayThread overlayThread; // 显示首字母对话框
	private ArrayList<CityInfo> allCity_lists; // 所有城市列表
	private List<CityInfo> city_lists;// 城市列表
	private ArrayList<CityInfo> city_hot;
	private ArrayList<CityInfo> city_result;
	private ArrayList<String> city_history;
	private EditText sh;
	private TextView tv_noresult;

	private String currentCity; // 用于保存定位到的城市
	private int locateProcess = 1; // 记录当前定位的状态 正在定位-定位成功-定位失败
	private boolean isNeedFresh;

	private DataHelper helper;

	private LocationService locationService;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		personList = (ListView) findViewById(R.id.list_view);
		allCity_lists = new ArrayList<CityInfo>();
		city_hot = new ArrayList<CityInfo>();
		city_result = new ArrayList<CityInfo>();
		city_history = new ArrayList<String>();
		resultList = (ListView) findViewById(R.id.search_result);
		sh = (EditText) findViewById(R.id.sh);
		tv_noresult = (TextView) findViewById(R.id.tv_noresult);
		helper = DataHelper.getInstance(this);
		sh.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
									  int count) {
				if (s.toString() == null || "".equals(s.toString())) {
					letterListView.setVisibility(View.VISIBLE);
					personList.setVisibility(View.VISIBLE);
					resultList.setVisibility(View.GONE);
					tv_noresult.setVisibility(View.GONE);
				} else {
					city_result.clear();
					letterListView.setVisibility(View.GONE);
					personList.setVisibility(View.GONE);
					getResultCityList(s.toString());
					if (city_result.size() <= 0) {
						tv_noresult.setVisibility(View.VISIBLE);
						resultList.setVisibility(View.GONE);
					} else {
						tv_noresult.setVisibility(View.GONE);
						resultList.setVisibility(View.VISIBLE);
						resultListAdapter.notifyDataSetChanged();
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		letterListView = (MyLetterListView) findViewById(R.id.MyLetterListView01);
		//letterListView.setOnTouchingLetterChangedListener(new LetterListViewListener());
		alphaIndexer = new HashMap<String, Integer>();
		handler = new Handler();
		overlayThread = new OverlayThread();
		isNeedFresh = true;
		personList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				if (position >= 4) {
					setSelectResult(allCity_lists.get(position).getCityName());
					//Toast.makeText(getApplicationContext(),
						//	allCity_lists.get(position).getCityName(),
						//	Toast.LENGTH_SHORT).show();
				}
			}
		});
		locateProcess = 1;
		personList.setAdapter(adapter);
		personList.setOnScrollListener(this);
		resultListAdapter = new ResultListAdapter(this, city_result);
		resultList.setAdapter(resultListAdapter);
		resultList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				setSelectResult(city_result.get(position).getCityName());
				/*Toast.makeText(getApplicationContext(),
						city_result.get(position).getCityName(), Toast.LENGTH_SHORT)
						.show();*/
			}
		});
		initOverlay();
		cityInit();
		hotCityInit();
		hisCityInit();
		setAdapter(allCity_lists, city_hot, city_history);

		// -----------location config ------------
		locationService = ((LocationApplication) getApplication()).locationService;
		// 获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
		locationService.registerListener(mListener);
	}

	public void InsertCity(String name) {
		SQLiteDatabase db = helper.getCitySQLiteDatabase();
		Cursor cursor = db.rawQuery("select * from recentcity where name = '"
				+ name + "'", null);
		if (cursor.getCount() > 0) { //
			db.delete("recentcity", "name = ?", new String[] { name });
		}
		db.execSQL("insert into recentcity(name, date) values('" + name + "', "
				+ System.currentTimeMillis() + ")");
	}


	private void cityInit() {
		CityInfo city = new CityInfo("定位", "0"); // 当前定位城市
		allCity_lists.add(city);
		city = new CityInfo("最近", "1"); // 最近访问的城市
		allCity_lists.add(city);
		city = new CityInfo("热门", "2"); // 热门城市
		allCity_lists.add(city);
		city = new CityInfo("全部", "3"); // 全部城市
		allCity_lists.add(city);
		city_lists = getCityList();
		allCity_lists.addAll(city_lists);
	}

	/**
	 * 热门城市
	 */
	public void hotCityInit() {
		CityInfo city = new CityInfo("上海", "2");
		city_hot.add(city);
		city = new CityInfo("北京", "2");
		city_hot.add(city);
		city = new CityInfo("广州", "2");
		city_hot.add(city);
		city = new CityInfo("深圳", "2");
		city_hot.add(city);
		city = new CityInfo("武汉", "2");
		city_hot.add(city);
		city = new CityInfo("天津", "2");
		city_hot.add(city);
		city = new CityInfo("西安", "2");
		city_hot.add(city);
		city = new CityInfo("南京", "2");
		city_hot.add(city);
		city = new CityInfo("杭州", "2");
		city_hot.add(city);
		city = new CityInfo("成都", "2");
		city_hot.add(city);
		city = new CityInfo("重庆", "2");
		city_hot.add(city);
	}

	private void hisCityInit() {
		SQLiteDatabase db = helper.getCitySQLiteDatabase();
		Cursor cursor = db.rawQuery(
				"select * from recentcity order by date desc limit 0, 3", null);
		while (cursor.moveToNext()) {
			city_history.add(cursor.getString(1));
		}
	}

	@SuppressWarnings("unchecked")
	private List<CityInfo> getCityList() {

		List<CityInfo> list = helper.getAllCityInfoList();
		Collections.sort(list, comparator);
		return list;
	}

	@SuppressWarnings("unchecked")
	private void getResultCityList(String keyword) {
		SQLiteDatabase db = helper.getCitySQLiteDatabase();
		Cursor cursor = db.rawQuery(.q
					"select * from city where name like \"%" + keyword
							+ "%\" or pinyin like \"%" + keyword + "%\"", null);
		CityInfo city;
		Log.e("info", "length = " + cursor.getCount());
		while (cursor.moveToNext()) {
			city = new CityInfo(cursor.getString(1), cursor.getString(2));
			city_result.add(city);
		}
		cursor.close();
		db.close();
		Collections.sort(city_result, comparator);
	}

	/**
	 * a-z排序
	 */
	@SuppressWarnings("rawtypes")
	Comparator comparator = new Comparator<CityInfo>() {
		@Override
		public int compare(CityInfo lhs, CityInfo rhs) {
			String a = lhs.getPingying().substring(0, 1);
			String b = rhs.getPingying().substring(0, 1);
			int flag = a.compareTo(b);
			if (flag == 0) {
				return a.compareTo(b);
			} else {
				return flag;
			}
		}
	};

	private void setAdapter(List<CityInfo> list, List<CityInfo> hotList,
							List<String> hisCity) {
		adapter = new ListAdapter(this, list, hotList, hisCity);
		personList.setAdapter(adapter);
	}

	/**
	 * 实现实位回调监听
	 */
	private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {

		@Override
		public void onReceiveLocation(BDLocation arg0) {
			Log.e("info", "city = " + arg0.getCity());
			if (!isNeedFresh) {
				return;
			}
			isNeedFresh = false;
			if (arg0.getCity() == null) {
				locateProcess = 3; // 定位失败
				personList.setAdapter(adapter);
				adapter.notifyDataSetChanged();
				return;
			}
			currentCity = arg0.getCity().substring(0,
					arg0.getCity().length() - 1);
			locateProcess = 2; // 定位成功
			personList.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		}

	};

	private class ResultListAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private ArrayList<CityInfo> results = new ArrayList<CityInfo>();

		public ResultListAdapter(Context context, ArrayList<CityInfo> results) {
			inflater = LayoutInflater.from(context);
			this.results = results;
		}

		@Override
		public int getCount() {
			return results.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item, null);
				viewHolder = new ViewHolder();
				viewHolder.name = (TextView) convertView
						.findViewById(R.id.name);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.name.setText(results.get(position).getCityName());
			return convertView;
		}

		class ViewHolder {
			TextView name;
		}
	}

	public class ListAdapter extends BaseAdapter {
		private Context context;
		private LayoutInflater inflater;
		private List<CityInfo> list;
		private List<CityInfo> hotList;
		private List<String> hisCity;
		final int VIEW_TYPE = 5;

		public ListAdapter(Context context, List<CityInfo> list,
						   List<CityInfo> hotList, List<String> hisCity) {
			this.inflater = LayoutInflater.from(context);
			this.list = list;
			this.context = context;
			this.hotList = hotList;
			this.hisCity = hisCity;
			alphaIndexer = new HashMap<String, Integer>();
			sections = new String[list.size()];
			for (int i = 0; i < list.size(); i++) {
				// 当前汉语拼音首字母
				String currentStr = getAlpha(list.get(i).getPingying());
				// 上一个汉语拼音首字母，如果不存在为" "
				String previewStr = (i - 1) >= 0 ? getAlpha(list.get(i - 1)
						.getPingying()) : " ";
				if (!previewStr.equals(currentStr)) {
					String name = getAlpha(list.get(i).getPingying());
					alphaIndexer.put(name, i);
					sections[i] = name;
				}
			}
		}

		@Override
		public int getViewTypeCount() {
			return VIEW_TYPE;
		}

		@Override
		public int getItemViewType(int position) {
			return position < 4 ? position : 4;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		ViewHolder holder;

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final TextView city;
			int viewType = getItemViewType(position);
			if (viewType == 0) { // 定位
				convertView = inflater.inflate(R.layout.frist_list_item, null);
				TextView locateHint = (TextView) convertView
						.findViewById(R.id.locateHint);
				city = (TextView) convertView.findViewById(R.id.lng_city);
				city.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (locateProcess == 2) {
							setSelectResult(city.getText().toString());
							/*Toast.makeText(getApplicationContext(),
									city.getText().toString(),
									Toast.LENGTH_SHORT).show();*/
						} else if (locateProcess == 3) {
							locateProcess = 1;
							personList.setAdapter(adapter);
							adapter.notifyDataSetChanged();
							//mLocationClient.stop();
							isNeedFresh = true;
							//InitLocation();
							currentCity = "";
							//mLocationClient.start();
						}
					}
				});
				ProgressBar pbLocate = (ProgressBar) convertView
						.findViewById(R.id.pbLocate);
				if (locateProcess == 1) { // 正在定位
					locateHint.setText("正在定位");
					city.setVisibility(View.GONE);
					pbLocate.setVisibility(View.VISIBLE);
				} else if (locateProcess == 2) { // 定位成功
					locateHint.setText("当前定位城市");
					city.setVisibility(View.VISIBLE);
					city.setText(currentCity);
					//mLocationClient.stop();
					pbLocate.setVisibility(View.GONE);
				} else if (locateProcess == 3) {
					locateHint.setText("未定位到城市,请选择");
					city.setVisibility(View.VISIBLE);
					city.setText("重新选择");
					pbLocate.setVisibility(View.GONE);
				}
			} else if (viewType == 1) { // 最近访问城市
				convertView = inflater.inflate(R.layout.recent_city, null);
				GridView rencentCity = (GridView) convertView
						.findViewById(R.id.recent_city);
				rencentCity
						.setAdapter(new HitCityAdapter(context, this.hisCity));
				rencentCity.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
											int position, long id) {
						setSelectResult(city_history.get(position));
						/*Toast.makeText(getApplicationContext(),
								city_history.get(position), Toast.LENGTH_SHORT)
								.show();*/

					}
				});
				TextView recentHint = (TextView) convertView
						.findViewById(R.id.recentHint);
				recentHint.setText("最近访问的城市");
			} else if (viewType == 2) {
				convertView = inflater.inflate(R.layout.recent_city, null);
				GridView hotCity = (GridView) convertView
						.findViewById(R.id.recent_city);
				hotCity.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
											int position, long id) {
						setSelectResult(city_hot.get(position).getCityName());
						/*Toast.makeText(getApplicationContext(),
								city_hot.get(position).getCityName(),
								Toast.LENGTH_SHORT).show();*/

					}
				});
				hotCity.setAdapter(new HotCityAdapter(context, this.hotList));
				TextView hotHint = (TextView) convertView
						.findViewById(R.id.recentHint);
				hotHint.setText("热门城市");
			} else if (viewType == 3) {
				convertView = inflater.inflate(R.layout.total_item, null);
			} else {
				if (convertView == null) {
					convertView = inflater.inflate(R.layout.list_item, null);
					holder = new ViewHolder();
					holder.alpha = (TextView) convertView
							.findViewById(R.id.alpha);
					holder.name = (TextView) convertView
							.findViewById(R.id.name);
					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}
				if (position >= 1) {
					holder.name.setText(list.get(position).getCityName());
					String currentStr = getAlpha(list.get(position).getPingying());
					String previewStr = (position - 1) >= 0 ? getAlpha(list
							.get(position - 1).getPingying()) : " ";
					if (!previewStr.equals(currentStr)) {
						holder.alpha.setVisibility(View.VISIBLE);
						holder.alpha.setText(currentStr);
					} else {
						holder.alpha.setVisibility(View.GONE);
					}
				}
			}
			return convertView;
		}

		private class ViewHolder {
			TextView alpha; // 首字母标题
			TextView name; // 城市名字
		}
	}

	public void setSelectResult(String city){
		Intent intent = new Intent();
		intent.setAction(city);
		setResult(MainActivity.SELECTCITYRESULTCODE,intent);
		finish();
	}

	@Override
	protected void onStop() {
		//mLocationClient.stop();
		if(locationService != null){
			locationService.unregisterListener(mListener); // 注销掉监听
		}
		super.onStop();
	}

	class HotCityAdapter extends BaseAdapter {
		private Context context;
		private LayoutInflater inflater;
		private List<CityInfo> hotCitys;

		public HotCityAdapter(Context context, List<CityInfo> hotCitys) {
			this.context = context;
			inflater = LayoutInflater.from(this.context);
			this.hotCitys = hotCitys;
		}

		@Override
		public int getCount() {
			return hotCitys.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = inflater.inflate(R.layout.item_city, null);
			TextView city = (TextView) convertView.findViewById(R.id.city);
			city.setText(hotCitys.get(position).getCityName());
			return convertView;
		}
	}

	class HitCityAdapter extends BaseAdapter {
		private Context context;
		private LayoutInflater inflater;
		private List<String> hotCitys;

		public HitCityAdapter(Context context, List<String> hotCitys) {
			this.context = context;
			inflater = LayoutInflater.from(this.context);
			this.hotCitys = hotCitys;
		}

		@Override
		public int getCount() {
			return hotCitys.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = inflater.inflate(R.layout.item_city, null);
			TextView city = (TextView) convertView.findViewById(R.id.city);
			city.setText(hotCitys.get(position));
			return convertView;
		}
	}

	private boolean mReady;

	// 初始化汉语拼音首字母弹出提示框
	private void initOverlay() {
		mReady = true;
		LayoutInflater inflater = LayoutInflater.from(this);
		overlay = (TextView) inflater.inflate(R.layout.overlay, null);
		overlay.setVisibility(View.INVISIBLE);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_APPLICATION,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
				PixelFormat.TRANSLUCENT);
		WindowManager windowManager = (WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE);
		windowManager.addView(overlay, lp);
	}

	private boolean isScroll = false;

	private class LetterListViewListener implements MyLetterListView.OnTouchingLetterChangedListener {

		@Override
		public void onTouchingLetterChanged(final String s) {
			isScroll = false;
			if (alphaIndexer.get(s) != null) {
				int position = alphaIndexer.get(s);
				personList.setSelection(position);
				overlay.setText(s);
				overlay.setVisibility(View.VISIBLE);
				handler.removeCallbacks(overlayThread);
				// 延迟一秒后执行，让overlay为不可见
				handler.postDelayed(overlayThread, 1000);
			}
		}
	}

	// 设置overlay不可见
	private class OverlayThread implements Runnable {
		@Override
		public void run() {
			overlay.setVisibility(View.GONE);
		}
	}

	// 获得汉语拼音首字母
	private String getAlpha(String str) {
		if (str == null) {
			return "#";
		}
		if (str.trim().length() == 0) {
			return "#";
		}
		char c = str.trim().substring(0, 1).charAt(0);
		// 正则表达式，判断首字母是否是英文字母
		Pattern pattern = Pattern.compile("^[A-Za-z]+$");
		if (pattern.matcher(c + "").matches()) {
			return (c + "").toUpperCase();
		} else if (str.equals("0")) {
			return "定位";
		} else if (str.equals("1")) {
			return "最近";
		} else if (str.equals("2")) {
			return "热门";
		} else if (str.equals("3")) {
			return "全部";
		} else {
			return "#";
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == SCROLL_STATE_TOUCH_SCROLL
				|| scrollState == SCROLL_STATE_FLING) {
			isScroll = true;
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
						 int visibleItemCount, int totalItemCount) {
		if (!isScroll) {
			return;
		}

		if (mReady) {
			String text;
			String name = allCity_lists.get(firstVisibleItem).getCityName();
			String pinyin = allCity_lists.get(firstVisibleItem).getPingying();
			if (firstVisibleItem < 4) {
				text = name;
			} else {
				text = PingYinUtil.converterToFirstSpell(pinyin)
						.substring(0, 1).toUpperCase();
			}
		}
	}
}