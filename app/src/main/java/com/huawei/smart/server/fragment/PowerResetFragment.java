package com.huawei.smart.server.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.smart.server.BaseFragment;
import com.huawei.smart.server.R;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.constants.ResourceResetType;
import com.huawei.smart.server.redfish.model.ActionResponse;
import com.huawei.smart.server.redfish.model.ComputerSystem;
import com.huawei.smart.server.utils.WidgetUtils;
import com.huawei.smart.server.widget.LabeledTextView;

import org.slf4j.LoggerFactory;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Response;


/**
 * 上下电
 */
public class PowerResetFragment extends BaseFragment {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(PowerResetFragment.class.getSimpleName());

    @BindView(R.id.resetTypeRadioGroup) RadioGroup radioGroup;
//    @BindView(R.id.section) LabeledTextView section;

    @BindView(R.id.value) TextView powerStatusValue;
    @BindView(R.id.image) ImageView powerStatusImage;

    private ResourceResetType[] resetTypes = ResourceResetType.values();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_power_reset, container, false);
        initialize(view);
        initializeView();
        return view;
    }

    private void initializeView() {
        for (int idx = 0; idx < resetTypes.length; idx++) {
            final ResourceResetType resetType = resetTypes[idx];
            final RadioButton inflated = (RadioButton) getLayoutInflater().inflate(R.layout.widget_radio_button, null);
            inflated.setId(idx);
            inflated.setText(resetType.getLabelResId());
            radioGroup.addView(inflated, WidgetUtils.getRadioButtonLayoutParams(getResources()));
        }
    }

    public void updateView(ComputerSystem computerSystem) {
        if (computerSystem.getPowerState() != null) {
            powerStatusValue.setText(computerSystem.getPowerState().getDisplayResId());
            powerStatusImage.setImageDrawable(ContextCompat.getDrawable(activity, computerSystem.getPowerState().getIconResId()));
            // 已下电状态下，不能再次下电 ??
            //  if (computerSystem.getPowerState().equals(PowerState.Off)) {
            //  }
        }
    }

    @OnClick(R.id.submit)
    public void submit() {
        final int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
        final ResourceResetType resetType = this.resetTypes[checkedRadioButtonId];
        activity.showLoadingDialog();
        LOG.info("Start reset power status");
        activity.getRedfishClient().systems().reset(resetType, RRLB.<ActionResponse>create(activity)
            .callback(new RedfishResponseListener.Callback<ActionResponse>() {
                @Override
                public void onResponse(Response okHttpResponse, ActionResponse system) {
                    activity.onRefresh(null);
                    activity.showToast(R.string.msg_action_success, Toast.LENGTH_SHORT, Gravity.CENTER);
                    LOG.info("Reset power status successfully");
                }
            }).build());
    }

}
