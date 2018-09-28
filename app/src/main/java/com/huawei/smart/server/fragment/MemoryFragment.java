package com.huawei.smart.server.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huawei.smart.server.BaseFragment;
import com.huawei.smart.server.R;
import com.huawei.smart.server.activity.HardwareActivity;
import com.huawei.smart.server.activity.SystemLogActivity;
import com.huawei.smart.server.adapter.MemoryListAdapter;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.SystemClient;
import com.huawei.smart.server.redfish.model.LogEntries;
import com.huawei.smart.server.redfish.model.LogEntryFilter;
import com.huawei.smart.server.redfish.model.Memory;
import com.huawei.smart.server.redfish.model.MemoryCollection;
import com.huawei.smart.server.task.LoadMemoriesTask;
import com.huawei.smart.server.utils.WidgetUtils;
import com.huawei.smart.server.widget.EnhanceRecyclerView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import butterknife.BindView;
import okhttp3.Response;

public class MemoryFragment extends BaseFragment {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MemoryFragment.class.getSimpleName());

    @BindView(R.id.memory_list) EnhanceRecyclerView mRecyclerView;
    @BindView(R.id.empty_view) View emptyView;
    @BindView(R.id.refresher) RefreshLayout refresher;

    private MemoryListAdapter adapter;
    MemoryCollection currentMemoryCollection;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memory, container, false);
        initialize(view);
        initializeView();
        return view;
    }

    public void initializeView() {
        this.adapter = new MemoryListAdapter(this.getContext(), new ArrayList<Memory>());
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(WidgetUtils.newDividerItemDecoration(this.getContext()));
        mRecyclerView.setEmptyView(emptyView);

        activity.showLoadingDialog();
        onRefresh(null);

        refresher.setEnableLoadMore(true); // 关闭上拉加载更多
        refresher.setEnableAutoLoadMore(false);
        refresher.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                MemoryFragment.this.onRefresh(refresher);
            }
        });

        refresher.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                final SystemClient client = activity.getRedfishClient().systems();
                client.getMemoryCollection(currentMemoryCollection.getNextLink(), RRLB.<MemoryCollection>create(activity).callback(
                    new RedfishResponseListener.Callback<MemoryCollection>() {
                        @Override
                        public void onResponse(Response okHttpResponse, MemoryCollection collection) {
                            LOG.info("Start load memory collection: {}", collection);
                            currentMemoryCollection = collection;
                            // no more data
                            refresher.setNoMoreData(TextUtils.isEmpty(collection.getNextLink()));
                            if (collection.getCount() > 0) {
                                ((HardwareActivity) activity).updateTabTitle(0, R.string.hardware_tab_memory, collection.getCount());
                                new LoadMemoriesTask(activity, MemoryFragment.this, adapter, true).submit(collection.getMembers());
                            } else {
                                refresher.finishLoadMore(true);
                            }
                        }
                    }).build());
            }
        });
    }

    public void finishRefreshing(boolean result) {
        if (refresher != null && refresher.getState() == RefreshState.Refreshing
            && !refresher.getState().isFinishing)
        {
            refresher.finishRefresh(result);
        }
    }

    public void onRefresh(RefreshLayout refreshLayout) {
        final SystemClient client = activity.getRedfishClient().systems();
        client.getMemoryCollection(null, RRLB.<MemoryCollection>create(activity).callback(
            new RedfishResponseListener.Callback<MemoryCollection>() {
                @Override
                public void onResponse(Response okHttpResponse, MemoryCollection collection) {
                    currentMemoryCollection = collection;
                    // no more data
                    refresher.setNoMoreData(TextUtils.isEmpty(collection.getNextLink()));
                    if (collection.getCount() > 0) {
                        ((HardwareActivity) activity).updateTabTitle(0, R.string.hardware_tab_memory, collection.getCount());
                        new LoadMemoriesTask(activity, MemoryFragment.this, adapter, false).submit(collection.getMembers());
                    } else {
                        finishLoadingViewData(true);
                    }
                }
            }).build());
    }


    public RefreshLayout getRefresher() {
        return refresher;
    }
}
