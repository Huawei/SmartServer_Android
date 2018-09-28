package com.huawei.smart.server.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.utils.ParseUtil;
import com.blankj.utilcode.util.RegexUtils;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.MenuSheetView;
import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.NetworkSettingActivity;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.constants.DNSAddressOrigin;
import com.huawei.smart.server.redfish.constants.IPVersion;
import com.huawei.smart.server.redfish.constants.IPv4AddressOrigin;
import com.huawei.smart.server.redfish.constants.IPv6AddressOrigin;
import com.huawei.smart.server.redfish.model.EthernetInterface;
import com.huawei.smart.server.utils.IPAddressUtil;
import com.huawei.smart.server.utils.StringUtils;
import com.huawei.smart.server.validator.DomainValidator;
import com.huawei.smart.server.validator.InetAddressValidator;
import com.huawei.smart.server.validator.ValidationException;
import com.huawei.smart.server.widget.LabeledEditTextView;
import com.huawei.smart.server.widget.LabeledSwitch;
import com.huawei.smart.server.widget.SimpleMenuSheetView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.suke.widget.SwitchButton;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Response;


public class NetworkFragment extends NetworkSettingActivity.BaseNetworkFragment {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NetworkFragment.class.getSimpleName());

    @BindView(R.id.bottom_sheet) BottomSheetLayout bottomSheetLayout;

    @BindView(R.id.ns_network_ip_version) LabeledEditTextView ipVersionView;
    @BindView(R.id.ns_network_ipv4_segment) View ipv4Segment;
    @BindView(R.id.ns_network_ipv6_segment) View ipv6Segment;

    @BindView(R.id.ns_network_ipv4_addr_origin_dhcp_switch) LabeledSwitch ipv4AddressOriginDHCPSwitch;  // 自动获取ipv4
    @BindView(R.id.ns_network_ipv4_address) LabeledEditTextView ipv4AddressView;
    @BindView(R.id.ns_network_ipv4_mask) LabeledEditTextView ipv4MaskView;
    @BindView(R.id.ns_network_ipv4_gateway) LabeledEditTextView ipv4GatewayView;
    @BindView(R.id.ns_network_ipv4_mac) LabeledEditTextView ipv4MACView;

    @BindView(R.id.ns_network_ipv6_addr_origin_dhcp_switch) LabeledSwitch ipv6AddressOriginDHCPSwitch;
    @BindView(R.id.ns_network_ipv6_address) LabeledEditTextView ipv6AddressView;
    @BindView(R.id.ns_network_ipv6_gateway) LabeledEditTextView ipv6GatewayView;
    @BindView(R.id.ns_network_ipv6_prefix_len) LabeledEditTextView ipv6PrefixLengthView;
    @BindView(R.id.ns_network_ipv6_link_local_address) LabeledEditTextView ipv6LinkLocalAddressView;

    @BindView(R.id.dns_address_origin) LabeledEditTextView DNSAddressOriginView;
    @BindView(R.id.network_primary_dns) LabeledEditTextView primaryDNSView;
    @BindView(R.id.network_domain) LabeledEditTextView domain;
    @BindView(R.id.network_alternative_dns) LabeledEditTextView alternativeDNSView;

    IPVersion selectedIPVersion;
    DNSAddressOrigin selectedDNSAddressOrigin;

    MenuSheetView mSelectIPType;
    SimpleMenuSheetView<SimpleMenuSheetView.HashSource> mSelectDNSAddressOrigin;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_network, container, false);
        this.initialize(view);
        return view;
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        ((NetworkSettingActivity) activity).loadingEthernetInterface();
        finishRefreshing(true);
    }

    @Override
    public void initializeView(EthernetInterface ethernetInterface) {
        selectedDNSAddressOrigin = ethernetInterface.getOem().getDNSAddressOrigin();

        // 设置开关事件
        initializeSwitchEvent();

        // 初始化界面数据
        // IPv4
        ipv4MACView.setText(ethernetInterface.getPermanentMACAddress());
        final EthernetInterface.IPv4Address ipv4 = ethernetInterface.getIPv4();
        if (ipv4 != null) {
            ipv4AddressView.setText(ipv4.getAddress());
            ipv4MaskView.setText(ipv4.getSubnetMask());
            ipv4GatewayView.setText(ipv4.getGateway());
            final boolean ipv4DHCPChecked = ipv4.getAddressOrigin().equals(IPv4AddressOrigin.DHCP);
            ipv4AddressOriginDHCPSwitch.setChecked(ipv4DHCPChecked);
            updateIpv4DHCPStatus(ipv4DHCPChecked);
        }

        // IPv6
        ipv6GatewayView.setText(ethernetInterface.getIPv6DefaultGateway());
        final EthernetInterface.IPv6Address ipv6 = ethernetInterface.getIPv6();
        if (ipv6 != null) {
            ipv6AddressView.setText(ipv6.getAddress());
            ipv6PrefixLengthView.setText(ipv6.getPrefixLength() == null ? "" : ipv6.getPrefixLength() + "");
            final boolean ipv6DHCPChecked = ipv6.getAddressOrigin().equals(IPv6AddressOrigin.DHCPv6);
            ipv6AddressOriginDHCPSwitch.setChecked(ipv6DHCPChecked);
            updateIpv6DHCPStatus(ipv6DHCPChecked);
        }

        final EthernetInterface.IPv6Address ipv6LinkLocal = ethernetInterface.getIPv6LinkLocal();
        if (ipv6LinkLocal != null) {
            ipv6LinkLocalAddressView.setText(ipv6LinkLocal.getAddress());
        }

        switchDNSAddressOrigin(ethernetInterface.getOem().getDNSAddressOrigin());
        final List<String> nameServers = ethernetInterface.getNameServers();
        primaryDNSView.setText(nameServers != null && nameServers.size() >= 1 ? nameServers.get(0) : null);
        alternativeDNSView.setText(nameServers != null && nameServers.size() == 2 ? nameServers.get(1) : null);

        if (!TextUtils.isEmpty(ethernetInterface.getFQDN())) {
            if (!TextUtils.isEmpty(ethernetInterface.getHostName())) {
                domain.setText(ethernetInterface.getFQDN().replaceFirst(ethernetInterface.getHostName() + ".", ""));
            } else {
                domain.setText(ethernetInterface.getFQDN());
            }
        }

        // 设置IP使能，切换界面
        onIPVersionChanged(ethernetInterface.getOem().getIPVersion());
    }

    private void initializeSwitchEvent() {
        ipv4AddressOriginDHCPSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                updateIpv4DHCPStatus(isChecked);
//                ipv4AddressView.enableInputEditText(!isChecked);
//                ipv4MaskView.enableInputEditText(!isChecked);
//                ipv4GatewayView.enableInputEditText(!isChecked);
            }
        });
        ipv6AddressOriginDHCPSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                updateIpv6DHCPStatus(isChecked);
//                ipv6AddressView.enableInputEditText(!isChecked);
//                ipv6PrefixLengthView.enableInputEditText(!isChecked);
//                ipv6GatewayView.enableInputEditText(!isChecked);
            }
        });
    }

    private void updateIpv6DHCPStatus(boolean isChecked) {
        ipv6AddressOriginDHCPSwitch.showDivider(!isChecked);
        ipv6AddressView.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        ipv6PrefixLengthView.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        ipv6GatewayView.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        ipv6LinkLocalAddressView.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        updateDNSAddressOriginDataSource();
    }

    private void updateIpv4DHCPStatus(boolean isChecked) {
        ipv4AddressOriginDHCPSwitch.showDivider(!isChecked);
        ipv4AddressView.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        ipv4MaskView.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        ipv4GatewayView.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        ipv4MACView.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        ipv6LinkLocalAddressView.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        updateDNSAddressOriginDataSource();
    }

    private void onIPVersionChanged(IPVersion ipVersion) {
        selectedIPVersion = ipVersion;
        ipVersionView.setText(ipVersion.getDisplayResId());
        ipv4Segment.setVisibility(ipVersion.equals(IPVersion.IPv6) ? View.GONE : View.VISIBLE);
        ipv6Segment.setVisibility(ipVersion.equals(IPVersion.IPv4) ? View.GONE : View.VISIBLE);

        updateDNSAddressOriginDataSource();
    }

    @OnClick(R.id.ns_network_ip_version)
    void onSelectIPVersion() {
        bottomSheetLayout.showWithSheetView(getSelectIPVersionMenuSheet());
    }

    private MenuSheetView getSelectIPVersionMenuSheet() {
        if (mSelectIPType == null) {
            mSelectIPType =
                new MenuSheetView(getContext(), MenuSheetView.MenuType.LIST, R.string.ns_network_ip_version_selection_title,
                    new MenuSheetView.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            item.setChecked(true);
                            switch (item.getItemId()) {
                                case R.id.network_ip_type_ipv4:
                                    onIPVersionChanged(IPVersion.IPv4);
                                    break;
                                case R.id.network_ip_type_ipv6:
                                    onIPVersionChanged(IPVersion.IPv6);
                                    break;
                                default:
                                    onIPVersionChanged(IPVersion.IPv4AndIPv6);
                                    break;
                            }
                            if (bottomSheetLayout.isSheetShowing()) {
                                bottomSheetLayout.dismissSheet();
                            }
                            return true;
                        }
                    });
            mSelectIPType.inflateMenu(R.menu.network_ip_type);
            mSelectIPType.setListItemLayoutRes(R.layout.sheet_list_no_icon_item);
        }

        return mSelectIPType;
    }

    /**
     * 更新 DNS 获取方式可选列表
     */
    private void updateDNSAddressOriginDataSource() {
        final Map<DNSAddressOrigin, String> DNSAddressOriginMapper = DNSAddressOrigin.toMap(this.getContext());
        if (!ipv4AddressOriginDHCPSwitch.isChecked() || IPVersion.IPv6.equals(selectedIPVersion)) {
            DNSAddressOriginMapper.remove(DNSAddressOrigin.IPv4);
        }
        if (!ipv6AddressOriginDHCPSwitch.isChecked() || IPVersion.IPv4.equals(selectedIPVersion)) {
            DNSAddressOriginMapper.remove(DNSAddressOrigin.IPv6);
        }

        getSelectDNSAddressOriginMenuSheet().updateDataSource(SimpleMenuSheetView.HashSource.convert(DNSAddressOriginMapper));

        if (!DNSAddressOriginMapper.containsKey(selectedDNSAddressOrigin)) {
            switchDNSAddressOrigin(DNSAddressOrigin.Static);
        }
    }

    /**
     * 切换 DNS 获取方式
     *
     * @param dnsAddressOrigin
     */
    private void switchDNSAddressOrigin(DNSAddressOrigin dnsAddressOrigin) {
        selectedDNSAddressOrigin = dnsAddressOrigin;
        DNSAddressOriginView.setText(dnsAddressOrigin.getDisplayResId());   // 设置DNS获取方式

        // 切换 View 状态
        final int visibility = dnsAddressOrigin.equals(DNSAddressOrigin.Static) ? View.VISIBLE : View.GONE;
        primaryDNSView.setVisibility(visibility);
        alternativeDNSView.setVisibility(visibility);
    }

    @OnClick(R.id.dns_address_origin)
    public void onSelectDNSAddressOrigin() {
        bottomSheetLayout.showWithSheetView(getSelectDNSAddressOriginMenuSheet());
    }

    private SimpleMenuSheetView getSelectDNSAddressOriginMenuSheet() {
        if (mSelectDNSAddressOrigin == null) {
            mSelectDNSAddressOrigin = new SimpleMenuSheetView<>(
                getContext(), R.string.ns_network_dns_addr_origin_selection_title);
            final Map<DNSAddressOrigin, String> DNSAddressOriginMapper = DNSAddressOrigin.toMap(this.getContext());
            mSelectDNSAddressOrigin.updateDataSource(SimpleMenuSheetView.HashSource.convert(DNSAddressOriginMapper));
            mSelectDNSAddressOrigin.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    final SimpleMenuSheetView.HashSource<DNSAddressOrigin, String> source =
                        (SimpleMenuSheetView.HashSource) adapterView.getItemAtPosition(position);
                    switchDNSAddressOrigin(source.get().getKey());
                    if (bottomSheetLayout.isSheetShowing()) {
                        bottomSheetLayout.dismissSheet();
                    }
                }
            });
        }
        return mSelectDNSAddressOrigin;
    }


    @OnClick(R.id.submit_ip_version)
    public void onUpdateIPVersion() {
        try {
            final EthernetInterface EI = new EthernetInterface();
            EI.setOem(new EthernetInterface.Oem());
            if (selectedIPVersion == null) {
                throw new ValidationException(ipVersionView, getString(R.string.ns_network_error_ip_version_required));
            }
            EI.getOem().setIPVersion(selectedIPVersion);
            onUpdateNetwork(R.string.ns_network_prompt_update_ip_version, EI);
        } catch (ValidationException e) {
            e.getSource().requestFocus();
            activity.showToast(e.getLocalizedMessage(), Toast.LENGTH_SHORT, Gravity.CENTER);
        }
    }

    @OnClick(R.id.submit_ipv4)
    public void onUpdateIPV4() {
        try {
            final EthernetInterface EI = new EthernetInterface();
            getIPv4Settings(EI);
            onUpdateNetwork(R.string.ns_network_prompt_update_ipv4_setting, EI);
        } catch (ValidationException e) {
            e.getSource().requestFocus();
            activity.showToast(e.getLocalizedMessage(), Toast.LENGTH_SHORT, Gravity.CENTER);
        }
    }

    @OnClick(R.id.submit_ipv6)
    public void onUpdateIPV6() {
        try {
            final EthernetInterface EI = new EthernetInterface();
            getIPv6Settings(EI);
            onUpdateNetwork(R.string.ns_network_prompt_update_ipv6_setting, EI);
        } catch (ValidationException e) {
            e.getSource().requestFocus();
            activity.showToast(e.getLocalizedMessage(), Toast.LENGTH_SHORT, Gravity.CENTER);
        }
    }

    @OnClick(R.id.submit_dns)
    public void onUpdateDNS() {
        try {
            final EthernetInterface EI = new EthernetInterface();
            EI.setOem(new EthernetInterface.Oem());
            getDNSSettings(EI);
            onUpdateNetwork(R.string.ns_network_prompt_update_dns_setting, EI);
        } catch (ValidationException e) {
            e.getSource().requestFocus();
            activity.showToast(e.getLocalizedMessage(), Toast.LENGTH_SHORT, Gravity.CENTER);
        }
    }

    public void onUpdateNetwork(int prompt, final EthernetInterface ethernetInterfaceSettings) {
        final String json = ParseUtil.getParserFactory().getString(ethernetInterfaceSettings);
        LOG.info("Start update ethernet interface, network setting to be submit is: " + json);
        new MaterialDialog.Builder(activity)
            .content(prompt)
            .positiveText(R.string.button_submit)
            .negativeText(R.string.button_cancel)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    activity.showLoadingDialog();
                    activity.getRedfishClient().managers().updateEthernetInterface(
                        getEthernetInterface().getOdataId(),
                        ethernetInterfaceSettings,
                        RRLB.<EthernetInterface>create(activity).callback(
                            new RedfishResponseListener.Callback<EthernetInterface>() {
                                @Override
                                public void onResponse(Response okHttpResponse, EthernetInterface ethernetInterface) {
                                    activity.showToast(R.string.msg_action_success, Toast.LENGTH_SHORT, Gravity.CENTER);
                                    setEthernetInterface(ethernetInterface);
                                    activity.dismissLoadingDialog();
                                    LOG.info("Update ethernet interface successfully");
                                }
                            }
                        ).build());
                }
            })
            .show();
    }

    private EthernetInterface getEthernetInterfaceSettings() throws ValidationException {
        final EthernetInterface EI = new EthernetInterface();
        EI.setOem(new EthernetInterface.Oem());

        if (selectedIPVersion == null) {
            throw new ValidationException(ipVersionView, getString(R.string.ns_network_error_ip_version_required));
        }
        EI.getOem().setIPVersion(selectedIPVersion);

        getIPv4Settings(EI);
        getIPv6Settings(EI);
        getDNSSettings(EI);

        final String json = ParseUtil.getParserFactory().getString(EI);
        LOG.info("Ethernet interface data to submit is: {}", json);
        return EI;
    }


    private void getDNSSettings(EthernetInterface ei) throws ValidationException {
        ei.getOem().setDNSAddressOrigin(selectedDNSAddressOrigin);

        final String domain = this.domain.getText().toString();
        if (!TextUtils.isEmpty(domain) && (!(DomainValidator.getInstance().isValid(domain) || InetAddressValidator.getInstance().isValid(domain)))) {
            throw new ValidationException(this.domain, getString(R.string.ns_network_error_domain_illegal));
        }
        ei.setFQDN(domain);

        if (DNSAddressOrigin.Static.equals(selectedDNSAddressOrigin)) {
            final String primaryDNS = primaryDNSView.getText().toString();
            final String alternativeDNS = alternativeDNSView.getText().toString();

//            if (StringUtils.isEmpty(primaryDNS)) {
//                throw new ValidationException(primaryDNSView, getString(R.string.ns_network_error_primary_dns_required));
//            } else
            if (!StringUtils.isEmpty(primaryDNS) && !IPAddressUtil.isIPv4LiteralAddress(primaryDNS)
                    && !IPAddressUtil.isIPv6LiteralAddress(primaryDNS)) {
                throw new ValidationException(primaryDNSView, getString(R.string.ns_network_error_primary_dns_illegal));
            }

//            if (StringUtils.isEmpty(alternativeDNS)) {
//                throw new ValidationException(alternativeDNSView, getString(R.string.ns_network_error_alternative_dns_required));
//            } else
            if (!StringUtils.isEmpty(alternativeDNS) && !IPAddressUtil.isIPv4LiteralAddress(alternativeDNS) && !IPAddressUtil.isIPv6LiteralAddress(alternativeDNS)) {
                throw new ValidationException(alternativeDNSView, getString(R.string.ns_network_error_alternative_dns_illegal));
            }

            ei.setNameServers(new ArrayList<String>());
            ei.getNameServers().add(primaryDNS);
            ei.getNameServers().add(alternativeDNS);
        }
    }

    /**
     * 验证IPv4用户输入
     *
     * @param EI
     * @throws ValidationException
     */
    private void getIPv4Settings(EthernetInterface EI) throws ValidationException {
        if (!selectedIPVersion.equals(IPVersion.IPv6)) {
            // IPV4 required
            EthernetInterface.IPv4Address ipv4 = new EthernetInterface.IPv4Address();
            if (ipv4AddressOriginDHCPSwitch.isChecked()) { // 自动获取模式
                ipv4.setAddressOrigin(IPv4AddressOrigin.DHCP);
            } else {  // 手动设置模式
                ipv4.setAddressOrigin(IPv4AddressOrigin.Static);
                final String addr = ipv4AddressView.getText().toString();
                final String mask = ipv4MaskView.getText().toString();
                final String gateway = ipv4GatewayView.getText().toString();

                if (StringUtils.isEmpty(addr)) {
                    throw new ValidationException(ipv4AddressView, getString(R.string.ns_network_error_ipv4_addr_required));
                } else if (!RegexUtils.isIP(addr)) {
                    throw new ValidationException(ipv4AddressView, getString(R.string.ns_network_error_ipv4_addr_illegal));
                }
                ipv4.setAddress(addr);

                if (StringUtils.isEmpty(mask)) {
                    throw new ValidationException(ipv4MaskView, getString(R.string.ns_network_error_ipv4_mask_required));
                } else if (!RegexUtils.isIP(mask)) {
                    throw new ValidationException(ipv4MaskView, getString(R.string.ns_network_error_ipv4_mask_illegal));
                }
                ipv4.setSubnetMask(mask);


                if (StringUtils.isEmpty(gateway)) {
                    throw new ValidationException(ipv4GatewayView, getString(R.string.ns_network_error_ipv4_gateway_required));
                } else if (!RegexUtils.isIP(gateway)) {
                    throw new ValidationException(ipv4GatewayView, getString(R.string.ns_network_error_ipv4_gateway_illegal));
                }
                ipv4.setGateway(gateway);
            }
            // 添加 IPV4 数据
            EI.setIPv4Addresses(new ArrayList<EthernetInterface.IPv4Address>());
            EI.getIPv4Addresses().add(ipv4);
        }
    }

    private void getIPv6Settings(EthernetInterface EI) throws ValidationException {
        if (!selectedIPVersion.equals(IPVersion.IPv4)) {
            // IPV4 required
            EthernetInterface.IPv6Address ipv6 = new EthernetInterface.IPv6Address();
            if (ipv6AddressOriginDHCPSwitch.isChecked()) { // 自动获取模式
                ipv6.setAddressOrigin(IPv6AddressOrigin.DHCPv6);
            } else {  // 手动设置模式
                ipv6.setAddressOrigin(IPv6AddressOrigin.Static);

                final String addr = ipv6AddressView.getText().toString();
                final String prefix = ipv6PrefixLengthView.getText().toString();
                final String gateway = ipv6GatewayView.getText().toString();

                // IPv6 gateway
                if (TextUtils.isEmpty(gateway)) {
                    throw new ValidationException(ipv6GatewayView, getString(R.string.ns_network_error_ipv6_gateway_required));
                } else if (!IPAddressUtil.isIPv6LiteralAddress(gateway)) {
                    throw new ValidationException(ipv6GatewayView, getString(R.string.ns_network_error_ipv6_gateway_illegal));
                }
                EI.setIPv6DefaultGateway(gateway);


                // IPV6 addr
                EthernetInterface.IPv6StaticAddress ipv6StaticAddr = new EthernetInterface.IPv6StaticAddress();
                if (StringUtils.isEmpty(addr)) {
                    throw new ValidationException(ipv6AddressView, getString(R.string.ns_network_error_ipv6_addr_required));
                } else if (!IPAddressUtil.isIPv6LiteralAddress(addr)) {
                    throw new ValidationException(ipv6AddressView, getString(R.string.ns_network_error_ipv6_addr_illegal));
                }
                ipv6StaticAddr.setAddress(addr);

                if (TextUtils.isEmpty(prefix)) {
                    throw new ValidationException(ipv6PrefixLengthView, getString(R.string.ns_network_error_ipv6_prefix_required));
                } else if (!TextUtils.isDigitsOnly(prefix)
                    || (Integer.parseInt(prefix) < 0 || Integer.parseInt(prefix) > 128))
                {
                    throw new ValidationException(ipv6PrefixLengthView, getString(R.string.ns_network_error_ipv6_prefix_illegal));
                }
                ipv6StaticAddr.setPrefixLength(Integer.parseInt(prefix));

                EI.setIPv6StaticAddresses(new ArrayList<EthernetInterface.IPv6StaticAddress>());
                EI.getIPv6StaticAddresses().add(ipv6StaticAddr);
            }

            // 添加 IPV6 数据
            EI.setIPv6Addresses(new ArrayList<EthernetInterface.IPv6Address>());
            EI.getIPv6Addresses().add(ipv6);
        }
    }

}
