package com.huawei.smart.server.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.huawei.smart.server.R;

public class StorageUtils {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static boolean hasWriteStoragePermission(Activity activity, int requestCode) {
        int osVersion = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
        if (osVersion > 22) {
            final int flag = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (flag != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // 用户拒绝过这个权限了，应该提示用户，为什么需要这个权限。
                    Toast.makeText(activity, R.string.msg_no_write_storage_permission, Toast.LENGTH_SHORT).show();
                } else {
                    ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, requestCode);
                }
                return false;
            }
        }
        return true;
    }

    public static String getCacheDir(Activity activity) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || !Environment.isExternalStorageRemovable()) {
            try {
                return activity.getExternalCacheDir().getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        } else {
            return activity.getCacheDir().getAbsolutePath();
        }
    }

}
