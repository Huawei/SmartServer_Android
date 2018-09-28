package com.huawei.smart.server.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.MenuSheetView;
import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.NetworkSettingActivity;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.constants.NetworkPortMode;
import com.huawei.smart.server.redfish.constants.NetworkPortType;
import com.huawei.smart.server.redfish.model.EthernetInterface;
import com.huawei.smart.server.utils.WidgetUtils;
import com.huawei.smart.server.widget.LabeledEditTextView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Response;


public class EthernetInterfaceFragment extends NetworkSettingActivity.BaseNetworkFragment {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(EthernetInterfaceFragment.class.getSimpleName());

    @BindView(R.id.bottom_sheet)
    BottomSheetLayout bottomSheetLayout;

    @BindView(R.id.ns_network_port_mode)
    LabeledEditTextView networkPortModeView;

    @BindView(R.id.network_port_radio_group)
    RadioGroup networkPortRadioGroup;

    @BindView(R.id.not_support)
    View notSupportView;

    @BindView(R.id.content)
    View contentView;


    NetworkPortMode networkPortMode;
    MenuSheetView mSelectEthernetMode;
    private List<RadioButton> networkPorts = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ethernet_interface, container, false);
        this.initialize(view);
        return view;
    }


    @Override
    public void initializeView(EthernetInterface ethernetInterface) {
        final EthernetInterface.Oem oem = ethernetInterface.getOem();
        if (oem != null) {
            // 初始化网口模式
            this.networkPortMode = oem.getNetworkPortMode();
            if (oem.getNetworkPortMode() != null) {
                networkPortModeView.setText(oem.getNetworkPortMode().getDisplayResId());
            }

            // 初始化网口列表
            networkPortRadioGroup.removeAllViews();
            RadioGroup.LayoutParams radioButtonLayoutParams = WidgetUtils.getRadioButtonLayoutParams(getResources());
            final List<EthernetInterface.NetworkPort> allowableNetworkPortValues = oem.getAllowableNetworkPortValues();
            final boolean support = allowableNetworkPortValues.size() != 0;
            this.notSupportView.setVisibility(support ? View.GONE : View.VISIBLE);
            this.contentView.setVisibility(support ? View.VISIBLE : View.GONE);
            if (support) {
                final EthernetInterface.NetworkPort active = oem.getManagementNetworkPort();
                for (int idx = 0; idx < allowableNetworkPortValues.size(); idx++) {
                    final EthernetInterface.NetworkPort networkPort = allowableNetworkPortValues.get(idx);
                    final RadioButton button = (RadioButton) getLayoutInflater().inflate(R.layout.widget_radio_button, null);
                    button.setId(idx);
                    final NetworkPortType networkPortType = networkPort.getType();
                    final String prefix = getResources().getString(networkPortType.getPrefixResId());
                    final String display = getResources().getString(networkPortType.getDisplayResId());
                    final int portNumber = NetworkPortType.Dedicated.equals(networkPort.getType())
                        || NetworkPortType.Aggregation.equals(networkPort.getType()) ? 2 : networkPort.getPortNumber();
                    button.setText(String.format(Locale.US, "%s%d (%s)", prefix, portNumber, display));
                    if (active.getType().equals(networkPortType)
                        && active.getPortNumber().equals(networkPort.getPortNumber()))
                    {
                        button.setChecked(true);    // 设置选中状态
                    }
                    networkPorts.add(button);
                    networkPortRadioGroup.addView(button, radioButtonLayoutParams);
                }
            }
        }
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        ((NetworkSettingActivity) activity).loadingEthernetInterface();
        finishRefreshing(true);
    }

    /**
     */
    @OnClick(R.id.submit)
    public void updateNetworkPort() {
        final EthernetInterface.Oem oem = new EthernetInterface.Oem();
        oem.setNetworkPortMode(this.networkPortMode);
        if (this.networkPortMode.equals(NetworkPortMode.Fixed)) {
            final int checkedRadioButtonId = networkPortRadioGroup.getCheckedRadioButtonId();
            final EthernetInterface.NetworkPort selected = ethernetInterface.getOem().getAllowableNetworkPortValues().get(checkedRadioButtonId);
            EthernetInterface.NetworkPort port = new EthernetInterface.NetworkPort();
            port.setType(selected.getType());
            port.setPortNumber(selected.getPortNumber());
            oem.setManagementNetworkPort(port);
        }

        final EthernetInterface ethernetInterface = new EthernetInterface();
        ethernetInterface.setOem(oem);

        LOG.info("Start update network port");
        new MaterialDialog.Builder(activity)
            .content(R.string.ns_port_prompt_update_port)
            .positiveText(R.string.button_sure)
            .negativeText(R.string.button_cancel)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    activity.showLoadingDialog();
                    activity.getRedfishClient().managers().updateEthernetInterface(
                        getEthernetInterface().getOdataId(),
                        ethernetInterface,
                        RRLB.<EthernetInterface>create(activity).callback(
                            new RedfishResponseListener.Callback<EthernetInterface>() {
                                @Override
                                public void onResponse(Response okHttpResponse, EthernetInterface ethernetInterface) {
                                    activity.showToast(R.string.msg_action_success, Toast.LENGTH_SHORT, Gravity.CENTER);
                                    setEthernetInterface(ethernetInterface);
                                    activity.dismissLoadingDialog();
                                    LOG.info("Update network port successfully");
                                }
                            }
                        ).build());
                }
            })
            .show();
    }

    /**
     *
     */
    @OnClick(R.id.ns_network_port_mode)
    void onPortModeSelectionClick() {
        if (mSelectEthernetMode == null) {
            mSelectEthernetMode =
                new MenuSheetView(getContext(), MenuSheetView.MenuType.LIST, R.string.ns_port_title_port_mode_selection,
                    new MenuSheetView.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            item.setChecked(true);
                            switch (item.getItemId()) {
                                case R.id.ethernet_mode_fix:
                                    networkPortMode = NetworkPortMode.Fixed;
                                    networkPortModeView.setText(NetworkPortMode.Fixed.getDisplayResId());
                                    enableNetworkPortRadioButtons(true);
                                    break;
                                case R.id.ethernet_mode_auto:
                                    networkPortMode = NetworkPortMode.Automatic;
                                    networkPortModeView.setText(NetworkPortMode.Automatic.getDisplayResId());
                                    enableNetworkPortRadioButtons(false);
                                    break;
                            }

                            if (bottomSheetLayout.isSheetShowing()) {
                                bottomSheetLayout.dismissSheet();
                            }
                            return true;
                        }
                    });
            mSelectEthernetMode.inflateMenu(R.menu.ethernet_interface_mode);
            mSelectEthernetMode.setListItemLayoutRes(R.layout.sheet_list_no_icon_item);
        }
        // show bottom sheet
        bottomSheetLayout.showWithSheetView(mSelectEthernetMode);
    }

    public void enableNetworkPortRadioButtons(boolean enable) {
        for (RadioButton rb : networkPorts) {
            rb.setEnabled(enable);
        }
    }

}
