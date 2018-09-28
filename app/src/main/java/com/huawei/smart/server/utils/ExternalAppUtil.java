package com.huawei.smart.server.utils;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.List;

public class ExternalAppUtil {
    private static final String TAG = "ExternalAppUtil";

    public static boolean canHandleSchema(final Context context, String uriString) {
        Intent intent = new Intent();
        intent.setData(Uri.parse(uriString));
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> resolvedActivities = packageManager.queryIntentActivities(intent, 0);
        return resolvedActivities.size() > 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean canHandleSchema(final Context context, String scheme, String action) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(action, Uri.parse(scheme));
        List list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
        return list.size() > 0;
    }

    public static void go2PlayStore(final Context context, String appPackageName) {
        String playStoreUri = "market://details?id=" + appPackageName;
        if (!canHandleSchema(context, playStoreUri)) {
            playStoreUri = "https://play.google.com/store/apps/details?id=" + appPackageName;
        }
        go2App(context, playStoreUri);
    }

    public static void go2App(final Context context, String uri) {
        Intent intent = getGo2AppIntent(context, uri);
        if (intent != null) {
            context.startActivity(intent);
        }
    }

    public static Intent getGo2AppIntent(final Context context, String uri) {
        if (!canHandleSchema(context, uri, Intent.ACTION_VIEW)) {
            DialogUtil.showInstallBrowserDialog(context);
            return null;
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        }
    }

}
