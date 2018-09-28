package com.huawei.smart.server.utils;

import android.os.Build;
import android.os.LocaleList;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Locale;

public class Compatibility {

    private static final String TAG = "Compatibility";
    private static Integer sdkVersion = null;

    public static synchronized int getApiLevel() {
        if (sdkVersion == null) {
            if ("3".equalsIgnoreCase(android.os.Build.VERSION.SDK)) {
                sdkVersion = 3;
            } else {
                try {
                    Field f = android.os.Build.VERSION.class.getDeclaredField("SDK_INT");
                    if (null != f) {
                        sdkVersion = (Integer) f.get(null);
                    }
                } catch (Throwable t) {
                    Log.e(TAG, "getApiLevel(), exception", t);
                    sdkVersion = 0;
                }
            }
        }

        return sdkVersion;
    }


    public static boolean isCompatibleWith(int sdkVersion) {
        return getApiLevel() >= sdkVersion;
    }

    public static Locale getLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return LocaleList.getDefault().get(0);
        } else {
            return Locale.getDefault();
        }
    }
}
