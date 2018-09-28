package com.huawei.smart.server.task;

import android.os.AsyncTask;

import com.huawei.smart.server.dialog.LoadingDialog;

import java.util.concurrent.CountDownLatch;

public class DismissLoadingDialogTask extends AsyncTask<Void, Void, Void> {

    private final CountDownLatch latch;
    private final LoadingDialog loadingDialog;

    public DismissLoadingDialogTask(CountDownLatch latch, LoadingDialog loadingDialog) {
        this.latch = latch;
        this.loadingDialog = loadingDialog;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            latch.await();
        } catch (InterruptedException e) {

        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}