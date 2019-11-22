package com.baibuti.biji.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.baibuti.biji.R;
import com.baibuti.biji.ui.IContextHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WebViewActivity extends AppCompatActivity implements IContextHelper {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_webview);
        } catch (RuntimeException ex) {
            //  java.lang.RuntimeException: Unable to start activity ComponentInfo{com.baibuti.biji/com.baibuti.biji.ui.activity.WebViewActivity}: android.view.InflateException: Binary XML file line #22: Binary XML file line #22: Error inflating class android.webkit.WebView
            showAlert(this, "错误", "Android WebView 加载错误");
            finish();
        }
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle("导入课程表");
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.webview_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_reload_web:
                webView.reload();
                break;
        }
        return true;
    }

    @BindView(R.id.webview)
    WebView webView;

    private String htmlStr = "";

    public final class InJavaScriptLocalObj {

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void showSource(String html) {
            htmlStr = html;
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void showDescription(String str) { }
    }

    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void initView() {

        webView.requestFocus();

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); // <<<
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccessFromFileURLs(false);

        webView.addJavascriptInterface(new InJavaScriptLocalObj(), "java_obj");
        webView.loadUrl("http://xsjw2018.jw.scut.edu.cn/jwglxt/kbcx/xskbcx_cxXskbcxIndex.html?gnmkdm=N2151&layout=default");

        // [InputMethodManagerWrapper.java:77] updateSelection: SEL [18, 18], COM [-1, -1]
        // [INFO:CONSOLE(138)] "null", source: http://xsjw2018.jw.scut.edu.cn/jwglxt/kbcx/xskbcx_cxXskbcxIndex.html?gnmkdm=N2151&layout=default (138)

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                htmlStr = "";
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:window.java_obj.showSource(document.getElementsByTagName('html')[0].innerHTML);");
                super.onPageFinished(view, url);
            }
        });

        Button importBtn = findViewById(R.id.webview_btn);
        importBtn.setOnClickListener((v) -> {
            if (htmlStr.isEmpty())
                showAlert(this, "错误", "内容未加载完成，请等待完全加载后再导入。");
            else {
                Intent intent = new Intent();
                intent.putExtra("html", htmlStr);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            ViewParent parent = webView.getParent();
            if (parent != null)
                ((ViewGroup) parent).removeView(webView);

            webView.stopLoading();
            webView.getSettings().setJavaScriptEnabled(false);
            webView.clearHistory();
            webView.clearView();
            webView.removeAllViews();
            webView.destroy();
        }
        super.onDestroy();
    }
}
