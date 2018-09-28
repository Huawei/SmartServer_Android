package com.huawei.smart.server.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.BaseFragment;
import com.huawei.smart.server.R;
import com.huawei.smart.server.adapter.HWMFragmentAdapter;
import com.huawei.smart.server.fragment.EthernetInterfaceFragment;
import com.huawei.smart.server.fragment.IBMCHostnameFragment;
import com.huawei.smart.server.fragment.NetworkFragment;
import com.huawei.smart.server.fragment.TimeZoneFragment;
import com.huawei.smart.server.fragment.VLANFragment;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.model.EthernetInterface;
import com.huawei.smart.server.redfish.model.EthernetInterfaceCollection;
import com.huawei.smart.server.redfish.model.ResourceId;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Response;

/**
 * 网络设置
 */
public class NetworkSettingActivity extends BaseActivity {

    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;

    EthernetInterface ethernetInterface;
    private List<BaseNetworkFragment> mFragments = new ArrayList<>();
    private List<CharSequence> mTabs = new ArrayList<>();
    private TimeZoneFragment timeZoneFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_setting);
        this.initialize(R.string.ns_title, true);
        initializeView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLoadingDialog();
        loadingEthernetInterface();
    }

    @Override
    protected boolean fitsSystemWindows() {
        return true;
    }

    /**
     * 加载网络数据，同时更新各个子页面的数据
     */
    public void loadingEthernetInterface() {
        getRedfishClient().managers().getEthernetInterfaces(RRLB.<EthernetInterfaceCollection>create(this).callback(
            new RedfishResponseListener.Callback<EthernetInterfaceCollection>() {
                @Override
                public void onResponse(Response okHttpResponse, EthernetInterfaceCollection collection) {
                    ResourceId resourceOdataId = collection.getMembers().get(0); // 获取对应的网口资源链接
                    getRedfishClient().managers().getEthernetInterface(
                        resourceOdataId.getOdataId(),
                        RRLB.<EthernetInterface>create(NetworkSettingActivity.this).callback(
                            new RedfishResponseListener.Callback<EthernetInterface>() {
                                @Override
                                public void onResponse(Response okHttpResponse, EthernetInterface ethernetInterface) {
                                    setEthernetInterface(ethernetInterface);
                                    finishLoadingViewData(true);
                                }
                            }).build());
                }
            }).build());
    }

    private void initializeView() {
        // init tabs
        mTabs.add(getResources().getString(R.string.ns_tab_hostname));
        mTabs.add(getResources().getString(R.string.ns_tab_port));
        mTabs.add(getResources().getString(R.string.ns_tab_network));
        mTabs.add(getResources().getString(R.string.ns_tab_vlan));
        mTabs.add(getResources().getString(R.string.ns_tab_time_zone));

        // init fragments
        IBMCHostnameFragment deviceNameFragment = new IBMCHostnameFragment();
        EthernetInterfaceFragment portFragment = new EthernetInterfaceFragment();
        NetworkFragment networkFragment = new NetworkFragment();
        VLANFragment vlanFragment = new VLANFragment();
        timeZoneFragment = new TimeZoneFragment();

        mFragments.add(deviceNameFragment);
        mFragments.add(portFragment);
        mFragments.add(networkFragment);
        mFragments.add(vlanFragment);
        mFragments.add(timeZoneFragment);

        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new HWMFragmentAdapter(fm, mFragments, mTabs));
        mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(4);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    public void setEthernetInterface(EthernetInterface ethernetInterface) {
        if (ethernetInterface != null) {
            this.ethernetInterface = ethernetInterface;
            for (BaseNetworkFragment fragment : mFragments) {
                fragment.setEthernetInterface(ethernetInterface);
            }
        }
    }

    public static abstract class BaseNetworkFragment extends BaseFragment {

        protected EthernetInterface ethernetInterface;

        public EthernetInterface getEthernetInterface() {
            return ethernetInterface;
        }

        public void setEthernetInterface(EthernetInterface ethernetInterface) {
            try {
                this.ethernetInterface = ethernetInterface;
                this.initializeView(this.ethernetInterface);
            } catch (Exception e) {

            }
        }

        public abstract void initializeView(EthernetInterface ethernetInterface);
    }

}
