package com.huawei.smart.server.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.R;
import com.huawei.smart.server.lock.AppLock;
import com.huawei.smart.server.lock.LockManager;
import com.suke.widget.SwitchButton;
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify;
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint;

import org.slf4j.LoggerFactory;

import butterknife.BindView;
import butterknife.OnClick;

public class SecurityActivity extends BaseActivity {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SecurityActivity.class.getSimpleName());

    public static final String TAG = "Security";

    @BindView(R.id.lock_switch) SwitchButton lockSwitch;
    @BindView(R.id.fingerprint_switch) SwitchButton fingerprintSwitch;
    @BindView(R.id.change_passcode) View changePasscode;
    @BindView(R.id.fingerprint) View fingerprint;

    private FingerprintIdentify mFingerprintIdentify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);
        this.initialize(R.string.title_security, true);

        initializeView();
    }

    private void initializeView() {

        lockSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                boolean enabled = LockManager.getInstance().getAppLock().isPasscodeSet();
                if (enabled ^ isChecked) {
                    int type = LockManager.getInstance().getAppLock().isPasscodeSet() ? AppLock.DISABLE_PASSLOCK
                        : AppLock.ENABLE_PASSLOCK;
                    Intent intent = new Intent(SecurityActivity.this, AppLockActivity.class);
                    intent.putExtra(AppLock.TYPE, type);
                    startActivityForResult(intent, type);
                }
            }
        });

        fingerprintSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                boolean enabled = LockManager.getInstance().getAppLock().isFingerprintEnabled();
                if (enabled ^ isChecked) { // 有修改
                    if (isChecked) { // 打开
                        final FingerprintIdentify identify = getFingerprintIdentify();
                        if (!identify.isFingerprintEnable()) {
                            showToast(R.string.security_msg_fingerprint_enable_failed, Toast.LENGTH_SHORT, Gravity.CENTER);
                        } else {
                            LockManager.getInstance().getAppLock().enableFingerprint(true);     // 开启指纹解锁
                            showToast(R.string.security_msg_fingerprint_enabled, Toast.LENGTH_SHORT, Gravity.CENTER);
                        }
                    } else {
                        LockManager.getInstance().getAppLock().enableFingerprint(false);     // 开启指纹解锁
                        showToast(R.string.security_msg_fingerprint_disabled, Toast.LENGTH_SHORT, Gravity.CENTER);
                    }
                }
            }
        });

        updateUI();
    }

    private FingerprintIdentify getFingerprintIdentify() {
        if (mFingerprintIdentify == null) {
            mFingerprintIdentify = new FingerprintIdentify(getApplicationContext(),
                new BaseFingerprint.FingerprintIdentifyExceptionListener() {
                    @Override
                    public void onCatchException(Throwable exception) {
                        LOG.error("Finger identify exception", exception);
                    }
                });
        }
        return mFingerprintIdentify;
    }


    @OnClick(R.id.change_passcode)
    public void changePasscode() {
        Intent intent = new Intent(this, AppLockActivity.class);
        intent.putExtra(AppLock.TYPE, AppLock.CHANGE_PASSWORD);
        intent.putExtra(AppLock.MESSAGE, getString(R.string.enter_old_passcode));
        startActivityForResult(intent, AppLock.CHANGE_PASSWORD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case AppLock.DISABLE_PASSLOCK:
                if (resultCode == RESULT_OK) {
                    showToast(R.string.disable_passcode_success, Toast.LENGTH_SHORT, Gravity.CENTER);
                }
                break;
            case AppLock.ENABLE_PASSLOCK:
                if (resultCode == RESULT_OK) {
                    showToast(R.string.setup_passcode_success, Toast.LENGTH_SHORT, Gravity.CENTER);
                }
                break;
            case AppLock.CHANGE_PASSWORD:
                if (resultCode == RESULT_OK) {
                    showToast(R.string.change_passcode_success, Toast.LENGTH_SHORT, Gravity.CENTER);
                }
                break;
            default:
                break;
        }
        updateUI();
    }

    private void updateUI() {
        final AppLock appLock = LockManager.getInstance().getAppLock();
        final boolean isPasswordProtected = appLock.isPasscodeSet();

        changePasscode.setVisibility(isPasswordProtected ? View.VISIBLE : View.GONE);
        fingerprint.setVisibility(isPasswordProtected ? View.VISIBLE : View.GONE);

        lockSwitch.setChecked(isPasswordProtected);
        fingerprintSwitch.setChecked(appLock.isFingerprintEnabled());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFingerprintIdentify != null) {
            mFingerprintIdentify.cancelIdentify();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFingerprintIdentify != null) {
            mFingerprintIdentify.cancelIdentify();
        }
    }

}
