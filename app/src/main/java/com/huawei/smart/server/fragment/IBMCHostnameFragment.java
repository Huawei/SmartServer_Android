package com.huawei.smart.server.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.NetworkSettingActivity;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishClient;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.model.EthernetInterface;
import com.huawei.smart.server.widget.LabeledEditTextView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Response;


public class IBMCHostnameFragment extends NetworkSettingActivity.BaseNetworkFragment {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(IBMCHostnameFragment.class.getSimpleName());

    static Pattern HOSTNAME_REGEX = Pattern.compile("^(?!-)(?!.*?-$)[a-zA-Z0-9-]{1,64}$");

    @BindView(R.id.ibmc_hostname)
    LabeledEditTextView hostnameView;

    @BindView(R.id.submit)
    Button submitButton;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ibmc_hostname, container, false);
        initialize(view);
        return view;
    }

    public void initializeView(EthernetInterface ethernetInterface) {
        hostnameView.setText(ethernetInterface.getHostName());
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        ((NetworkSettingActivity) activity).loadingEthernetInterface();
        finishRefreshing(true);
    }

    @OnClick(R.id.submit)
    public void updateHostname() {
        final String hostname = this.hostnameView.getText().toString();
        final Matcher matcher = HOSTNAME_REGEX.matcher(hostname);
        if (!matcher.matches()) {
            activity.showToast(R.string.ns_network_msg_illegal_hostname, Toast.LENGTH_SHORT, Gravity.CENTER);
        } else {
            LOG.info("Start update ibmc host name");
            final RedfishClient redfishClient = activity.getRedfishClient();
            EthernetInterface updated = new EthernetInterface();
            updated.setHostName(hostname);

            activity.showLoadingDialog();
            redfishClient.managers().updateEthernetInterface(
                getEthernetInterface().getOdataId(),
                updated,
                RRLB.<EthernetInterface>create(activity).callback(
                    new RedfishResponseListener.Callback<EthernetInterface>() {
                        @Override
                        public void onResponse(Response okHttpResponse, EthernetInterface ethernetInterface) {
                            activity.showToast(R.string.msg_action_success, Toast.LENGTH_SHORT, Gravity.CENTER);
                            setEthernetInterface(ethernetInterface);
                            activity.dismissLoadingDialog();
                            LOG.info("Update ibmc host name successfully");
                        }
                    }
                ).build());
        }
    }
}
