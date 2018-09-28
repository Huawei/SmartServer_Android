package com.huawei.smart.server;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.huawei.smart.server.dialog.LoadingDialog;
import com.huawei.smart.server.lock.AppLock;
import com.huawei.smart.server.lock.LockManager;
import com.huawei.smart.server.lock.PageListener;
import com.huawei.smart.server.model.Device;
import com.huawei.smart.server.redfish.RedfishClient;
import com.huawei.smart.server.utils.Compatibility;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import butterknife.Unbinder;
import io.realm.Realm;

import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_DEVICE_ID;
import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_DEVICE_PASSWORD;

/**
 * Created by DuoQi on 2018-02-23.
 */
public class BaseActivity extends AppCompatActivity {

    private static PageListener pageListener;
    protected Device device;              // device from bundle
    protected RedfishClient redfish;      // redfish client
    protected LoadingDialog loadingDialog = null;     // 处理中模态进度框
    protected Toast toast;
    @Nullable
    @BindView(R.id.refreshLayout)
    protected RefreshLayout refreshLayout;
    @Nullable
    @BindView(R.id.title_bar)
    View titleBar;
    @Nullable
    @BindView(R.id.title_bar_title_txt)
    TextView titleTxt;
    @Nullable
    @BindView(R.id.title_bar_subtitle_txt)
    TextView subTitleTxt;
    @Nullable
    @BindView(R.id.title_bar_go_back)
    View goBackView;
    private Realm realm;    // Realm 数据库管理对象
    private Unbinder viewUnbinder = null;

    public static void setListener(PageListener listener) {
        pageListener = listener;
    }

    /**
     * butter-knife view binding
     */
    protected void bindView() {
        viewUnbinder = ButterKnife.bind(this);
    }


    @Override
    public void onUserInteraction() {
        final AppLock locker = LockManager.getInstance().getAppLock();
        if (locker != null) {
            final boolean shouldLock = locker.shouldLockScreen(this);
            if (shouldLock) {
                if (pageListener != null) {
                    pageListener.onActivityResumed(this);
                }
            } else {
                locker.updateLastActiveOn();
                super.onUserInteraction();
            }
        } else {
            super.onUserInteraction();
        }
    }

    @Override
    protected void attachBaseContext(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(HWConstants.PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
        final String lang = preferences.getString(HWConstants.PREFERENCE_SETTING_LANG, Compatibility.getLocale().getLanguage());
        Locale newLocale = lang.equals(HWConstants.LANG_ZH) ? Locale.CHINESE : Locale.ENGLISH;
        Context wrapped = AppContextWrapper.wrap(context, newLocale);
        super.attachBaseContext(wrapped);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (pageListener != null) {
            pageListener.onActivityCreated(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (pageListener != null) {
            pageListener.onActivityStarted(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pageListener != null) {
            pageListener.onActivityResumed(this);
        }
        setupThemeColor();
    }

    private void setupThemeColor() {
        final String theme = HWConstants.getTheme(this);
        final int color = theme.equals(HWConstants.THEME_GREEN) ? R.color.colorAccent : R.color.colorPrimary;
        // 由于不是替换整套theme，只修改title bar的颜色，所以这边直接修改背景色。
        if (this.titleBar != null) {
            titleBar.setBackground(getResources().getDrawable(color));
        }
        BarUtils.setStatusBarColor(this, getResources().getColor(color), 0);

        if (!fitsSystemWindows()) {
            ViewGroup mContentView = (ViewGroup) this.findViewById(Window.ID_ANDROID_CONTENT);
            View root = mContentView.getChildAt(0);
            BarUtils.addMarginTopEqualStatusBarHeight(root);
        }
    }

    protected boolean fitsSystemWindows() {
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pageListener != null) {
            pageListener.onActivityPaused(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (toast != null) {
            toast.cancel();
        }
        if (pageListener != null) {
            pageListener.onActivityStopped(this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (pageListener != null) {
            pageListener.onActivitySaveInstanceState(this);
        }
    }

    /**
     * initialize activity
     */
    protected void initialize() {
        this.initialize("", "", false);
    }

    /**
     * initialize activity when include common header layout
     *
     * @param titleTxt    标题栏文字资源
     * @param backEnabled 是否显示返回按钮
     */
    protected void initialize(String titleTxt, boolean backEnabled) {
        this.initialize(titleTxt, null, backEnabled);
    }

    /**
     * initialize activity when include common header layout
     *
     * @param titleTxtResId 标题栏文字资源ID
     * @param backEnabled   是否显示返回按钮
     */
    protected void initialize(int titleTxtResId, boolean backEnabled) {
        this.initialize(getString(titleTxtResId), null, backEnabled);
    }

    /**
     * initialize activity when include common header layout
     *
     * @param titleTxtResId 标题栏文字资源ID
     * @param backEnabled   是否显示返回按钮
     */
    protected void initialize(int titleTxtResId, int subTitleResId, boolean backEnabled) {
        this.initialize(getString(titleTxtResId), getString(subTitleResId), backEnabled);
    }

    /**
     * initialize activity when include common header layout
     *
     * @param title       标题栏标题文字
     * @param subTitle    标题栏副标题文字
     * @param backEnabled 是否显示返回按钮
     */
    protected void initialize(String title, String subTitle, boolean backEnabled) {
        this.bindView();
        if (titleTxt != null) {
            this.titleTxt.setText(title);
        }

        if (this.subTitleTxt != null) {
            Objects.requireNonNull(this.subTitleTxt).setText(subTitle);
            Objects.requireNonNull(this.subTitleTxt).setVisibility(TextUtils.isEmpty(subTitle) ? View.GONE : View.VISIBLE);
        }

        if (this.goBackView != null) {
            Objects.requireNonNull(goBackView).setVisibility(backEnabled ? View.VISIBLE : View.INVISIBLE);
        }

        if (refreshLayout != null) {
            Objects.requireNonNull(refreshLayout).setEnableLoadMore(false); // 关闭上拉加载更多
            Objects.requireNonNull(refreshLayout).setEnableAutoLoadMore(false);
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    BaseActivity.this.onRefresh(refreshLayout);
                }
            });
        }
    }

    /**
     * 初始化对应的 Device 对象.（从Bundle里获取DeviceId）
     */
    protected void initializeDeviceFromBundle() {
        if (this.device == null) {
            String deviceId = getExtraString(BUNDLE_KEY_DEVICE_ID);
            final Device managedDeviceInstance = getDefaultRealmInstance().where(Device.class)
                .equalTo("id", deviceId).findFirst();
            this.device = getDefaultRealmInstance().copyFromRealm(Objects.requireNonNull(managedDeviceInstance));
            if (!this.device.getRememberPwd()) {
                device.setPassword(getExtraString(BUNDLE_KEY_DEVICE_PASSWORD));
            }
        }
    }

    /**
     * 初始化Redfish客户端，认证信息是从RealmDB里加载的。（Bundle里获取的DeviceId）
     * 同时还会初始化对应的 Device 对象
     */
    public void getOrCreateRedfishClient() {
        initializeDeviceFromBundle();
        this.redfish = RedfishClientManager.getInstance().getOrCreate(device);
    }

    /**
     * 根据Activity中传递的DeviceId加载对应的RedfishClient
     */
    public RedfishClient getRedfishClient() {
        String deviceId = getExtraString(BUNDLE_KEY_DEVICE_ID);
        this.redfish = RedfishClientManager.getInstance().get(deviceId);
        return this.redfish;
    }

    /**
     * return back to last view click event handler
     */
    @Optional
    @OnClick(R.id.title_bar_go_back)
    public void goBack() {
        KeyboardUtils.hideSoftInput(this);
        ActivityUtils.finishActivity(this, true);
    }

    public void finishLoadingViewData(boolean result) {
        this.finishRefreshing(result);
        this.dismissLoadingDialog();
    }

    /**
     * 假如activity支持下拉刷新，需要实现该方法，并实现刷新逻
     *
     * @param refreshLayout
     * @return
     */
    public void onRefresh(RefreshLayout refreshLayout) {
    }

    public void finishRefreshing(boolean result) {
        if (refreshLayout != null && refreshLayout.getState() == RefreshState.Refreshing
            && !refreshLayout.getState().isFinishing)
        {
            refreshLayout.finishRefresh(result);
        }
    }

    /**
     * 生成一个默认的 loading-dialog - 无法取消，没有文字信息。
     *
     * @return
     */
    public LoadingDialog getDefaultLoadingDialog(Context context) {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog.build(context, null, false, null);
        }
        return loadingDialog;
    }

    public LoadingDialog showLoadingDialog() {
        if (loadingDialog == null || !loadingDialog.isShowing()) {
            getDefaultLoadingDialog(this).show();
        }
        return loadingDialog;
    }

    public void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    public void dismissLoadingDialog(DialogInterface.OnDismissListener dismissListener) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.setOnDismissListener(dismissListener);
            loadingDialog.dismiss();
        }
    }

    public Toast showToast(int txtResId, int duration, int gravity) {
        if (toast != null) {
            toast.setText(txtResId);
//            toast.cancel();
        } else {
            toast = Toast.makeText(this, txtResId, duration);
        }
        toast.setGravity(gravity, 0, 0);
        toast.show();
        return toast;
    }

    public Toast showToast(String text, int duration, int gravity) {
        if (toast != null) {
            toast.setText(text);
//            toast.cancel();
        } else {
            toast = Toast.makeText(this, text, duration);
        }
        toast.setGravity(gravity, 0, 0);
        toast.show();
        return toast;
    }

    public void setTitleTxt(int resid) {
        Objects.requireNonNull(titleTxt).setText(getResources().getText(resid));
    }

    public void setVisibilityOfLeftButton(int visibility) {
        Objects.requireNonNull(goBackView).setVisibility(visibility);
    }

    /**
     * 获取 Realm DB 管理对象
     *
     * @return
     */
    public Realm getDefaultRealmInstance() {
        if (realm == null) {
            realm = Realm.getDefaultInstance();
        }
        return realm;
    }

    public Device getDevice() {
        return device;
    }


    /**
     * get bundle value
     *
     * @param key
     * @return
     */
    @Nullable
    public String getExtraString(String key) {
        final Bundle extras = getIntent().getExtras();
        return extras != null ? extras.getString(key) : null;
    }

    public String getDeviceId() {
        return getExtraString(BUNDLE_KEY_DEVICE_ID);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (pageListener != null) {
            pageListener.onActivityDestroyed(this);
        }

        // release Realm DB
        if (realm != null) {
            realm.close();
        }

        // unbind ButterKnife
        if (viewUnbinder != null) {
            viewUnbinder.unbind();
        }
    }
}