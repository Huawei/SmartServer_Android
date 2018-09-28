package com.huawei.smart.server.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.R;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.model.ActionResponse;
import com.huawei.smart.server.redfish.model.Power;
import com.huawei.smart.server.redfish.model.PowerControl;
import com.huawei.smart.server.widget.LabeledTextView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Response;

/**
 * 功耗信息
 */
public class PowerConsumptionActivity extends BaseActivity {

    @BindView(R.id.consumed_watts)
    LabeledTextView consumedWatts;

    @BindView(R.id.avg_consumed_watts)
    LabeledTextView avgConsumedWatts;

    @BindView(R.id.max_consumed_watts)
    LabeledTextView maxConsumedWatts;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_consumption);
        this.initialize(R.string.ds_label_menu_power_consumption, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLoadingDialog();
        onRefresh(null);
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        this.getRedfishClient().chassis().getPower(RRLB.<Power>create(this).callback(
            new RedfishResponseListener.Callback<Power>() {
                @Override
                public void onResponse(Response okHttpResponse, Power power) {
                    final PowerControl powerControl = power.getPowerControl().get(0);
                    consumedWatts.setText(powerControl.getPowerConsumedWatts() + "W");
                    avgConsumedWatts.setText(powerControl.getPowerMetrics().getAverageConsumedWatts() + "W");
                    maxConsumedWatts.setText(powerControl.getPowerMetrics().getMaxConsumedWatts() + "W");
                    finishLoadingViewData(true);
                }
            }).build());
    }


    /**
     * update device activity_location
    @OnClick(R.id.submit)
    public void onSaveLocation() {
        showLoadingDialog();
        this.getRedfishClient().chassis().recountPower(RRLB.<ActionResponse>create(this).callback(
            new RedfishResponseListener.Callback<ActionResponse>() {
                @Override
                public void onResponse(Response okHttpResponse, ActionResponse response) {
                    finishLoadingViewData(true);
                    // PowerConsumptionActivity.this.showToast(R.string.pc_recount_success, Toast.LENGTH_SHORT,  Gravity.CENTER);
                }
            }).build());
    }
     */
}
