package com.huawei.smart.server.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.SpannableStringBuilder;
import android.view.ViewGroup;

import com.blankj.utilcode.util.SpanUtils;
import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.BaseFragment;
import com.huawei.smart.server.R;
import com.huawei.smart.server.adapter.HWMFragmentAdapter;
import com.huawei.smart.server.fragment.MemoryFragment;
import com.huawei.smart.server.fragment.NetworkAdapterFragment;
import com.huawei.smart.server.fragment.PowerFragment;
import com.huawei.smart.server.fragment.ProcessorFragment;
import com.huawei.smart.server.fragment.StorageFragment;
import com.huawei.smart.server.widget.BadgeIndicatorView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 硬件信息
 */
public class HardwareActivity extends BaseActivity {

    @Nullable
    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    @Nullable
    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;

    private List<BaseFragment> mFragments = new ArrayList<>();
    private List<CharSequence> mTabs;
    private HWMFragmentAdapter mNetWorkSettingFragmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hardware);
        this.initialize(R.string.ds_label_menu_hardware, true);

        initViewPage();
        initTabs();
        initFragmentAdapter();
    }

    public void setBadgeToTab(int position, int badgeValue) {
        BadgeIndicatorView badge = new BadgeIndicatorView(this);
        badge.setTargetView(((ViewGroup) mTabLayout.getChildAt(0)).getChildAt(position));
        badge.setBadgeCount(badgeValue);
        badge.setTextColor(ContextCompat.getColor(this, R.color.colorComment));
        badge.setBackgroundColor(ContextCompat.getColor(this, R.color.transparentColor));
        badge.setBadgeMargin(5);
    }

    private void initFragmentAdapter() {
        FragmentManager fm = getSupportFragmentManager();
        mNetWorkSettingFragmentAdapter = new HWMFragmentAdapter(fm, mFragments, mTabs);
        mViewPager.setAdapter(mNetWorkSettingFragmentAdapter);
    }

    private void initViewPage() {
        MemoryFragment memoryFragment = new MemoryFragment();
        ProcessorFragment cpuFragment = new ProcessorFragment();
        PowerFragment powerFragment = new PowerFragment();
        NetworkAdapterFragment netCardFragment = new NetworkAdapterFragment();
        StorageFragment storageFragment = new StorageFragment();
        mFragments.add(memoryFragment);
        mFragments.add(cpuFragment);
        mFragments.add(powerFragment);
        mFragments.add(netCardFragment);
        mFragments.add(storageFragment);

        mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(4);
    }

    private void initTabs() {
        mTabs = new ArrayList<>();
        mTabs.add(getSpannableString(R.string.hardware_tab_memory, 0));
        mTabs.add(getSpannableString(R.string.hardware_tab_cpu, 0));
        mTabs.add(getSpannableString(R.string.hardware_tab_power, 0));
        mTabs.add(getSpannableString(R.string.hardware_tab_netcard, 0));
        mTabs.add(getSpannableString(R.string.hardware_tab_storage, 0));
        mTabLayout.setupWithViewPager(mViewPager);
    }

    public void updateTabTitle(int pos, int tabTitleRes, int count) {
        if (count > 0) {
            final SpannableStringBuilder spannableString = getSpannableString(tabTitleRes, count);
            mTabLayout.getTabAt(pos).setText(spannableString);
        }
    }

    private SpannableStringBuilder getSpannableString(int tabTitleRes, int count) {
        return new SpanUtils()
                    .append(getResources().getString(tabTitleRes))
//                    .setSubscript()
                    .appendSpace(16)
                    .append(String.valueOf(count)).setSuperscript().setFontSize(11, true)
                    .create();
    }

}
