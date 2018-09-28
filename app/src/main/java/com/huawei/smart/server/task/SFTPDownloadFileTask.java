package com.huawei.smart.server.task;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.FileUtils;
import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.CollectionActivity;
import com.huawei.smart.server.model.Device;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;


public class SFTPDownloadFileTask extends AsyncTask<Void, Void, Boolean> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SFTPDownloadFileTask.class.getSimpleName());

    public static final String TAG = "SFTP";

    private final MaterialDialog loadingDialog;
    private final Device device;
    private final Integer sshPort;

    private final String remote;
    private final String local;
    private final BaseActivity activity;

    public SFTPDownloadFileTask(BaseActivity activity, Device device, Integer sshPort,
                                String local, String remote, MaterialDialog loadingDialog) {
        this.activity = activity;
        this.device = device;
        this.sshPort = sshPort;
        this.local = local;
        this.remote = remote;
        this.loadingDialog = loadingDialog;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        JSch ssh = new JSch();
        JSch.setLogger(new JSCHLogger());
        Session session = null;
        Channel channel = null;
        try {
            session = ssh.getSession(device.getUsername(), device.getHostname(), sshPort);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setPassword(device.getPassword());
            session.setTimeout(30*1000);

            session.connect();
            channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftp = (ChannelSftp) channel;
            // use the put method , if you are using android remember to remove "file://" and use only the relative path
            final File localFile = new File(local);
            if (!localFile.exists()) {
                FileUtils.createOrExistsDir(localFile.getParentFile());
                localFile.createNewFile();
            }
            sftp.get(remote, new FileOutputStream(localFile));
            return true;
        } catch (Exception e) {
            LOG.warn("Failed to download file using sftp", e);
        } finally {
            if (session != null) {
                session.disconnect();
            }
            if (channel != null) {
                channel.disconnect();
            }
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }

        if (success) {
            final String downloadTo = activity.getString(R.string.collection_msg_download_to);
            new MaterialDialog.Builder(activity)
                .content(downloadTo + activity.getString(R.string.default_path) + "/" + new File(local).getName())
                .positiveText(R.string.button_done)
                .negativeText(R.string.button_share)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        CollectionActivity.share(activity, local, "application/gzip");
                    }
                })
                .show();
        } else {
            new MaterialDialog.Builder(activity)
                .content(R.string.collection_msg_download_failed)
                .positiveText(R.string.button_done)
                .show();
        }
    }

    private class JSCHLogger implements com.jcraft.jsch.Logger {

        public JSCHLogger() {
        }

        @Override
        public boolean isEnabled(int pLevel) {
            return true;
        }

        @Override
        public void log(int pLevel, String pMessage) {
            Log.i(TAG, pMessage);
        }
    }
}