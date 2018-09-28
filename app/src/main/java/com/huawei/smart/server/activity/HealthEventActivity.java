package com.huawei.smart.server.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import com.androidnetworking.error.ANError;
import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.R;
import com.huawei.smart.server.adapter.HWMFragmentAdapter;
import com.huawei.smart.server.fragment.HealthEventFragment;
import com.huawei.smart.server.model.HealthEvent;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.constants.Severity;
import com.huawei.smart.server.redfish.model.LogService;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import io.realm.Realm;
import okhttp3.Response;

/**
 * 健康事件
 */
public class HealthEventActivity extends BaseActivity {

    @BindView(R.id.filter_tabs) TabLayout filterTabs;
    @BindView(R.id.viewpager) ViewPager mViewPager;

    private List<CharSequence> mTabs;
    private List<HealthEventFragment> mFragments = new ArrayList<>();
    private HWMFragmentAdapter mNetWorkSettingFragmentAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_event);
        this.initialize(R.string.ds_label_menu_health, true);
        initializeView();
    }

    private void initializeView() {
        initializeDeviceFromBundle();   // 初始化 device 对象
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
        mFragments.add(HealthEventFragment.build(null));
        mFragments.add(HealthEventFragment.build(Severity.Critical));
        mFragments.add(HealthEventFragment.build(Severity.Warning));
        mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(2);
    }

    private void initTabs() {
        mTabs = new ArrayList<>();
        mTabs.add(getResources().getString(R.string.he_filter_all));
        mTabs.add(getResources().getString(R.string.he_filter_critical));
        mTabs.add(getResources().getString(R.string.he_filter_warning));
        filterTabs.setupWithViewPager(mViewPager);
    }


    @Override
    protected void onResume() {
        super.onResume();
        showLoadingDialog();
        onRefresh(null);
    }

    public void onRefresh(final RefreshLayout refreshLayout) {
        getRedfishClient().systems().getLogService(RRLB.<LogService>create(this).callback(
            new RedfishResponseListener.Callback<LogService>() {
                public void onResponse(Response okHttpResponse, LogService logService) {
                    final List<LogService.HealthEvent> events = logService.getOem().getHealthEvents();
                    // persist to db
                    final List<HealthEvent> dbEvents = new ArrayList<>();
                    for (LogService.HealthEvent healthEvent : events) {
                        HealthEvent e = new HealthEvent(healthEvent);
                        e.setDeviceId(device.getId());
                        e.setId(UUID.randomUUID().toString());
                        dbEvents.add(e);
                    }
                    getDefaultRealmInstance().executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            // remove all exists data
                            realm.where(HealthEvent.class).equalTo("deviceId", device.getId())
                                .findAll().deleteAllFromRealm();
                            realm.copyToRealm(dbEvents);    // save to db
                        }
                    });

                    updateHealthEventLists();
                    finishLoadingViewData(true);
                    if (refreshLayout != null && refreshLayout.getState() == RefreshState.Refreshing
                        && !refreshLayout.getState().isFinishing) {
                        refreshLayout.finishRefresh(true);
                    }
                }

                public void onError(ANError anError) {
                    super.onError(anError);
                    updateHealthEventLists();
                    finishLoadingViewData(false);
                    if (refreshLayout != null && refreshLayout.getState() == RefreshState.Refreshing
                        && !refreshLayout.getState().isFinishing) {
                        refreshLayout.finishRefresh(false);
                    }
                }
            }).build());
    }

    private void updateHealthEventLists() {
        for (HealthEventFragment fragment : mFragments) {
            fragment.update();
        }
    }

}
