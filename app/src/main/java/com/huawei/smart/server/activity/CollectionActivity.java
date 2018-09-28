package com.huawei.smart.server.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;
import com.blankj.utilcode.util.ActivityUtils;
import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.BuildConfig;
import com.huawei.smart.server.R;
import com.huawei.smart.server.lock.LockManager;
import com.huawei.smart.server.model.Device;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishClient;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.constants.TaskState;
import com.huawei.smart.server.redfish.model.NetworkProtocol;
import com.huawei.smart.server.redfish.model.Task;
import com.huawei.smart.server.redfish.model.TaskServiceClient;
import com.huawei.smart.server.task.SFTPDownloadFileTask;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Response;

public class CollectionActivity extends BaseActivity implements FolderChooserDialog.FolderCallback {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CollectionActivity.class.getSimpleName());

    @BindView(R.id.collect_download_path) View downloadToPathView;

    private File downloadToFolder;
    private int SELECT_FOLDER_REQUEST_CODE = 1;
    private int SAVE_COLLECT_FILE_REQUEST_CODE = 2;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        this.initialize(R.string.ds_label_menu_collector, true);
        this.initializeDeviceFromBundle();
        this.initializeView();
    }

    private void initializeView() {
        downloadToFolder = new File(getDownloadToFolderPath());
//        downloadToPathView.setText(downloadToFolder.getAbsolutePath() + File.separator + BuildConfig.APPLICATION_ID);
    }

    public static String getDownloadToFolderPath() {
        final String downloadFolderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        return downloadFolderPath + File.separator + BuildConfig.APPLICATION_ID;
    }

    private void collectAndSaveToFile() {
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        final String filename = formatter.format(new Date()) + ".tar.gz";

        final MaterialDialog dialog = new MaterialDialog.Builder(CollectionActivity.this)
            .content(R.string.collection_msg_collecting)
            .cancelable(false)
            .canceledOnTouchOutside(false)
            .progress(true, 0)
            .progressNumberFormat("%1d/%2d")
            .show();


        // 提交收集任务
        getRedfishClient().managers().collect(filename, RRLB.<Task>create(this).callback(
            new RedfishResponseListener.Callback<Task>() {
                @Override
                public void onResponse(Response okHttpResponse, Task task) {
                    new RedfishTaskMonitorTask(task.getOdataId(), filename, getRedfishClient(), dialog)
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

                @Override
                public void onError(ANError anError) {
                    dialog.dismiss();
                    super.onError(anError);
                }
            }).build());

    }

    @OnClick(R.id.collect_download_path)
    protected void showDownloadFiles() {
        ActivityUtils.startActivity(CollectFileListActivity.class);
    }

    @OnClick(R.id.collect)
    public void onCollect() {
        final boolean hasPermission = requestExternalStoragePermission(SAVE_COLLECT_FILE_REQUEST_CODE);
        if (hasPermission) {
            new MaterialDialog.Builder(CollectionActivity.this)
                .content(R.string.collection_msg_long_time_task)
                .positiveText(R.string.button_submit)
                .negativeText(R.string.button_cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        collectAndSaveToFile();
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                })
                .show();
        }
    }

    @Deprecated
//    @OnClick(R.id.folder_selection)
    protected void onFolderSelectionClick() {
        final boolean hasPermission = requestExternalStoragePermission(SELECT_FOLDER_REQUEST_CODE);
        if (hasPermission) {
            FolderSelectionDialog();
        }
    }

    private void FolderSelectionDialog() {
        new FolderChooserDialog.Builder(this)
            .chooseButton(R.string.md_choose_label)  // changes label of the choose button
            .initialPath(downloadToFolder.getAbsolutePath())  // changes initial path, defaults to external storage directory
            .tag("optional-identifier")
            .goUpLabel(getString(R.string.md_up_label))
            .allowNewFolder(true, R.string.md_new_folder)
            .show(this);
    }

    @Override
    public void onFolderSelection(@NonNull FolderChooserDialog dialog, @NonNull File folder) {
        this.downloadToFolder = folder;
//        downloadToPathView.setText(folder.getPath().toString());
    }

    @Override
    public void onFolderChooserDismissed(@NonNull FolderChooserDialog dialog) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == SELECT_FOLDER_REQUEST_CODE) {
                FolderSelectionDialog();
            } else if (requestCode == SAVE_COLLECT_FILE_REQUEST_CODE) {
                collectAndSaveToFile();
            }
        }
    }

    public boolean requestExternalStoragePermission(int requestCode) {
        int osVersion = Integer.valueOf(android.os.Build.VERSION.SDK);
        if (osVersion > 22) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    requestCode);
                return false;
            }
        }

        return true;
    }

    public static void share(Context context, String filePath, String type) {
        try {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType(type);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.ds_label_menu_collector));
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Uri attachmentUri = FileProvider.getUriForFile(context, context.getPackageName(), new File(filePath));
            sendIntent.putExtra(Intent.EXTRA_STREAM, attachmentUri);

            ActivityUtils.startActivity(Intent.createChooser(sendIntent, context.getResources().getString(R.string.display_report_share_title)));
        } catch (java.lang.Throwable ex) {
            LOG.error("Failed to share file", ex.getMessage());
        }
    }

    public class RedfishTaskMonitorTask extends AsyncTask<Void, Void, Void> {

        private final String taskOdataId;
        private final String filename;
        MaterialDialog dialog;
        RedfishClient redfish;
        Boolean completed = false;

        public RedfishTaskMonitorTask(String taskOdataId, String filename, RedfishClient redfish, MaterialDialog dialog) {
            this.filename = filename;
            this.taskOdataId = taskOdataId;
            this.redfish = redfish;
            this.dialog = dialog;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final TaskServiceClient taskService = redfish.taskService();
            while (!completed) {
                final ANResponse<Task> response = taskService.syncGet(this.taskOdataId);
                if (response.isSuccess()) {
                    final Task task = response.getResult();
                    if (TaskState.Running.equals(task.getTaskState())) { // 执行中
                        final String percentage = task.getOem().getTaskPercentage();
                        if (!TextUtils.isEmpty(percentage)) { // 更新进度
                            dialog.setProgress(Integer.parseInt(percentage.replace("%", "")));
                        }
                        LockManager.getInstance().getAppLock().updateLastActiveOn();
                    } else if (TaskState.Completed.equals(task.getTaskState())) {
                        completed = true;
                    }
                } else { // 出现异常
                    LOG.error("Collection task failed, response {}", response.getResult());
                    break;
                }

                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {

                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (completed) {
                dialog.setContent(R.string.collection_msg_downloading);
                final Device device = CollectionActivity.this.getDevice();
                final String local = downloadToFolder.getAbsolutePath() + File.separator + filename;

                getRedfishClient().managers().getNetworkProtocol(RRLB.<NetworkProtocol>create(CollectionActivity.this).callback(
                    new RedfishResponseListener.Callback<NetworkProtocol>() {
                        @Override
                        public void onResponse(Response okHttpResponse, NetworkProtocol networkProtocol) {
                            final NetworkProtocol.Protocol ssh = networkProtocol.getSSH();
                            final SFTPDownloadFileTask download = new SFTPDownloadFileTask(CollectionActivity.this,
                                device, ssh.getPort(), local, filename, dialog);
                            download.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }

                        public void onError(ANError anError) {
                            dialog.dismiss();
                            super.onError(anError);
                        }
                    }
                ).build());
            } else {
                dialog.dismiss();
                showToast(R.string.collection_msg_collect_failed, Toast.LENGTH_SHORT, Gravity.CENTER);
            }

            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
}
