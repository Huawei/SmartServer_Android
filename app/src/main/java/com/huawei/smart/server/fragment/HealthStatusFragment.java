package com.huawei.smart.server.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.ActivityUtils;
import com.huawei.smart.server.BaseFragment;
import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.HealthEventActivity;
import com.huawei.smart.server.activity.RealtimeStateActivity;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishClient;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.constants.HealthRollupState;
import com.huawei.smart.server.redfish.model.Chassis;
import com.huawei.smart.server.redfish.model.ComputerSystem;
import com.huawei.smart.server.redfish.model.Thermal;
import com.huawei.smart.server.utils.BundleBuilder;
import com.huawei.smart.server.widget.LabeledTextView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.concurrent.CountDownLatch;

import butterknife.BindView;
import okhttp3.Response;

import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_DEVICE_ID;

public class HealthStatusFragment extends BaseFragment {

    @BindView(R.id.fan) LabeledTextView fan;
    @BindView(R.id.power) LabeledTextView power;
    @BindView(R.id.drive) LabeledTextView drive;
    @BindView(R.id.cpu) LabeledTextView cpu;
    @BindView(R.id.memory) LabeledTextView memory;
    @BindView(R.id.temperature) LabeledTextView temperature;

    View.OnClickListener goHealthEventListener;

    Thermal thermal;
    Chassis chassis;
    ComputerSystem computerSystem;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health_status, container, false);
        initialize(view);

        goHealthEventListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = BundleBuilder.instance().with(BUNDLE_KEY_DEVICE_ID, activity.getDeviceId()).build();
                    ActivityUtils.startActivity(bundle, HealthEventActivity.class);
                }
            };
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        activity.showLoadingDialog();
        onRefresh(null);
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        new LoadHealthStatusTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public class LoadHealthStatusTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            final CountDownLatch latch = new CountDownLatch(3);
            final RedfishClient redfishClient = activity.getRedfishClient();
            redfishClient.chassis().getThermal(RRLB.<Thermal>create(activity).callback(
                new RedfishResponseListener.Callback<Thermal>() {
                    @Override
                    public void onResponse(Response okHttpResponse, Thermal thermal) {
                        HealthStatusFragment.this.thermal = thermal;
                        latch.countDown();
                    }
                }).build());

            redfishClient.systems().get(RRLB.<ComputerSystem>create(activity).callback(
                new RedfishResponseListener.Callback<ComputerSystem>() {
                    @Override
                    public void onResponse(Response okHttpResponse, ComputerSystem computerSystem) {
                        HealthStatusFragment.this.computerSystem = computerSystem;
                        latch.countDown();
                    }
                }).build());

            redfishClient.chassis().get(RRLB.<Chassis>create(activity).callback(new RedfishResponseListener.Callback<Chassis>() {
                @Override
                public void onResponse(Response okHttpResponse, Chassis chassis) {
                    HealthStatusFragment.this.chassis = chassis;
                    latch.countDown();
                }
            }).build());

            try {
                latch.await();
            } catch (InterruptedException e) {
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // 风扇健康状态
            final Thermal.Oem oem = thermal.getOem();
            if (oem != null) {
                if (oem.getFanSummary() != null && oem.getFanSummary().getStatus() != null) {
                    final HealthRollupState fanHealth = oem.getFanSummary().getStatus().getHealthRollup();
                    if (fanHealth != null) {
                        fan.setText(fanHealth.getDisplayResId());
                        fan.setDrawableEnd(fanHealth.getIconResId());
                        if (!fanHealth.equals(HealthRollupState.OK)) {
                            fan.setOnClickListener(goHealthEventListener);
                        }
                    }
                }

                // 温度健康状态
                if (oem.getTemperatureSummary() != null && oem.getTemperatureSummary().getStatus() != null) {
                    final HealthRollupState tempHealth = oem.getTemperatureSummary().getStatus().getHealthRollup();
                    if (tempHealth != null) {
                        temperature.setText(tempHealth.getDisplayResId());
                        temperature.setDrawableEnd(tempHealth.getIconResId());
                        if (!tempHealth.equals(HealthRollupState.OK)) {
                            temperature.setOnClickListener(goHealthEventListener);
                        }
                    }
                }
            }

            // CPU 健康
            if (computerSystem.getProcessorSummary() != null && computerSystem.getProcessorSummary().getStatus() != null) {
                final HealthRollupState cpuHealth = computerSystem.getProcessorSummary().getStatus().getHealthRollup();
                if (cpuHealth != null) {
                    cpu.setText(cpuHealth.getDisplayResId());
                    cpu.setDrawableEnd(cpuHealth.getIconResId());
                    if (!cpuHealth.equals(HealthRollupState.OK)) {
                        cpu.setOnClickListener(goHealthEventListener);
                    }
                }
            }

            // 内存健康
            if (computerSystem.getMemorySummary() != null && computerSystem.getMemorySummary().getStatus() != null) {
                final HealthRollupState memoryHealth = computerSystem.getMemorySummary().getStatus().getHealthRollup();
                if (memoryHealth != null) {
                    memory.setText(memoryHealth.getDisplayResId());
                    memory.setDrawableEnd(memoryHealth.getIconResId());
                    if (!memoryHealth.equals(HealthRollupState.OK)) {
                        memory.setOnClickListener(goHealthEventListener);
                    }
                }
            }

            final Chassis.Oem chassisOem = chassis.getOem();
            if (chassisOem != null) {
                // 存储健康
                if (chassisOem.getDriveSummary() != null && chassisOem.getDriveSummary().getStatus() != null) {
                    final HealthRollupState storageHealth = chassisOem.getDriveSummary().getStatus().getHealthRollup();
                    if (storageHealth != null) {
                        drive.setText(storageHealth.getDisplayResId());
                        drive.setDrawableEnd(storageHealth.getIconResId());
                        if (!storageHealth.equals(HealthRollupState.OK)) {
                            drive.setOnClickListener(goHealthEventListener);
                        }
                    }
                }

                // 电源健康状态
                if (chassisOem.getPowerSupplySummary() != null && chassisOem.getPowerSupplySummary().getStatus() != null) {
                    final HealthRollupState powerHealth = chassisOem.getPowerSupplySummary().getStatus().getHealthRollup();
                    if (powerHealth != null) {
                        power.setText(powerHealth.getDisplayResId());
                        power.setDrawableEnd(powerHealth.getIconResId());
                        if (!powerHealth.equals(HealthRollupState.OK)) {
                            power.setOnClickListener(goHealthEventListener);
                        }
                    }
                }
            }

            ((RealtimeStateActivity) activity).updateThermal(thermal);
            finishLoadingViewData(true);
        }
    }
}
