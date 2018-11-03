package com.traffic.locationremind.baidu.location.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.WindowManager;
import android.webkit.*;
import android.widget.TextView;
import android.widget.Toast;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.activity.MainActivity;
import com.traffic.locationremind.baidu.location.activity.WebMainActivity;
import com.traffic.locationremind.baidu.location.utils.Utils;
import com.traffic.locationremind.baidu.location.view.FullMapView;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.common.util.FileUtil;
import com.traffic.locationremind.manager.bean.CityInfo;
import com.traffic.locationremind.manager.database.DataManager;

import java.util.List;

public class FullMapFragment extends Fragment {
    private static final String TAG = "FullMapFragment";
    private View rootView;
    //private FullMapView mFullMapView;
    private int screenWidth ,screenHeight;
    private DataManager mDataManager;
    private TextView text;
    private WebView webView;
//https://stavinli.github.io/the-subway-of-china/dest/index.html?cityCode=131
    private final String URL = "https://stavinli.github.io/the-subway-of-china/dest/index.html?cityCode=";
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView");
        if (null != rootView) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent) {
                parent.removeView(rootView);
            }
        } else {
            rootView = inflater.inflate(R.layout.full_map_layout,container,false);
            WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            screenWidth = dm.widthPixels;         // 屏幕宽度（像素）
            screenHeight = dm.heightPixels-(int)getResources().getDimension(R.dimen.full_map_top_bottom_height);       // 屏幕高度（像素）
            initView(rootView);// 控件初始化
        }
        return rootView;
    }

    private void initView(View rootView){
        //mFullMapView = (FullMapView)rootView.findViewById(R.id.map);
        mDataManager = ((MainActivity) getActivity()).getDataManager();
        webView = (WebView) rootView.findViewById(R.id.web_wv);
        text = (TextView) rootView.findViewById(R.id.text);
        initData();
    }

    public class JsInterface {
        @JavascriptInterface
        public void log(final String msg) {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    Log.d("zxc", "JsInterface");
                    //webView.loadUrl("javascript:callJS(" + "'" + msg + "'" + ")");
                }
            });
        }
    }

    public void initData(){
        String shpno = CommonFuction.getSharedPreferencesValue(getContext(), CityInfo.CITYNAME);
        List<CityInfo> cityList = mDataManager.getDataHelper().QueryCityByCityNo(shpno);
        if(cityList != null && cityList.size() > 0){
            int id = FileUtil.getResIconId(getContext(),cityList.get(0).getPingying());
            //Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), id);
            //mFullMapView.setBitmap(bitmap2,screenWidth,screenHeight);
            WebSettings webSettings = webView.getSettings();
            // 将JavaScript设置为可用，这一句话是必须的，不然所做一切都是徒劳的
            webSettings.setJavaScriptEnabled(true);
            // 给webview添加JavaScript接口
            webView.addJavascriptInterface(new JsInterface(), "index");
            String url = "file:///android_asset/src/index.html?cityCode="+cityList.get(0).getCityNo();
            webView.loadUrl(url);
            webView.setWebChromeClient(new MyWebChromeClient());
            if(Utils.isGpsOPen(getContext())){
                text.setVisibility(View.GONE);
            }
        }
    }

    public void updateCity(){
        String shpno = CommonFuction.getSharedPreferencesValue(getContext(), CityInfo.CITYNAME);
        List<CityInfo> cityList = mDataManager.getDataHelper().QueryCityByCityNo(shpno);
        if(cityList != null && cityList.size() > 0){
            String url = "file:///android_asset/src/index.html?cityCode="+cityList.get(0).getCityNo();
            //webView.loadUrl("javascript:callJS(" + "'" + cityList.get(0).getCityNo() + "'" + ")");
            //webView.loadUrl("javascript:wave()");
            webView.loadUrl(url);
        }
    }

    final class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.d("zxc", message);
            result.confirm();
            return true;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onResume() {
        super.onResume();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        //挂在后台  资源释放
        webView.getSettings().setJavaScriptEnabled(false);
    }

    @Override
    public void onDestroy() {
        webView.setVisibility(View.GONE);
        webView.destroy();
        super.onDestroy();
    }
}
