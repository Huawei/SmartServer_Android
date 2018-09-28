package com.huawei.smart.server.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.blankj.utilcode.util.ActivityUtils;
import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.HWConstants;
import com.huawei.smart.server.R;
import com.huawei.smart.server.utils.Compatibility;

import butterknife.BindView;
import butterknife.OnClick;

public class LanguageActivity extends BaseActivity {

    @BindView(R.id.langRadioGroup) RadioGroup langRadioGroup;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lang);
        this.initialize(R.string.profile_lang, true);
        preferences = this.getSharedPreferences(HWConstants.PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
        final String lang = preferences.getString(HWConstants.PREFERENCE_SETTING_LANG, Compatibility.getLocale().getLanguage());
        if (lang.equals("zh")) {
            ((RadioButton) langRadioGroup.getChildAt(0)).setChecked(true);
        } else { // default
            ((RadioButton) langRadioGroup.getChildAt(1)).setChecked(true);
        }
    }

    @OnClick(R.id.submit)
    public void onSubmit() {
        final int checkedRadioButtonId = langRadioGroup.getCheckedRadioButtonId();
        final SharedPreferences.Editor editor = preferences.edit();
        if (checkedRadioButtonId == R.id.zh) {
            editor.putString(HWConstants.PREFERENCE_SETTING_LANG, "zh");
        } else if (checkedRadioButtonId == R.id.en) {
            editor.putString(HWConstants.PREFERENCE_SETTING_LANG, "en");
        }
        editor.apply();
        ActivityUtils.finishAllActivities();
        ActivityUtils.startActivity(MainActivity.class);
    }

}
