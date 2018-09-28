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

public class AppLockImplBackup extends AppLock implements PageListener {
    private static final String TAG = "DefaultAppLock";
    private static final String PASSWORD_SALT = "7xn7@c$";

    private int liveCount;
    private int visibleCount;
    private long lastActive;

    AppLockImplBackup(Application app) {
        super();
        this.liveCount = 0;
        this.visibleCount = 0;
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
        if (visibleCount > 1) {
            Log.d(TAG, "start more than one page");
            return false;
        }

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

        lastActive = 0;
    }

    @Override
    public void onActivityCreated(Activity activity) {
        if (isIgnoredActivity(activity)) {
            return;
        }
        liveCount++;
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (isIgnoredActivity(activity)) {
            return;
        }

        liveCount--;
        if (liveCount == 0) {
            lastActive = System.currentTimeMillis();
            Log.d(TAG, "set last active " + lastActive);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity) {
//        if (isIgnoredActivity(activity)) {
//            return;
//        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        String clazzName = activity.getClass().getName();
        Log.d(TAG, "onActivityStarted " + clazzName);

        if (isIgnoredActivity(activity)) {
            return;
        }

        visibleCount++;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        String clazzName = activity.getClass().getName();
        Log.d(TAG, "onActivityStopped " + clazzName);

        if (isIgnoredActivity(activity)) {
            return;
        }

        visibleCount--;
        if (visibleCount == 0) {
            lastActive = System.currentTimeMillis();
            Log.d(TAG, "set last active " + lastActive);
        }
    }
}
