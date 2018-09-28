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
import com.huawei.smart.server.adapter.NetworkAdapterListAdapter;
import com.huawei.smart.server.redfish.ChassisClient;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.model.NetworkAdapter;
import com.huawei.smart.server.redfish.model.NetworkAdapters;
import com.huawei.smart.server.task.LoadNetworkAdaptersTask;
import com.huawei.smart.server.utils.WidgetUtils;
import com.huawei.smart.server.widget.EnhanceRecyclerView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Response;

import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_DEVICE_ID;


public class NetworkAdapterFragment extends BaseFragment {

    @BindView(R.id.network_adapter_list) EnhanceRecyclerView mRecyclerView;
    @BindView(R.id.empty_view) TextView emptyView;

    private NetworkAdapterListAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_network_adapter, container, false);
        this.initialize(view);
        initializeView();
        return view;
    }

    private void initializeView() {
        String deviceId = activity.getExtraString(BUNDLE_KEY_DEVICE_ID);
        this.adapter = new NetworkAdapterListAdapter(this.getContext(), deviceId, new ArrayList<NetworkAdapter>());
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(WidgetUtils.newDividerItemDecoration(this.getContext()));
        mRecyclerView.setEmptyView(emptyView);

        onRefresh(null);
    }

    public void onRefresh(RefreshLayout refreshLayout) {
        final ChassisClient chassis = activity.getRedfishClient().chassis();
        chassis.getNetworkAdapters(RRLB.<NetworkAdapters>create(activity).callback(
            new RedfishResponseListener.Callback<NetworkAdapters>() {
                @Override
                public void onResponse(Response okHttpResponse, NetworkAdapters networkAdapters) {
                    ((HardwareActivity) activity).updateTabTitle(3, R.string.hardware_tab_netcard, networkAdapters.getCount());
                    final List<NetworkAdapter> members = networkAdapters.getMembers();
                    if (members.size() > 0) {
                        new LoadNetworkAdaptersTask(NetworkAdapterFragment.this, adapter).submit(members);
                    } else {
                        finishRefreshing(true);
                    }
                }
            }).build());
    }

}
