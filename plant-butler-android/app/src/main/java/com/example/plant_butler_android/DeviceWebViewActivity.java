package com.example.plant_butler_android;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class DeviceWebViewActivity extends AppCompatActivity {

    private WebView webView;
    private Button buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_webview);

        webView = findViewById(R.id.webView);
        buttonBack = findViewById(R.id.buttonBack);

        // 配置WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // 启用JavaScript
        webSettings.setDomStorageEnabled(true); // 启用DOM存储
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        // 设置WebViewClient，确保链接在WebView内打开
        webView.setWebViewClient(new WebViewClient());

        // 加载服务器主页面（显示所有设备状态）
        String url = Config.BASE_URL + "/";
        webView.loadUrl(url);

        // 返回按钮
        buttonBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onBackPressed() {
        // 如果WebView可以后退，则后退
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
