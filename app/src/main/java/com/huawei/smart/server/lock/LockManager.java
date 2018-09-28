package com.huawei.smart.server.lock;

import android.app.Application;

public class LockManager {

    private AppLock curAppLocker;

    private static class LockManagerHolder {
        private static final LockManager instance = new LockManager();
    }

    public static LockManager getInstance() {
        return LockManagerHolder.instance;
    }

    public void enableAppLock(Application app) {
        if (curAppLocker == null) {
            curAppLocker = new AppLockImpl(app);
        }
        curAppLocker.enable();
    }

    public boolean isAppLockEnabled() {
        return curAppLocker != null;
    }

    public void setAppLock(AppLock appLocker) {
        if (curAppLocker != null) {
            curAppLocker.disable();
        }
        curAppLocker = appLocker;
    }

    public AppLock getAppLock() {
        return curAppLocker;
    }
}
