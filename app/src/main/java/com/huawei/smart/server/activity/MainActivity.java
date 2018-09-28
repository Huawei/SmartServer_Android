package com.huawei.smart.server.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;

import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.HWConstants;
import com.huawei.smart.server.R;
import com.huawei.smart.server.fragment.DeviceListFragment;
import com.huawei.smart.server.fragment.DiscoveryFragment;
import com.huawei.smart.server.fragment.ProfileFragment;
import com.huawei.smart.server.upgrade.UpgradeManager;
import com.huawei.smart.server.utils.AppUpgradeUtils;
import com.huawei.smart.server.widget.HeaderViewBase;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.huawei.smart.server.HWConstants.DEVICE_LIST_POSITION;
import static com.huawei.smart.server.HWConstants.POSITION;

/**
 * 应用入口，主界面
 */
public class MainActivity extends BaseActivity implements ViewPager.OnPageChangeListener, HeaderViewBase.HeaderButtons {

    public static final String TAG = "Main";
    @BindView(R.id.nav_bar)
    BottomNavigationView mBottomNavigation;

    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    private Unbinder mUnbinder;
    private List<Fragment> mFragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUnbinder = ButterKnife.bind(this);

        initBottomNavigation();
        initViewPage();
        initFragmentAdapter();

        final String checkUpgradeUrl = AppUpgradeUtils.getManifestString(this, HWConstants.KEY_UPGRADE_CHECK_URL);
        UpgradeManager.builder().activity(this).checkUrl(checkUpgradeUrl).silent(true).build().check();;
    }

    private void initFragmentAdapter() {
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }
        });
    }

    private void initBottomNavigation() {
        mBottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        // force Nav to change icon status from drawable-selector
        mBottomNavigation.setItemIconTintList(null);
    }

    private void initViewPage() {
        DeviceListFragment deviceListFragment = new DeviceListFragment();
        DiscoveryFragment discoveryFragment = new DiscoveryFragment();
        ProfileFragment myProfileFragment = new ProfileFragment();
        mFragments.add(deviceListFragment);
        mFragments.add(discoveryFragment);
        mFragments.add(myProfileFragment);
        mViewPager.addOnPageChangeListener(this);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            mViewPager.setCurrentItem(item.getOrder());
            return true;
        }

    };

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mBottomNavigation.getMenu().getItem(position).setChecked(true);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onResume() {
        Log.i(TAG, "resume main activity");
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "stop main activity");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "pause main activity");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "destory main activity");
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
        super.onDestroy();
    }

    @Override
    public void onLeftButtonClicked() {

    }

    @Override
    public void onRightButtonClicked() {
        Intent intent = new Intent(this, AddDeviceActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Intent intentValue = getIntent();
        mViewPager.setCurrentItem(intentValue.getIntExtra(POSITION, DEVICE_LIST_POSITION));
    }

    @Override
    public void onBackPressed() {
    }
}
