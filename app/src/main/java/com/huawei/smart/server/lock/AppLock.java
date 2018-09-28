package com.huawei.smart.server.lock;

import android.app.Activity;

import com.huawei.smart.server.activity.SplashActivity;

import java.util.HashSet;

public abstract class AppLock {

    public static final int ENABLE_PASSLOCK = 0;
    public static final int DISABLE_PASSLOCK = 1;
    public static final int CHANGE_PASSWORD = 2;
    public static final int UNLOCK_PASSWORD = 3;

    public static final String MESSAGE = "message";
    public static final String CONFIRM_MESSAGE = "confirm_message";
    public static final String TYPE = "type";
    public static final String CANCELABLE = "cancelable";
    public static final String INITIALIZE = "initialize";

    private static final long DEFAULT_TIMEOUT = 3 * 60 * 1000; // 3 minutes
//    private static final long DEFAULT_TIMEOUT = 0L; // 2000ms

    long lockTimeOut;
    HashSet<String> ignoredActivities;

    public void setTimeout(int timeout) {
        this.lockTimeOut = timeout;
    }

    AppLock() {
        ignoredActivities = new HashSet<>();
        ignoredActivities.add(SplashActivity.class.getName());
        lockTimeOut = DEFAULT_TIMEOUT;
    }

    public void addIgnoredActivity(Class<?> clazz) {
        String clazzName = clazz.getName();
        this.ignoredActivities.add(clazzName);
    }

    public void removeIgnoredActivity(Class<?> clazz) {
        String clazzName = clazz.getName();
        this.ignoredActivities.remove(clazzName);
    }

    public abstract void enable();

    public abstract void disable();

    public abstract boolean setPasscode(String passcode);

    /**
     * 验证
     * @param passcode
     * @return
     */
    public abstract boolean checkPasscode(String passcode);

    public abstract boolean isPasscodeSet();

    public abstract boolean isFingerprintEnabled();

    public abstract boolean enableFingerprint(boolean enable);

    public abstract boolean shouldLockScreen(Activity activity);

    /**
     * 设置最后启动时间
     */
    public abstract void updateLastActiveOn();
}
