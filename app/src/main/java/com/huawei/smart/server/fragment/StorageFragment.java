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
import com.huawei.smart.server.adapter.StorageControllerListAdapter;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.SystemClient;
import com.huawei.smart.server.redfish.model.Storage;
import com.huawei.smart.server.redfish.model.StorageController;
import com.huawei.smart.server.redfish.model.Storages;
import com.huawei.smart.server.task.LoadStorageListTask;
import com.huawei.smart.server.utils.WidgetUtils;
import com.huawei.smart.server.widget.EnhanceRecyclerView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Response;


public class StorageFragment extends BaseFragment {

    @BindView(R.id.storage_controller_list) EnhanceRecyclerView mRecyclerView;
    @BindView(R.id.empty_view) TextView emptyView;

    public Storages storages;
    private StorageControllerListAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_storage, container, false);
        this.initialize(view);
        initializeView();
        return view;
    }

    private void initializeView() {
        this.adapter = new StorageControllerListAdapter(getActivity(), activity.getDeviceId(), new ArrayList<Storage>());
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(WidgetUtils.newDividerItemDecoration(this.getContext()));
        mRecyclerView.setEmptyView(emptyView);
    }

    @Override
    public void onResume() {
        super.onResume();
        onRefresh(null);
    }


    public void onRefresh(RefreshLayout refreshLayout) {
        // 加载数据
        activity.getRedfishClient().systems().getStorages(RRLB.<Storages>create(activity).callback(
            new RedfishResponseListener.Callback<Storages>() {
                @Override
                public void onResponse(Response okHttpResponse, Storages storages) {
                    StorageFragment.this.storages = storages;
                    final List<Storage> members = storages.getMembers();
                    ((HardwareActivity) activity).updateTabTitle(4, R.string.hardware_tab_storage, members.size());
                    if (members.size() > 0) {
                        new LoadStorageListTask(StorageFragment.this, adapter).submit(members);
                    } else {
                        finishRefreshing(true);
                    }
                }
            }
        ).build());
    }
}
