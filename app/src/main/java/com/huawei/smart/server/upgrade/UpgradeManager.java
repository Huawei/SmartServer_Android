package com.huawei.smart.server.upgrade;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.ActivityUtils;
import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.HWConstants;
import com.huawei.smart.server.R;
import com.huawei.smart.server.utils.AppUpgradeUtils;
import com.huawei.smart.server.utils.Compatibility;
import com.huawei.smart.server.utils.StorageUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpgradeManager implements Serializable {

    private static final String APP_KEY = "UPDATE_APP_KEY";

    BaseActivity activity;
    String checkUrl;        // 查询是否有更新地址
    String downloadTo;      // 下载路径
    Boolean silent;
    HttpManager httpManager;
    MaterialDialog dialog;
    AppUpgrade upgrade;


    public void check() {
        if (httpManager == null) {
            httpManager = new FANHttpManager();
        }

        // 请求参数
        Map<String, String> params = new HashMap<String, String>();
        params.put("version", getVersionName());
        params.put("lang", Locale.getDefault().getLanguage());

        if (!silent) {
            activity.showLoadingDialog();
        }
        httpManager.asyncGet(checkUrl, params, new HttpManager.Callback() {
            @Override
            public void onResponse(final AppUpgrade upgrade) {
                UpgradeManager.this.upgrade = upgrade;
                if (upgrade.isHasNewVersion()) {
                    activity.dismissLoadingDialog();
                    showUpgradeDialog();
                } else if (!silent) {
                    activity.dismissLoadingDialog();
                    Toast.makeText(activity, R.string.upgrade_app_is_latest, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                if (!silent) {
                    Toast.makeText(activity, R.string.upgrade_app_is_latest, Toast.LENGTH_SHORT).show();
                    activity.dismissLoadingDialog();
                }
            }
        });

    }

    private void showUpgradeDialog() {
        dialog = new MaterialDialog.Builder(activity)
            .customView(R.layout.widget_upgrade, false)
            .autoDismiss(false)
            .show();

        final View dialogView = dialog.getCustomView();

        SharedPreferences preferences = activity.getSharedPreferences(HWConstants.PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
        final String lang = preferences.getString(HWConstants.PREFERENCE_SETTING_LANG, Compatibility.getLocale().getLanguage());
        final boolean zh = lang.equals("zh");
        ((TextView) dialogView.findViewById(R.id.updateContent)).setText(zh ? upgrade.getUpdateContentZh() : upgrade.getUpdateContentEn());

        dialogView.findViewById(R.id.later).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialogView.findViewById(R.id.upgrade).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(zh ? upgrade.getAppStoreZh() : upgrade.getAppStoreEn()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ActivityUtils.startActivity(intent);
//                if (!DownloadService.isRunning) {
//                    final boolean hasPermission = StorageUtils.hasWriteStoragePermission(activity, HWConstants.WRITE_STORAGE_REQUEST);
//                    if (hasPermission) {
//                        upgrade();
//                    }
//                } else {
//                    Toast.makeText(activity, R.string.upgrade_updating, Toast.LENGTH_SHORT).show();
//                }
            }
        });
    }

    public void upgrade() {
        upgrade.setTargetPath(StorageUtils.getCacheDir(activity)); //设置apk 的保存路径
        upgrade.setHttpManager(httpManager);

        if (AppUpgradeUtils.appIsDownloaded(upgrade)) {
            AppUpgradeUtils.installApp(activity, AppUpgradeUtils.getAppFile(upgrade));
        } else {
            DownloadService.bindService(activity.getApplicationContext(), new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    ((DownloadService.DownloadBinder) service).start(upgrade, null);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            });
        }

    }

    @NonNull
    private String getVersionName() {
        String versionName = AppUpgradeUtils.getVersionName(activity);
        if (versionName.endsWith("-debug")) {
            versionName = versionName.substring(0, versionName.lastIndexOf('-'));
        }
        return versionName;
    }

}
