package com.huawei.smart.server.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.webkit.WebView;

import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.HWApplication;
import com.huawei.smart.server.HWConstants;
import com.huawei.smart.server.R;
import com.huawei.smart.server.utils.Compatibility;

import butterknife.BindView;

/**
 * @author coa.ke on 5/12/18
 */
public class FeedbackDetailActivity extends BaseActivity {
    @BindView(R.id.web_view)
    WebView mWebView;
    private String mUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_detail);
        Intent intent = getIntent();
        int titleId = intent.getIntExtra(HWConstants.INTENT_KEY_FEEDBACK_DETAIL_TITLE, 0);
        String title = intent.getStringExtra(HWConstants.INTENT_KEY_FEEDBACK_DETAIL_TITLE_STRING);
        mUrl = intent.getStringExtra(HWConstants.INTENT_KEY_FEEDBACK_DETAIL_CONTENT_URL);
        if (!TextUtils.isEmpty(title)) {
            this.initialize(title, true);
        } else {
            this.initialize(titleId, true);
        }
        initView();
        loadWebContent(constructUrl(mUrl));
    }

    private String constructUrl(String originalUrl) {
        SharedPreferences preferences = HWApplication.getContextHW().getSharedPreferences(HWConstants.PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
        String lang = preferences.getString(HWConstants.PREFERENCE_SETTING_LANG, Compatibility.getLocale().getLanguage());
        int index = originalUrl.lastIndexOf(".");
        StringBuilder newUrl = new StringBuilder(originalUrl.substring(0, index));
        newUrl.append("_").append(lang).append(".html");
        return newUrl.toString();
    }

    private void initView() {
        mWebView.getSettings().setJavaScriptEnabled(true); // 开启javascript支持
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setAppCacheEnabled(false);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setDefaultTextEncodingName("utf-8");
//        mWebView.setWebViewClient(new WebViewClient() {
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//                return false;
//            }
//        });
    }

    private void loadWebContent(String url) {
        // 根据语言加载不同的页面，实现多语言适配
        mWebView.loadUrl(url);
    }
}
