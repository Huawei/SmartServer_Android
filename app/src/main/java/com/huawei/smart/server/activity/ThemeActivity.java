package com.huawei.smart.server.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.HWConstants;
import com.huawei.smart.server.R;

import butterknife.BindView;
import butterknife.OnClick;

public class ThemeActivity extends BaseActivity {

    @BindView(R.id.theme_green)
    FrameLayout green;

    @BindView(R.id.theme_dark)
    FrameLayout dark;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);
        this.initialize(R.string.profile_theme, true);
        preferences = this.getSharedPreferences(HWConstants.PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
        final String theme = preferences.getString(HWConstants.PREFERENCE_SETTING_THEME, HWConstants.THEME_DARK);
        if (theme.equals(HWConstants.THEME_DARK)) {
            onDarkThemeSelect();
        } else if (theme.equals(HWConstants.THEME_GREEN)) {
            onGreenThemeSelect();
        }
    }

    @OnClick({R.id.theme_green})
    public void onGreenThemeSelect() {
        green.getChildAt(0).setVisibility(View.VISIBLE);
        dark.getChildAt(0).setVisibility(View.GONE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(HWConstants.PREFERENCE_SETTING_THEME, HWConstants.THEME_GREEN);
        editor.apply();
        HWConstants.THEME = HWConstants.THEME_GREEN;

        super.onResume();
    }

    @OnClick({R.id.theme_dark})
    public void onDarkThemeSelect() {
        dark.getChildAt(0).setVisibility(View.VISIBLE);
        green.getChildAt(0).setVisibility(View.GONE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(HWConstants.PREFERENCE_SETTING_THEME, HWConstants.THEME_DARK);
        editor.apply();
        HWConstants.THEME = HWConstants.THEME_DARK;
        super.onResume();
    }

}
