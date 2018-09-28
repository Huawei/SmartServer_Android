package com.huawei.smart.server.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.huawei.smart.server.BaseFragment;
import com.huawei.smart.server.R;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.constants.BootSourceOverrideMode;
import com.huawei.smart.server.redfish.model.ComputerSystem;
import com.huawei.smart.server.utils.WidgetUtils;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Response;


public class BootModeFragment extends BaseFragment {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BootModeFragment.class.getSimpleName());

    @BindView(R.id.boot_mode)
    RadioGroup bootModeRadioGroup;
    @BindView(R.id.submit)
    View submitButton;

    private ComputerSystem computerSystem;
    private List<RadioButton> overrideModeRadioButtons = new ArrayList<>();
    private BootSourceOverrideMode[] overrideModeList = BootSourceOverrideMode.values();

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_boot_mode, container, false);
        this.initialize(view);
//        initializeView();
        return view;
    }

    private void initializeView() {
        final RadioGroup.LayoutParams radioButtonLayoutParams = WidgetUtils.getRadioButtonLayoutParams(getResources());
        for (int idx = 0; idx < overrideModeList.length; idx++) {
            final BootSourceOverrideMode mode = overrideModeList[idx];
            final RadioButton button = (RadioButton) getLayoutInflater().inflate(R.layout.widget_radio_button, null);
            button.setId(idx);
            button.setText(mode.name());
            overrideModeRadioButtons.add(button);
            bootModeRadioGroup.addView(button, radioButtonLayoutParams);
        }
    }

    @OnClick(R.id.submit)
    public void submit() {
        final int checkedRadioButtonId = bootModeRadioGroup.getCheckedRadioButtonId();

        final ComputerSystem build = new ComputerSystem();
        ComputerSystem.BootSourceOverride boot = new ComputerSystem.BootSourceOverride();
        boot.setOverrideMode(this.overrideModeList[checkedRadioButtonId]);
        build.setBoot(boot);

        activity.showLoadingDialog();
        LOG.info("Start update boot mode");
        activity.getRedfishClient().systems().update(build, RRLB.<ComputerSystem>create(activity).callback(
            new RedfishResponseListener.Callback<ComputerSystem>() {
                @Override
                public void onResponse(Response okHttpResponse, ComputerSystem system) {
                    activity.dismissLoadingDialog();
                    activity.showToast(R.string.msg_action_success, Toast.LENGTH_SHORT, Gravity.CENTER);
                    LOG.info("Update boot mode done");
                }
            }).build());
    }


    public void setComputerSystem(ComputerSystem computerSystem) {
        this.computerSystem = computerSystem;

        // 设置是否显示提交按钮
        final ComputerSystem.Oem oem = computerSystem.getOem();
        final String productVersion = oem.getProductVersion();
        final boolean isV3 = "V3".equalsIgnoreCase(productVersion);
        final Boolean readonly = isV3 || (oem.getBootModeChangeEnabled() != null && !oem.getBootModeChangeEnabled())
            || (oem.getBootModeConfigOverIpmiEnabled() != null && !oem.getBootModeConfigOverIpmiEnabled());
        submitButton.setVisibility(readonly ? View.INVISIBLE : View.VISIBLE);

        // 生成 Radio Buttons
        bootModeRadioGroup.removeAllViews();
        final BootSourceOverrideMode currentOverrideMode = computerSystem.getBoot().getOverrideMode();
        final RadioGroup.LayoutParams radioButtonLayoutParams = WidgetUtils.getRadioButtonLayoutParams(getResources());
        for (int idx = 0; idx < overrideModeList.length; idx++) {
            final BootSourceOverrideMode mode = overrideModeList[idx];
            final RadioButton button = (RadioButton) getLayoutInflater().inflate(R.layout.widget_radio_button, null);
            button.setId(idx);
            button.setText(mode.name());
            button.setChecked(currentOverrideMode.equals(overrideModeList[button.getId()]));
            button.setEnabled(!readonly);
            overrideModeRadioButtons.add(button);
            bootModeRadioGroup.addView(button, radioButtonLayoutParams);
        }
    }
}
