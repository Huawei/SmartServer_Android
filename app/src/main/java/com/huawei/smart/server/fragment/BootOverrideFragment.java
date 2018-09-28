package com.huawei.smart.server.fragment;

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
import com.huawei.smart.server.redfish.constants.BootSourceOverrideEnabled;
import com.huawei.smart.server.redfish.constants.BootSourceOverrideTarget;
import com.huawei.smart.server.redfish.model.ComputerSystem;
import com.huawei.smart.server.utils.WidgetUtils;
import com.suke.widget.SwitchButton;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Response;


public class BootOverrideFragment extends BaseFragment {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BootOverrideFragment.class.getSimpleName());

    @BindView(R.id.AllowableOverrideValues) RadioGroup overrideTargetRadioGroup;
    @BindView(R.id.override_enabled) SwitchButton overrideEnabled;

    private ComputerSystem computerSystem;
    private BootSourceOverrideTarget[] overrideTargetList = BootSourceOverrideTarget.values();
    List<RadioButton> radioButtons = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_boot_override, container, false);
        this.initialize(view);
//        initializeView();
        return view;
    }

    private void initializeView() {
        final RadioGroup.LayoutParams radioButtonLayoutParams = WidgetUtils.getRadioButtonLayoutParams(getResources());
        for (int idx = 0; idx < overrideTargetList.length; idx++) {
            final BootSourceOverrideTarget overrideTarget = overrideTargetList[idx];
            final RadioButton inflated = (RadioButton) getLayoutInflater().inflate(R.layout.widget_radio_button, null);
            inflated.setId(idx);
            inflated.setText(overrideTarget.getLabelResId());
            radioButtons.add(inflated);
            overrideTargetRadioGroup.addView(inflated, radioButtonLayoutParams);
        }
    }

    @OnClick(R.id.submit)
    public void submit() {
        final int checkedRadioButtonId = overrideTargetRadioGroup.getCheckedRadioButtonId();
        final boolean overrideEnabled = this.overrideEnabled.isChecked();

        final ComputerSystem build = new ComputerSystem();
        ComputerSystem.BootSourceOverride boot = new ComputerSystem.BootSourceOverride();
        boot.setOverrideTarget(overrideTargetList[checkedRadioButtonId]);
        boot.setOverrideEnabled(overrideEnabled ? BootSourceOverrideEnabled.Continuous : BootSourceOverrideEnabled.Once);
        build.setBoot(boot);

        activity.showLoadingDialog();
        LOG.info("Start update boot override setting");
        activity.getRedfishClient().systems().update(build, RRLB.<ComputerSystem>create(activity).callback(
            new RedfishResponseListener.Callback<ComputerSystem>() {
                @Override
                public void onResponse(Response okHttpResponse, ComputerSystem system) {
                    activity.dismissLoadingDialog();
                    activity.showToast(R.string.msg_action_success, Toast.LENGTH_SHORT,  Gravity.CENTER);
                    LOG.info("Update boot override setting done");
                }
            }).build());
    }

    public void setComputerSystem(ComputerSystem computerSystem) {
        this.computerSystem = computerSystem;

        // 设置是否永久生效
        if (BootSourceOverrideEnabled.Continuous.equals(computerSystem.getBoot().getOverrideEnabled())) {
            overrideEnabled.setChecked(true);
        }

        overrideTargetRadioGroup.removeAllViews();
        final List<BootSourceOverrideTarget> allowableValues = this.computerSystem.getBoot().getAllowableValues();
        final BootSourceOverrideTarget currentOverrideTarget = computerSystem.getBoot().getOverrideTarget();// 当前启动介质
        final RadioGroup.LayoutParams radioButtonLayoutParams = WidgetUtils.getRadioButtonLayoutParams(getResources());
        for (int idx = 0; idx < overrideTargetList.length; idx++) {
            final BootSourceOverrideTarget overrideTarget = overrideTargetList[idx];
            final RadioButton inflated = (RadioButton) getLayoutInflater().inflate(R.layout.widget_radio_button, null);
            inflated.setId(idx);
            inflated.setText(overrideTarget.getLabelResId());
            inflated.setChecked(currentOverrideTarget.equals(overrideTarget)); // 设置选中状态
            if (allowableValues == null || !allowableValues.contains(overrideTarget)) {
                inflated.setEnabled(false);
            }
            radioButtons.add(inflated);
            overrideTargetRadioGroup.addView(inflated, radioButtonLayoutParams);
        }

//        for( RadioButton rb : radioButtons) {
//            rb.setChecked(currentOverrideTarget.equals(overrideTargetList[rb.getId()])); // 设置选中状态
//            if (allowableValues == null || !allowableValues.contains(overrideTargetList[rb.getId()])) {
//                rb.setEnabled(false);
//            }
//        }
    }
}
