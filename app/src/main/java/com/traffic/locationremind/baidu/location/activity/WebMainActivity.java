package com.traffic.locationremind.baidu.location.activity;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import com.traffic.location.remind.R;

public class WebMainActivity extends Activity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        initView();

    }
    private void initView() {
        // TODO Auto-generated method stub
        // 获取webview控件
        webView = (WebView) findViewById(R.id.web_wv);
        // 获取WebView的设置
        WebSettings webSettings = webView.getSettings();
        // 将JavaScript设置为可用，这一句话是必须的，不然所做一切都是徒劳的
        webSettings.setJavaScriptEnabled(true);
        // 给webview添加JavaScript接口
        webView.addJavascriptInterface(new JsInterface(), "control");
        // 通过webview加载html页面
        webView.loadUrl("file:///android_asset/shanghai.html");
    }

    public class JsInterface {
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(WebMainActivity.this, toast, Toast.LENGTH_SHORT).show();
        }

        public void log(final String msg) {
            webView.post(new Runnable() {

                @Override
                public void run() {
                    webView.loadUrl("javascript log(" + "'" + msg + "'" + ")");

                }
            });
        }
    }


}

