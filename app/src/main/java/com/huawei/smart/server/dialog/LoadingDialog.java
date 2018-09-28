package com.huawei.smart.server.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.huawei.smart.server.R;

/**
 * Created by DuoQi on 2018-02-23.
 */
public class LoadingDialog extends Dialog {

    public LoadingDialog(@NonNull Context context) {
        super(context);
    }

    public LoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected LoadingDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public static LoadingDialog build(Context context, @Nullable CharSequence message,
                                      Boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        LoadingDialog dialog = new LoadingDialog(context, R.style.LoadingDialog);
        dialog.setContentView(R.layout.loading_dialog);

//        SpinKitView icon = dialog.findViewById(R.id.loading_icon);
        TextView m = dialog.findViewById(R.id.loading_text);
        if (!TextUtils.isEmpty(message)) {
            m.setText(message);
            m.setVisibility(View.VISIBLE);
        } else {
            m.setVisibility(View.GONE);
        }

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(cancelListener);


//        dialog.getWindow().setAttributes(WindowManager.LayoutParams);
//        dialog.window.attributes.gravity = Gravity.CENTER
//        val lp = dialog.window.attributes
//        lp.dimAmount = 0.2f
//        dialog.window.attributes = lp
        return dialog;
    }

}