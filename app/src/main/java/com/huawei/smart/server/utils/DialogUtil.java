package com.huawei.smart.server.utils;


import android.app.Dialog;
import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;

import com.huawei.smart.server.R;


public class DialogUtil {

    public static void showHintDialog(final Context context, @StringRes int titleRes, @StringRes int messageRes) {
        buildDialog(context, titleRes, messageRes)
                .setPositiveButton(android.R.string.ok, null).show();
    }

    public static void showInstallBrowserDialog(final Context context) {
        showInstallBrowserDialog(context, R.string.no_browser_app_found, R.string.please_install_a_browser_app);
    }

    public static void showInstallBrowserDialog(final Context context, @StringRes int titleRes, @StringRes int messageRes) {
        showHintDialog(context, titleRes, messageRes);
    }


    public static AlertDialog.Builder buildDialog(final Context context, @StringRes int titleRes, @StringRes int messageRes) {
        return new AlertDialog.Builder(context)
                .setTitle(titleRes)
                .setMessage(messageRes);
    }

    public static AlertDialog.Builder buildDialog(final Context context, String title, String message) {
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message);
    }

    public static AlertDialog.Builder buildDialog(final Context context, @StringRes int titleRes, @StringRes int messageRes, Dialog.OnDismissListener onDismissListener) {
        return new AlertDialog.Builder(context)
                .setTitle(titleRes)
                .setMessage(messageRes)
                .setOnDismissListener(onDismissListener);
    }
}
