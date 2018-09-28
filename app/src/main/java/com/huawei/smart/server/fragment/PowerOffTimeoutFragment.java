package com.huawei.smart.server.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.huawei.smart.server.BaseFragment;
import com.huawei.smart.server.R;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.model.ComputerSystem;
import com.huawei.smart.server.widget.LabeledEditTextView;

import org.slf4j.LoggerFactory;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Response;


/**
 * 下电时限
 */
public class PowerOffTimeoutFragment extends BaseFragment {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(PowerResetFragment.class.getSimpleName());

    @BindView(R.id.power_off_timeout)
    LabeledEditTextView timeoutView;
//    @BindView(R.id.power_off_timeout_switch)
//    LabeledSwitch timeoutSwitchView;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_power_off_timeout, container, false);
        initialize(view);
        return view;
    }

    public void updateView(ComputerSystem computerSystem) {
        if (computerSystem.getOem() != null) {
            timeoutView.setText(computerSystem.getOem().getSafePowerOffTimoutSeconds() + "");
        }

//        vlanSwitchView.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
//                vlanIdView.setVisibility(isChecked ? VISIBLE : View.GONE);
//                vlanSwitchView.showDivider(isChecked);
//            }
//        });
    }

    @OnClick(R.id.submit)
    public void updatePowerOffTimeout() {
        final String timeout = timeoutView.getText().toString();
        if (TextUtils.isEmpty(timeout) || (Integer.parseInt(timeout) < 10 || Integer.parseInt(timeout) > 6000)) {
            activity.showToast(R.string.pt_msg_illegal_timeout, Toast.LENGTH_SHORT, Gravity.CENTER);
        } else {
            final ComputerSystem computer = new ComputerSystem();
            final ComputerSystem.Oem oem = new ComputerSystem.Oem();
            oem.setSafePowerOffTimoutSeconds(Integer.parseInt(timeout));
            computer.setOem(oem);
            activity.showLoadingDialog();
            LOG.info("Start to update power off timeout");
            activity.getRedfishClient().systems().update(computer, RRLB.<ComputerSystem>create(activity).callback(
                new RedfishResponseListener.Callback<ComputerSystem>() {
                    @Override
                    public void onResponse(Response okHttpResponse, ComputerSystem system) {
                        activity.onRefresh(null);
                        activity.showToast(R.string.msg_action_success, Toast.LENGTH_SHORT, Gravity.CENTER);
                        LOG.info("Update power off timeout done");
                    }
                }).build());
        }
    }

}
