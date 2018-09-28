package com.huawei.smart.server.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.BuildConfig;
import com.huawei.smart.server.HWConstants;
import com.huawei.smart.server.R;
import com.huawei.smart.server.upgrade.UpgradeManager;
import com.huawei.smart.server.utils.AppUpgradeUtils;
import com.huawei.smart.server.utils.ExternalAppUtil;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 关于我们
 */
public class AboutUsActivity extends BaseActivity {

    private static final String TAG = "Upgrade";

    @BindView(R.id.version) TextView versionView;

    UpgradeManager upgradeManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        this.initialize(R.string.title_about_us, true);
        // 设置版本
        versionView.setText(getString(R.string.app_full_name) + " " + BuildConfig.VERSION_NAME);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            upgradeManager.upgrade();
        }
    }

    /**
     * 去评分
     */
    @OnClick(R.id.rate)
    public void onRateClick() {
        ExternalAppUtil.go2PlayStore(this, getPackageName());
    }

    @OnClick(R.id.policy)
    public void onPolicyClick() {
        ActivityUtils.startActivity(PolicyActivity.class);
    }

    @OnClick(R.id.introduce)
    public void onIntroduceClick() {
        ActivityUtils.startActivity(AppVersionIntroductionActivity.class);
    }

    @OnClick(R.id.upgrade)
    public void onUpgradeClick() {
        final String checkUpgradeUrl = AppUpgradeUtils.getManifestString(this, HWConstants.KEY_UPGRADE_CHECK_URL);
        upgradeManager = UpgradeManager.builder().activity(this).silent(false).checkUrl(checkUpgradeUrl).build();
        upgradeManager.check();
    }

}
