package com.huawei.smart.server.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.ActivityUtils;
import com.huawei.smart.server.BaseFragment;
import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.HealthEventActivity;
import com.huawei.smart.server.adapter.LabeledTextItemListAdapter;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.constants.HealthState;
import com.huawei.smart.server.redfish.model.Thermal;
import com.huawei.smart.server.utils.BundleBuilder;
import com.huawei.smart.server.widget.EnhanceRecyclerView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Response;

import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_DEVICE_ID;

public class TemperatureFragment extends BaseFragment {

    @BindView(R.id.temperature_list)
    EnhanceRecyclerView temperatureRecyclerView;

    @BindView(R.id.empty_view)
    View emptyView;

    View.OnClickListener goHealthEventListener;
    LabeledTextItemListAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_temperature, container, false);
        initialize(view);
        initializeView();
        return view;
    }

    private void initializeView() {
        goHealthEventListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = BundleBuilder.instance().with(BUNDLE_KEY_DEVICE_ID, activity.getDeviceId()).build();
                    ActivityUtils.startActivity(bundle, HealthEventActivity.class);
                }
            };
        // 设置温度
        this.adapter = new LabeledTextItemListAdapter(getContext(), new ArrayList<LabeledTextItemListAdapter.LabeledTextItem>());
        temperatureRecyclerView.setAdapter(adapter);
        temperatureRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        temperatureRecyclerView.setEmptyView(emptyView);
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        activity.getRedfishClient().chassis().getThermal(RRLB.<Thermal>create(activity).callback(
            new RedfishResponseListener.Callback<Thermal>() {
                @Override
                public void onResponse(Response okHttpResponse, Thermal thermal) {
                    updateThermal(thermal);
                }
            }).build());
    }

    public void updateThermal(Thermal thermal) {
        final List<Thermal.Temperature> temperatures = thermal.getTemperatures();
        List<LabeledTextItemListAdapter.LabeledTextItem> items = new ArrayList<>();
        for (Thermal.Temperature temp : temperatures) {
            if (temp.getReadingCelsius() != null) {
                final String name = temp.getName();
                boolean noUnit = !TextUtils.isEmpty(name) && (name.endsWith(" DTS") || name.endsWith(" Margin"));
                final LabeledTextItemListAdapter.LabeledTextItem item = LabeledTextItemListAdapter.LabeledTextItem.builder()
                    .label(name).value(temp.getReadingCelsius() + (noUnit ? "" : "℃"))
                    .drawableEnd(temp.getStatus().getHealth().getTempIconResId()).build();
                if (!temp.getStatus().getHealth().equals(HealthState.OK)) {
                    item.setOnClickListener(goHealthEventListener);
                }
                items.add(item);
            }
        }
        this.adapter.resetItems(items);
        this.finishRefreshing(true);
    }

}
