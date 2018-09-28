package com.huawei.smart.server.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huawei.smart.server.BaseFragment;
import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.HardwareActivity;
import com.huawei.smart.server.adapter.ProcessorListAdapter;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishClient;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.model.Processor;
import com.huawei.smart.server.redfish.model.Processors;
import com.huawei.smart.server.task.LoadProcessorsTask;
import com.huawei.smart.server.utils.WidgetUtils;
import com.huawei.smart.server.widget.EnhanceRecyclerView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.ArrayList;

import butterknife.BindView;
import okhttp3.Response;


public class ProcessorFragment extends BaseFragment {

    @BindView(R.id.processor_list) EnhanceRecyclerView mRecyclerView;
    @BindView(R.id.empty_view) View emptyView;

    private ProcessorListAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_processor, container, false);
        this.initialize(view);
        initializeView();
        return view;
    }

    public void initializeView() {
        this.adapter = new ProcessorListAdapter(this.getContext(), new ArrayList<Processor>());
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(WidgetUtils.newDividerItemDecoration(this.getContext()));
        mRecyclerView.setEmptyView(emptyView);

        onRefresh(null);
    }

    public void onRefresh(RefreshLayout refreshLayout) {
        final RedfishClient redfish = activity.getRedfishClient();
        redfish.systems().getProcessors(RRLB.<Processors>create(activity).callback(
            new RedfishResponseListener.Callback<Processors>() {
                @Override
                public void onResponse(Response okHttpResponse, Processors processors) {
                    if (processors.getCount() > 0) {
                        ((HardwareActivity) activity).updateTabTitle(1, R.string.hardware_tab_cpu, processors.getCount());
                        new LoadProcessorsTask(null, ProcessorFragment.this, adapter).submit(processors.getMembers());
                    } else {
                        finishRefreshing(true);
                    }
                }
            }
        ).build());
    }

}
