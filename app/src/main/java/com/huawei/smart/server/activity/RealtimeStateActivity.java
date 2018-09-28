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
import com.huawei.smart.server.fragment.HealthStatusFragment;
import com.huawei.smart.server.fragment.TemperatureFragment;
import com.huawei.smart.server.redfish.model.Thermal;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 实时状态
 */
public class RealtimeStateActivity extends BaseActivity {
    @Nullable
    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    @Nullable
    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;

    private TemperatureFragment temperatureFragment;
    private HealthStatusFragment healthStatusFragment;

    private List<Fragment> mFragments = new ArrayList<>();
    private List<CharSequence> mTabs;
    private HWMFragmentAdapter mNetWorkSettingFragmentAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_state);
        this.initialize(R.string.ds_label_menu_realtime_state, true);

        initViewPage();
        initTabs();
        initFragmentAdapter();
    }

    private void initFragmentAdapter() {
        FragmentManager fm = getSupportFragmentManager();
        mNetWorkSettingFragmentAdapter = new HWMFragmentAdapter(fm, mFragments, mTabs);
        mViewPager.setAdapter(mNetWorkSettingFragmentAdapter);
    }

    private void initViewPage() {
        temperatureFragment = new TemperatureFragment();
        healthStatusFragment = new HealthStatusFragment();
        mFragments.add(temperatureFragment);
        mFragments.add(healthStatusFragment);

        mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(1);
    }

    private void initTabs() {
        mTabs = new ArrayList<>();
        mTabs.add(getResources().getString(R.string.rs_section_temperature));
        mTabs.add(getResources().getString(R.string.rs_section_health_state));
        mTabLayout.setupWithViewPager(mViewPager);
    }

    public void updateThermal(Thermal thermal) {
        temperatureFragment.updateThermal(thermal);
    }
}
