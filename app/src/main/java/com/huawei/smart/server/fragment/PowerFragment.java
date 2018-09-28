package com.huawei.smart.server.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huawei.smart.server.BaseFragment;
import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.HardwareActivity;
import com.huawei.smart.server.adapter.PowerListAdapter;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.constants.ResourceState;
import com.huawei.smart.server.redfish.model.Power;
import com.huawei.smart.server.utils.WidgetUtils;
import com.huawei.smart.server.widget.EnhanceRecyclerView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Response;


public class PowerFragment extends BaseFragment {

    @BindView(R.id.power_supply_list) EnhanceRecyclerView mRecyclerView;
    @BindView(R.id.empty_view) TextView emptyView;

    private PowerListAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_power, container, false);
        this.initialize(view);
        initializeView();
        return view;
    }

    private void initializeView() {
        this.adapter = new PowerListAdapter(getActivity(), new ArrayList<Power.PowerSupply>());
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(WidgetUtils.newDividerItemDecoration(this.getContext()));
        mRecyclerView.setEmptyView(emptyView);

        onRefresh(null);
    }

    public void onRefresh(RefreshLayout refreshLayout) {
        activity.getRedfishClient().chassis().getPower(RRLB.<Power>create(activity)
            .callback(new RedfishResponseListener.Callback<Power>() {
                @Override
                public void onResponse(Response okHttpResponse, Power power) {
                    final List<Power.PowerSupply> powerSupplies = power.getPowerSupplies();
                    final List<Power.PowerSupply> filtered = new ArrayList<>();
                    for (Power.PowerSupply powerSupply : powerSupplies) {
                        if (powerSupply.getStatus() != null && !powerSupply.getStatus().getState().equals(ResourceState.Absent)) {
                            filtered.add(powerSupply);
                        }
                    }

                    ((HardwareActivity) activity).updateTabTitle(2, R.string.hardware_tab_power, filtered.size());
                    adapter.resetItems(filtered);
                    finishRefreshing(true);
                }
            }).build());
    }

}
