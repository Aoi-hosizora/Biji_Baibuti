package com.baibuti.biji.ui.activity;

import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.baibuti.biji.R;

public class WebViewActivity extends AppCompatActivity {

    private String htmlStr;

    public final class InJavaScriptLocalObj {

        @JavascriptInterface
        public void showSource(String html) {
            htmlStr = html;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        initView();
    }

    private void initView() {

        //设置webView属性
        WebView webView = findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // XSS
        webView.addJavascriptInterface(new InJavaScriptLocalObj(), "java_obj");
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(final WebView view, String url) {
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        view.loadUrl("javascript:window.java_obj.showSource(document.getElementsByTagName('html')[0].innerHTML);");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });

        webView.loadUrl("https://sso.scut.edu.cn/cas/login?service=http%3A%2F%2Fxsjw2018.scuteo.com%2Fsso%2Fdriotlogin");

        Button importBtn = findViewById(R.id.webview_btn);
        importBtn.setOnClickListener((v) -> {

            webView.loadUrl("javascript:window.java_obj.showSource(document.getElementsByTagName('html')[0].innerHTML);");
            SystemClock.sleep(1000);

            Intent intent = new Intent();
            intent.putExtra("html", htmlStr);
            setResult(RESULT_OK, intent);
            finish();
        });
    }
}
