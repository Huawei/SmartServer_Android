package com.huawei.smart.server.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebView;

import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.HWApplication;
import com.huawei.smart.server.HWConstants;
import com.huawei.smart.server.R;
import com.huawei.smart.server.utils.Compatibility;

import java.io.InputStream;

import butterknife.BindView;

/**
 * 用户协议
 */
public class PolicyActivity extends BaseActivity {


    @BindView(R.id.browser)
    WebView browser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy);
        this.initialize(R.string.title_policy, true);
//        final InputStream is = getResources().openRawResource(R.raw.policy);
        browser.getSettings().setDefaultTextEncodingName("utf-8");
        String url = "file:///android_asset/policy.html";
        browser.loadUrl(constructUrl(url));
    }

    private String constructUrl(String originalUrl) {
        SharedPreferences preferences = HWApplication.getContextHW().getSharedPreferences(HWConstants.PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
        String lang = preferences.getString(HWConstants.PREFERENCE_SETTING_LANG, Compatibility.getLocale().getLanguage());
        int index = originalUrl.lastIndexOf(".");
        StringBuilder newUrl = new StringBuilder(originalUrl.substring(0, index));
        newUrl.append("_").append(lang).append(".html");
        return newUrl.toString();
    }

}
