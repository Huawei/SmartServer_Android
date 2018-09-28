package com.huawei.smart.server.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.R;
import com.huawei.smart.server.adapter.HWMFragmentAdapter;
import com.huawei.smart.server.fragment.BootModeFragment;
import com.huawei.smart.server.fragment.BootOverrideFragment;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.model.ComputerSystem;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Response;

/**
 * 启动设置
 */
public class BootSettingActivity extends BaseActivity {

    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;

    private HWMFragmentAdapter mFragmentAdapter;
    private BootOverrideFragment bootOverrideFragment;
    private BootModeFragment bootModeFragment;
    private List<Fragment> mFragments = new ArrayList<>();
    private List<CharSequence> mTabs;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boot_setting);
        this.initialize(R.string.ds_label_menu_boot_setting, true);
        initializeView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLoadingDialog();
        onRefresh(null);
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        this.getRedfishClient().systems().get(RRLB.<ComputerSystem>create(this).callback(
            new RedfishResponseListener.Callback<ComputerSystem>() {
                public void onResponse(Response okHttpResponse, ComputerSystem system) {
                    bootOverrideFragment.setComputerSystem(system);
                    bootModeFragment.setComputerSystem(system);
                    finishLoadingViewData(true);
                }
            }).build());
    }

    private void initializeView() {
        mTabs = new ArrayList<>();
        mTabs.add(getResources().getString(R.string.bs_tab_boot_override));
        mTabs.add(getResources().getString(R.string.bs_tab_boot_mode));

        // setup fragments
        this.bootOverrideFragment = new BootOverrideFragment();
        mFragments.add(bootOverrideFragment);

        this.bootModeFragment = new BootModeFragment();
        mFragments.add(bootModeFragment);

        // setup view pager adapter
        mFragmentAdapter = new HWMFragmentAdapter(getSupportFragmentManager(), mFragments, mTabs);
        mViewPager.setAdapter(mFragmentAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(4);

        // bind view-pager to tab
        mTabLayout.setupWithViewPager(mViewPager);
    }

}
