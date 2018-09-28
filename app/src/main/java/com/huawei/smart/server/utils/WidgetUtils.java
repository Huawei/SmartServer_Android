package com.huawei.smart.server.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioGroup;

import com.huawei.smart.server.R;
import com.huawei.smart.server.redfish.constants.ResourceState;
import com.huawei.smart.server.widget.LabeledTextView;

public class WidgetUtils {

    public static DividerItemDecoration newDividerItemDecoration(Context context) {
        return newDividerItemDecoration(context, ContextCompat.getDrawable(context, R.drawable.bg_form_list_item_divider));
    }

    public static DividerItemDecoration newDividerItemDecoration(Context context, Drawable drawable) {
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(drawable);
        return dividerItemDecoration;
    }

    public static RadioGroup.LayoutParams getRadioButtonLayoutParams(Resources resources) {
        int WC = RadioGroup.LayoutParams.WRAP_CONTENT;
        int MP = RadioGroup.LayoutParams.MATCH_PARENT;
        RadioGroup.LayoutParams rParams = new RadioGroup.LayoutParams(MP, WC);
        rParams.setMarginStart(resources.getDimensionPixelSize(R.dimen.form_list_item_with_radio_ms));
        return rParams;
    }

    public static void updateLabeledTextStateView(LabeledTextView view, ResourceState state) {
        view.setText(state.getLabelResId());
        view.setDrawableStart(state.getIconResId());
    }

    public static void setStatusBarColor(Activity activity, int statusBarColor) {
        Window window = activity.getWindow();
        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        window.setStatusBarColor(statusBarColor);
        ViewGroup mContentView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            //注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View . 预留出系统 View 的空间.
            mChildView.setFitsSystemWindows(true);
        }
    }

}
