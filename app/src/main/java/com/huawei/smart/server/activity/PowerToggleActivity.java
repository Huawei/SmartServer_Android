package com.huawei.smart.server.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.R;
import com.huawei.smart.server.adapter.HWMFragmentAdapter;
import com.huawei.smart.server.fragment.PowerOffTimeoutFragment;
import com.huawei.smart.server.fragment.PowerResetFragment;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.model.ComputerSystem;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Response;

/**
 * 电源开关
 */
public class PowerToggleActivity extends BaseActivity {

    @Nullable
    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    @Nullable
    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;

    private List<Fragment> mFragments = new ArrayList<>();
    private List<CharSequence> mTabs;
    private HWMFragmentAdapter mNetWorkSettingFragmentAdapter;

    PowerResetFragment powerResetFragment;
    PowerOffTimeoutFragment powerOffTimeoutFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_toggle);
        this.initialize(R.string.ds_label_menu_power, true);

        initViewPage();
        initTabs();
        initFragmentAdapter();
    }

    private void initViewPage() {
        powerResetFragment = new PowerResetFragment();
        powerOffTimeoutFragment = new PowerOffTimeoutFragment();
        mFragments.add(powerResetFragment);
        mFragments.add(powerOffTimeoutFragment);
        mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(1);
    }

    private void initTabs() {
        mTabs = new ArrayList<>();
        mTabs.add(getResources().getString(R.string.ds_label_menu_power));
        mTabs.add(getResources().getString(R.string.pt_label_safe_power_off_time));
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void initFragmentAdapter() {
        FragmentManager fm = getSupportFragmentManager();
        mNetWorkSettingFragmentAdapter = new HWMFragmentAdapter(fm, mFragments, mTabs);
        mViewPager.setAdapter(mNetWorkSettingFragmentAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLoadingDialog();
        onRefresh(null);
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        getRedfishClient().systems().get(RRLB.<ComputerSystem>create(this)
            .callback(new RedfishResponseListener.Callback<ComputerSystem>() {
                public void onResponse(Response okHttpResponse, final ComputerSystem computer) {
                    powerResetFragment.updateView(computer);
                    powerOffTimeoutFragment.updateView(computer);
                    finishLoadingViewData(true);
                }
            }).build());
    }

}
