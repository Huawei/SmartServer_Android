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
import com.huawei.smart.server.adapter.HealthEventAdapter;
import com.huawei.smart.server.model.HealthEvent;
import com.huawei.smart.server.redfish.constants.Severity;
import com.huawei.smart.server.utils.WidgetUtils;
import com.huawei.smart.server.widget.EnhanceRecyclerView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import butterknife.BindView;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class HealthEventFragment extends BaseFragment {

    @BindView(R.id.empty_view) TextView emptyView;
    @BindView(R.id.health_event_list) EnhanceRecyclerView mRecyclerView;


    Severity severity;
    HealthEventAdapter mHealthEventListAdapter;

    public HealthEventFragment() {
    }

    public static HealthEventFragment build(Severity severity) {
        final HealthEventFragment healthEventFragment = new HealthEventFragment();
        healthEventFragment.severity = severity;
        return healthEventFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health_event, container, false);
        initialize(view);
        initializeView();
        return view;
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        activity.onRefresh(this.refreshLayout);
    }

    public void initializeView() {
        mHealthEventListAdapter = new HealthEventAdapter(activity, null);
        mRecyclerView.setAdapter(mHealthEventListAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(WidgetUtils.newDividerItemDecoration(activity));
        mRecyclerView.setEmptyView(emptyView);
    }

    public void update() {
        final RealmQuery<HealthEvent> query = getDefaultRealmInstance().where(HealthEvent.class)
            .equalTo("deviceId", activity.getDevice().getId());
        if (severity != null) {
            query.equalTo("Severity", severity.name());
        }
        final RealmResults<HealthEvent> events = query.findAll();
        mHealthEventListAdapter.setHealthEvents(events);
    }

}
