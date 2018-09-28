package com.huawei.smart.server.lock;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.activity.AppLockActivity;
import com.huawei.smart.server.model.Preference;

import io.realm.Realm;

public class AppLockImpl extends AppLock implements PageListener {
    private static final String TAG = "DefaultAppLock";
    private static final String PASSWORD_SALT = "7xn7@c$";
    private long lastActive;

    AppLockImpl(Application app) {
        super();
    }

    @Override
    public void enable() {
        BaseActivity.setListener(this);
    }

    @Override
    public void disable() {
        BaseActivity.setListener(null);
    }

    @Override
    public boolean checkPasscode(String passcode) {
        passcode = PASSWORD_SALT + passcode + PASSWORD_SALT;
        passcode = Encryptor.getSHA1(passcode);
        final Preference preference = Realm.getDefaultInstance().where(Preference.class).findFirst();
        return passcode.equalsIgnoreCase(preference.getPasscode());
    }

    @Override
    public boolean setPasscode(final String passcode) {
        final Realm realm = Realm.getDefaultInstance();
        final Preference preference = realm.where(Preference.class).findFirst();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (passcode == null) {
                    preference.setPasscode(null);
                    preference.setIsFingerprintEnabled(false);
                    disable();
                } else {
                    preference.setPasscode(Encryptor.getSHA1(PASSWORD_SALT + passcode + PASSWORD_SALT));
                    enable();
                }
            }
        });
        return true;
    }

    @Override
    public boolean isPasscodeSet() {
        final Realm realm = Realm.getDefaultInstance();
        final Preference preference = realm.where(Preference.class).findFirst();
        return !TextUtils.isEmpty(preference.getPasscode());
    }

    @Override
    public boolean isFingerprintEnabled() {
        final Realm realm = Realm.getDefaultInstance();
        final Preference preference = realm.where(Preference.class).findFirst();
        return preference.getIsFingerprintEnabled();
    }

    @Override
    public boolean enableFingerprint(final boolean enable) {
        final Realm realm = Realm.getDefaultInstance();
        final Preference preference = realm.where(Preference.class).findFirst();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                preference.setIsFingerprintEnabled(enable);
            }
        });
        return true;
    }

    @Override
    public void updateLastActiveOn() {
        this.lastActive = System.currentTimeMillis();
    }

    private boolean isIgnoredActivity(Activity activity) {
        String clazzName = activity.getClass().getName();
        // ignored activities
        if (ignoredActivities.contains(clazzName)) {
            Log.d(TAG, "ignore activity " + clazzName);
            return true;
        }
        return false;
    }

    public boolean shouldLockScreen(Activity activity) {
        // already unlock
        if (activity instanceof AppLockActivity) {
            AppLockActivity ala = (AppLockActivity) activity;
            if (ala.getType() == AppLock.UNLOCK_PASSWORD) {
                Log.d(TAG, "already unlock activity");
                return false;
            }
        }

        // no pass code set
        if (!isPasscodeSet()) {
            Log.d(TAG, "lock passcode not set.");
            return false;
        }

        // no enough timeout
        long passedTime = System.currentTimeMillis() - lastActive;
        if (lastActive > 0 && passedTime <= lockTimeOut) {
            Log.d(TAG, "no enough timeout " + passedTime + " for "
                + lockTimeOut);
            return false;
        }

        // start more than one page
//        if (visibleCount > 1) {
//            Log.d(TAG, "start more than one page");
//            return false;
//        }

        return true;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        String clazzName = activity.getClass().getName();
        Log.d(TAG, "onActivityPaused " + clazzName);
//        isIgnoredActivity(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        String clazzName = activity.getClass().getName();
        Log.d(TAG, "onActivityResumed " + clazzName);
        if (isIgnoredActivity(activity)) {
            return;
        }

        if (shouldLockScreen(activity)) {
            Intent intent = new Intent(activity.getApplicationContext(), AppLockActivity.class);
            intent.putExtra(AppLock.TYPE, AppLock.UNLOCK_PASSWORD);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.getApplication().startActivity(intent);
        }

        // 在启动界面时候，设置最后启动时间
        lastActive = System.currentTimeMillis();
    }

    @Override
    public void onActivityCreated(Activity activity) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        String clazzName = activity.getClass().getName();
        Log.d(TAG, "onActivityStarted " + clazzName);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        String clazzName = activity.getClass().getName();
        Log.d(TAG, "onActivityStopped " + clazzName);
    }
}
