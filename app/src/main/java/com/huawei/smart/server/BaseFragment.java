package com.huawei.smart.server;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.huawei.smart.server.dialog.LoadingDialog;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.Realm;

/**
 * Created by DuoQi on 2018-02-23.
 */
public class BaseFragment extends Fragment {

    protected Realm realm;    // Realm 数据库管理对象
    protected BaseActivity activity;
    protected Unbinder viewUnbinder = null;
    protected LoadingDialog loadingDialog = null;     // 处理中模态进度框
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
    @BindView(R.id.title_bar_go_back)
    View goBackView;

    /**
     * butter-knife view binding
     *
     * @param source
     */
    private void bindView(View source) {
        viewUnbinder = ButterKnife.bind(this, source);
    }


    /**
     * initialize activity
     */
    protected void initialize(View source) {
        this.initialize(source, 0, false);
    }

    /**
     * initialize activity when include common header layout
     *
     * @param titleTxtResId 标题栏文字资源ID
     * @param backEnabled   是否显示返回按钮
     */
    protected void initialize(View source, int titleTxtResId, boolean backEnabled) {
        this.bindView(source);
        if (titleTxt != null && titleTxtResId > 0) {
            titleTxt.setText(titleTxtResId);
        }
        if (goBackView != null) {
            goBackView.setVisibility(backEnabled ? View.VISIBLE : View.INVISIBLE);
        }
        this.activity = (BaseActivity) this.getActivity();

        if (refreshLayout != null) {
            Objects.requireNonNull(refreshLayout).setEnableLoadMore(false); // 关闭上拉加载更多
            Objects.requireNonNull(refreshLayout).setEnableAutoLoadMore(false);
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    BaseFragment.this.onRefresh(refreshLayout);
                }
            });
        }
    }

    public void onResume() {
        super.onResume();
        if (this.titleBar != null) {
            // 由于不是替换整套theme，只修改title bar的颜色，所以这边直接修改背景色。
            final String theme = HWConstants.getTheme(getBaseActivity());
            titleBar.setBackground(getResources().getDrawable(theme.equals(HWConstants.THEME_GREEN) ? R.color.colorAccent : R.color.colorPrimary));
        }
    }

    public void finishLoadingViewData(boolean result) {
        this.finishRefreshing(result);
        activity.dismissLoadingDialog();
    }

    public void finishRefreshing(boolean result) {
        if (refreshLayout != null && refreshLayout.getState() == RefreshState.Refreshing
            && !refreshLayout.getState().isFinishing)
        {
            refreshLayout.finishRefresh(result);
        }
    }

    public boolean isUIActive() {
        return isAdded() && !isDetached() && !isRemoving();
    }

    /**
     * 假如fragment支持下拉刷新，需要实现该方法，并实现刷新逻
     *
     * @param refreshLayout
     * @return
     */
    public void onRefresh(RefreshLayout refreshLayout) {

    }

    /**
     * 生成一个默认的 loading-dialog - 无法取消，没有文字信息。
     *
     * @return
     */
    public LoadingDialog getDefaultLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog.build(getContext(), null, false, null);
        }
        return loadingDialog;
    }

    public BaseActivity getBaseActivity() {
        return this.activity;
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (realm != null) {
            realm.close();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // unbind ButterKnife
        if (viewUnbinder != null) {
            viewUnbinder.unbind();
        }
    }

}