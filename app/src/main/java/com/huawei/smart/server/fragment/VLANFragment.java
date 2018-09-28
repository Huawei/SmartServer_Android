package com.huawei.smart.server.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.NetworkSettingActivity;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.model.EthernetInterface;
import com.huawei.smart.server.widget.LabeledEditTextView;
import com.huawei.smart.server.widget.LabeledSwitch;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.suke.widget.SwitchButton;

import org.slf4j.LoggerFactory;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Response;

import static android.view.View.VISIBLE;


public class VLANFragment extends NetworkSettingActivity.BaseNetworkFragment {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(EthernetInterfaceFragment.class.getSimpleName());

    @BindView(R.id.vlan_id) LabeledEditTextView vlanIdView;
    @BindView(R.id.vlan_switch) LabeledSwitch vlanSwitchView;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vlan, container, false);
        this.initialize(view);
        return view;
    }

    @Override
    public void initializeView(EthernetInterface ethernetInterface) {
        // 设置开关
        vlanSwitchView.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                vlanIdView.setVisibility(isChecked ? VISIBLE : View.GONE);
                vlanSwitchView.showDivider(isChecked);
            }
        });

        // 初始化界面
        final EthernetInterface.VLAN vlan = ethernetInterface.getVLAN();
        vlanSwitchView.setChecked(vlan.getEnabled());
        vlanIdView.setText(vlan.getId() > 0 ? vlan.getId() + "" : null);
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        ((NetworkSettingActivity) activity).loadingEthernetInterface();
        finishRefreshing(true);
    }


    @OnClick(R.id.submit)
    public void updateVLANSetting() {
        final NetworkSettingActivity baseActivity = (NetworkSettingActivity) this.activity;
        final String vlanId = this.vlanIdView.getText().toString();
        final boolean vlanEnabled = vlanSwitchView.isChecked();
        if (vlanEnabled && (TextUtils.isEmpty(vlanId) || Integer.parseInt(vlanId) < 1 || Integer.parseInt(vlanId) > 4094)) {
            baseActivity.showToast(R.string.ns_vlan_illegal_vlan_id, Toast.LENGTH_SHORT, Gravity.CENTER);
        } else {
            // 设置数据
            EthernetInterface.VLAN vlan = new EthernetInterface.VLAN();
            vlan.setEnabled(vlanEnabled);
            vlan.setId(vlanEnabled ? Integer.parseInt(vlanId) : null);
            EthernetInterface updated = new EthernetInterface();
            updated.setVLAN(vlan);

            baseActivity.showLoadingDialog();
            LOG.info("Start update VLAN settings");
            baseActivity.getRedfishClient().managers().updateEthernetInterface(
                getEthernetInterface().getOdataId(),
                updated,
                RRLB.<EthernetInterface>create(baseActivity).callback(
                    new RedfishResponseListener.Callback<EthernetInterface>() {
                        @Override
                        public void onResponse(Response okHttpResponse, EthernetInterface ethernetInterface) {
                            baseActivity.setEthernetInterface(ethernetInterface);
                            baseActivity.dismissLoadingDialog();
                            baseActivity.showToast(R.string.msg_action_success, Toast.LENGTH_SHORT, Gravity.CENTER);
                            LOG.info("Update VLAN settings successfully");
                        }
                    }
                ).build());
        }
    }

}
