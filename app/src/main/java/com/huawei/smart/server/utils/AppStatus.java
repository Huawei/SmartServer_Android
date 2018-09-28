package com.huawei.smart.server.utils;

import android.content.SharedPreferences;

import com.huawei.smart.server.BuildConfig;
import com.huawei.smart.server.HWApplication;

import static android.content.Context.MODE_PRIVATE;

/**
 * @author coa.ke on 3/31/18
 */
public class AppStatus
{
    public static boolean isFirstInstall() {
        SharedPreferences sp = HWApplication.getContextHW().getSharedPreferences("HWMApp", MODE_PRIVATE);
        int lastVersionCode = sp.getInt("FIRSTTIMERUN", -1);
        boolean isFreshInstall = false;
        if (lastVersionCode == -1) {
            isFreshInstall = true;
        }

        return isFreshInstall;
    }

    public static boolean isUpgrade() {
        SharedPreferences sp = HWApplication.getContextHW().getSharedPreferences("HWMApp", MODE_PRIVATE);
        int lastVersionCode = sp.getInt("FIRSTTIMERUN", -1);
        int currentVersionCode = BuildConfig.VERSION_CODE;
        boolean isUpgrade = false;
        if (lastVersionCode == -1 && lastVersionCode != currentVersionCode) {
            isUpgrade = true;
        }

        return isUpgrade;
    }


    public static void updateFirstInstallFlag() {
        SharedPreferences sp = HWApplication.getContextHW().getSharedPreferences("HWMApp", MODE_PRIVATE);
        int currentVersionCode = BuildConfig.VERSION_CODE;
        sp.edit().putInt("FIRSTTIMERUN", currentVersionCode).apply();
    }
}
