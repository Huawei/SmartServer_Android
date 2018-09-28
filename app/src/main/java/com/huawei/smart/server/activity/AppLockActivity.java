package com.huawei.smart.server.activity;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.R;
import com.huawei.smart.server.lock.AppLock;
import com.huawei.smart.server.lock.LockManager;
import com.huawei.smart.server.utils.AppStatus;
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify;
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint;

import org.slf4j.LoggerFactory;

import butterknife.BindView;
import butterknife.OnClick;

public class AppLockActivity extends BaseActivity {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AppLockActivity.class.getSimpleName());

    public static final String TAG = "AppLockActivity";
    protected InputFilter[] filters = null;
    @BindView(R.id.passcode_1)
    EditText codeField1;
    @BindView(R.id.passcode_2)
    EditText codeField2;
    @BindView(R.id.passcode_3)
    EditText codeField3;
    @BindView(R.id.passcode_4)
    EditText codeField4;
    @BindView(R.id.passcode_5)
    EditText codeField5;
    @BindView(R.id.passcode_6)
    EditText codeField6;
    @BindView(R.id.button0)
    Button button0;
    @BindView(R.id.button1)
    Button button1;
    @BindView(R.id.button2)
    Button button2;
    @BindView(R.id.button3)
    Button button3;
    @BindView(R.id.button4)
    Button button4;
    @BindView(R.id.button5)
    Button button5;
    @BindView(R.id.button6)
    Button button6;
    @BindView(R.id.button7)
    Button button7;
    @BindView(R.id.button8)
    Button button8;
    @BindView(R.id.button9)
    Button button9;
    @BindView(R.id.button_clear)
    Button buttonClear;
    @BindView(R.id.button_erase) Button buttonErase;
    @BindView(R.id.tv_message) TextView tvMessage;
    @BindView(R.id.error_message) TextView errorMessage;
    @BindView(R.id.fingerprint) View fingerprint;

    private MaterialDialog fingerIdentifyDialog;
    private FingerprintIdentify mFingerprintIdentify;

    private int type = -1;
    private String oldPasscode = null;
    private boolean mCancelable = true;
    private boolean mInitialize = true;

    private String titleMessage;
    private String confirmTitleMessage;

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            errorMessage.setVisibility(View.GONE);
            int currentValue = -1;
            int id = view.getId();
            if (id == R.id.button0) {
                currentValue = 0;
            } else if (id == R.id.button1) {
                currentValue = 1;
            } else if (id == R.id.button2) {
                currentValue = 2;
            } else if (id == R.id.button3) {
                currentValue = 3;
            } else if (id == R.id.button4) {
                currentValue = 4;
            } else if (id == R.id.button5) {
                currentValue = 5;
            } else if (id == R.id.button6) {
                currentValue = 6;
            } else if (id == R.id.button7) {
                currentValue = 7;
            } else if (id == R.id.button8) {
                currentValue = 8;
            } else if (id == R.id.button9) {
                currentValue = 9;
            }

            // set the value and move the focus
            String currentValueString = String.valueOf(currentValue);
            if (codeField1.isFocused()) {
                codeField1.setText(currentValueString);
                codeField2.requestFocus();
                codeField2.setText("");
            } else if (codeField2.isFocused()) {
                codeField2.setText(currentValueString);
                codeField3.requestFocus();
                codeField3.setText("");
            } else if (codeField3.isFocused()) {
                codeField3.setText(currentValueString);
                codeField4.requestFocus();
                codeField4.setText("");
            } else if (codeField4.isFocused()) {
                codeField4.setText(currentValueString);
                codeField5.requestFocus();
                codeField5.setText("");
            } else if (codeField5.isFocused()) {
                codeField5.setText(currentValueString);
                codeField6.requestFocus();
                codeField6.setText("");
            } else if (codeField6.isFocused()) {
                codeField6.setText(currentValueString);
            }

            if (codeField6.getText().toString().length() > 0
                && codeField5.getText().toString().length() > 0
                && codeField4.getText().toString().length() > 0
                && codeField3.getText().toString().length() > 0
                && codeField2.getText().toString().length() > 0
                && codeField1.getText().toString().length() > 0)
            {
                onPasscodeInputFinished();
            }
        }
    };
    private InputFilter numberFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            if (source.length() > 1) {
                return "";
            }
            if (source.length() == 0) {
                return null;
            }

            try {
                int number = Integer.parseInt(source.toString());
                if ((number >= 0) && (number <= 9)) {
                    return String.valueOf(number);
                } else {
                    return "";
                }
            } catch (NumberFormatException e) {
                return "";
            }
        }
    };
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            v.performClick();
            clearFields();
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcode_setting);
        this.initialize(R.string.security_label_set_app_password, true);

        Bundle extras = getIntent().getExtras();
        titleMessage = getExtraString(AppLock.MESSAGE);
        confirmTitleMessage = getExtraString(AppLock.CONFIRM_MESSAGE);
        if (extras != null) {
            type = extras.getInt(AppLock.TYPE, -1);
            mCancelable = extras.getBoolean(AppLock.CANCELABLE);
            if (mCancelable) {
                setVisibilityOfLeftButton(View.VISIBLE);
            } else {
                setVisibilityOfLeftButton(View.GONE);
            }

            mInitialize = extras.getBoolean(AppLock.INITIALIZE, false);
        }

        if (!TextUtils.isEmpty(titleMessage)) {
            tvMessage.setText(titleMessage);
        }

        filters = new InputFilter[2];
        filters[0] = new InputFilter.LengthFilter(1);
        filters[1] = numberFilter;

        setupEditText(codeField1);
        setupEditText(codeField2);
        setupEditText(codeField3);
        setupEditText(codeField4);
        setupEditText(codeField5);
        setupEditText(codeField6);

        // setup the keyboard
        button0.setOnClickListener(btnListener);
        button1.setOnClickListener(btnListener);
        button2.setOnClickListener(btnListener);
        button3.setOnClickListener(btnListener);
        button4.setOnClickListener(btnListener);
        button5.setOnClickListener(btnListener);
        button6.setOnClickListener(btnListener);
        button7.setOnClickListener(btnListener);
        button8.setOnClickListener(btnListener);
        button9.setOnClickListener(btnListener);

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearFields();
            }
        });

        buttonErase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDeleteKey();
            }
        });

        overridePendingTransition(R.anim.slide_up, R.anim.nothing);
        switch (type) {

            case AppLock.DISABLE_PASSLOCK:
                setTitleTxt(R.string.disable_passcode_title);
                break;
            case AppLock.ENABLE_PASSLOCK:
                setTitleTxt(R.string.enable_passcode_title);
                break;
            case AppLock.CHANGE_PASSWORD:
                setTitleTxt(R.string.change_passcode_title);
                break;
            case AppLock.UNLOCK_PASSWORD:
                setTitleTxt(R.string.unlock_title);
                setVisibilityOfLeftButton(View.GONE);
                break;
        }
    }

    public int getType() {
        return type;
    }

    protected void onPasscodeInputFinished() {
        StringBuilder passLockBuild = new StringBuilder();
        passLockBuild.append(codeField1.getText().toString())
            .append(codeField2.getText().toString())
            .append(codeField3.getText().toString())
            .append(codeField4.getText().toString())
            .append(codeField5.getText().toString())
            .append(codeField6.getText().toString());
        String passLock = passLockBuild.toString();

        final boolean verify = LockManager.getInstance().getAppLock().checkPasscode(passLock);
        verifyHandle(passLock, verify);
    }

    private void verifyHandle(String passLock, boolean verify) {
        switch (type) {
            case AppLock.DISABLE_PASSLOCK:
                if (verify) {
                    setResult(RESULT_OK);
                    LockManager.getInstance().getAppLock().setPasscode(null);
                    finish();
                } else {
                    clearInputs();
                    onPasscodeError(R.string.unlock_error);
                }
                break;

            case AppLock.ENABLE_PASSLOCK:
                if (oldPasscode == null) {
                    clearInputs();
                    tvMessage.setText(TextUtils.isEmpty(confirmTitleMessage) ? getString(R.string.reenter_passcode) : confirmTitleMessage);
                    oldPasscode = passLock;
                } else {
                    if (passLock.equals(oldPasscode)) {
                        LockManager.getInstance().getAppLock().setPasscode(passLock);
                        setResult(RESULT_OK);
                        if (mInitialize) {
                            AppStatus.updateFirstInstallFlag();
                            Intent intent = new Intent(AppLockActivity.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            finish();
                        }
                    } else {
                        clearInputs();
                        oldPasscode = null;
                        tvMessage.setText(TextUtils.isEmpty(titleMessage) ? getString(R.string.enter_passcode) : titleMessage);
                        onPasscodeError(R.string.passcode_wrong);
                    }
                }
                break;

            case AppLock.CHANGE_PASSWORD:
                if (verify) {
                    tvMessage.setText(R.string.enter_new_passcode);
                    type = AppLock.ENABLE_PASSLOCK;
                } else {
                    onPasscodeError(R.string.unlock_error);
                }
                clearInputs();
                break;

            case AppLock.UNLOCK_PASSWORD:
                if (verify) {
                    // 验证通过，设置最后激活时间
                    LockManager.getInstance().getAppLock().updateLastActiveOn();
                    finish();
                } else {
                    clearInputs();
                    onPasscodeError(R.string.unlock_error);
                }
                break;

            default:
                break;
        }
    }

    private void clearInputs() {
        clearCodeFieldValue();
        codeField1.requestFocus();
    }

    private void clearCodeFieldValue() {
        codeField1.setText("");
        codeField2.setText("");
        codeField3.setText("");
        codeField4.setText("");
        codeField5.setText("");
        codeField6.setText("");
    }

    @Override
    public void onBackPressed() {
        if (type == AppLock.UNLOCK_PASSWORD) {
            // back to home screen
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            this.startActivity(intent);
            finish();
        } else {
            finish();
        }
    }

    protected void setupEditText(EditText editText) {
        editText.setInputType(InputType.TYPE_NULL);
        editText.setFilters(filters);
        editText.setOnTouchListener(touchListener);
        editText.setTransformationMethod(PasswordTransformationMethod
            .getInstance());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            onDeleteKey();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void onDeleteKey() {
        if (!codeField1.isFocused()) {
            if (codeField2.isFocused()) {
                codeField1.requestFocus();
                codeField1.setText("");
            } else if (codeField3.isFocused()) {
                codeField2.requestFocus();
                codeField2.setText("");
            } else if (codeField4.isFocused()) {
                codeField3.requestFocus();
                codeField3.setText("");
            } else if (codeField5.isFocused()) {
                codeField4.requestFocus();
                codeField4.setText("");
            } else if (codeField6.isFocused()) {
                codeField5.requestFocus();
                codeField5.setText("");
            }
        }
    }

    protected void onPasscodeError(int resId) {
        errorMessage.setVisibility(View.VISIBLE);
        errorMessage.setText(resId);
        Thread thread = new Thread() {
            @Override
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(
                    AppLockActivity.this, R.anim.shake);
                findViewById(R.id.ll_applock).startAnimation(animation);
                clearInputs();
            }
        };
        runOnUiThread(thread);
    }

    private void clearFields() {
        clearCodeFieldValue();
        codeField1.postDelayed(new Runnable() {

            @Override
            public void run() {
                codeField1.requestFocus();
            }
        }, 200);
    }

    private void startFingerprintIdentify() {
        getFingerprintIdentify().startIdentify(3, new BaseFingerprint.FingerprintIdentifyListener() {
            @Override
            public void onSucceed() {
                LOG.info("Finger identify successfully");
                LockManager.getInstance().getAppLock().updateLastActiveOn();
                fingerIdentifyDialog.dismiss();
                verifyHandle(null, true);
            }

            @Override
            public void onNotMatch(int availableTimes) {
                final TextView tips = fingerIdentifyDialog.getCustomView().findViewById(R.id.tips);
                if (availableTimes > 0) {
                    tips.setText(R.string.security_label_fingerprint_unlock_failed);
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            Animation animation = AnimationUtils.loadAnimation(
                                AppLockActivity.this, R.anim.shake);
                            tips.startAnimation(animation);
                        }
                    };
                    runOnUiThread(thread);
                } else {
                    fingerIdentifyDialog.dismiss();
                    showToast(R.string.security_label_fingerprint_unlock_retry, Toast.LENGTH_SHORT, Gravity.CENTER);
                }
            }

            @Override
            public void onFailed(boolean isDeviceLocked) {
                if (!isDeviceLocked) {
                    fingerIdentifyDialog.dismiss();
                    showToast(R.string.security_label_fingerprint_unlock_retry, Toast.LENGTH_SHORT, Gravity.CENTER);
                } else {
                    fingerIdentifyDialog.dismiss();
                    showToast(R.string.security_label_fingerprint_locked, Toast.LENGTH_SHORT, Gravity.CENTER);
                }
            }

            @Override
            public void onStartFailedByDeviceLocked() {
                fingerIdentifyDialog.dismiss();
                showToast(R.string.security_label_fingerprint_locked, Toast.LENGTH_SHORT, Gravity.CENTER);
            }
        });

    }


    @OnClick(R.id.fingerprint)
    @TargetApi(Build.VERSION_CODES.M)
    public void validateByFingerprint() {
        this.fingerIdentifyDialog = new MaterialDialog.Builder(this)
            .iconRes(R.mipmap.ic_fingerprint_36dp)
            .customView(R.layout.widget_fingerprint, false)
            .autoDismiss(false)
            .showListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    startFingerprintIdentify(); // 启动指纹认证
                }
            })
            .dismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    if (mFingerprintIdentify != null) {
                        mFingerprintIdentify.cancelIdentify();
                    }
                }
            })
            .show();

        this.fingerIdentifyDialog.setCanceledOnTouchOutside(false);
        fingerIdentifyDialog.getCustomView().findViewById(R.id.cancel)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fingerIdentifyDialog.dismiss();
                }
            });
    }

    private FingerprintIdentify getFingerprintIdentify() {
        if (mFingerprintIdentify == null) {
            mFingerprintIdentify = new FingerprintIdentify(getApplicationContext(),
                new BaseFingerprint.FingerprintIdentifyExceptionListener() {
                    @Override
                    public void onCatchException(Throwable exception) {
                        LOG.warn("Fingerprint authentication failed", exception);
                    }
                });
        }
        return mFingerprintIdentify;
    }

    @Override
    protected void onResume() {
        super.onResume();
        final boolean fingerprintEnabled = LockManager.getInstance().getAppLock().isFingerprintEnabled();
        if (fingerprintEnabled && !mInitialize) {
            validateByFingerprint();
            fingerprint.setVisibility(View.VISIBLE);
        } else {
            fingerprint.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.fingerIdentifyDialog != null && this.fingerIdentifyDialog.isShowing()) {
            this.fingerIdentifyDialog.dismiss();
        }
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

        if (fingerIdentifyDialog != null && fingerIdentifyDialog.isShowing()) {
            fingerIdentifyDialog.dismiss();
        }
    }
}