package com.huawei.smart.server.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.widget.TextView;

import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.R;
import com.huawei.smart.server.adapter.LogicalDriveListAdapter;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.model.LogicalDrive;
import com.huawei.smart.server.redfish.model.Volumes;
import com.huawei.smart.server.task.LoadLogicalDriveListTask;
import com.huawei.smart.server.utils.WidgetUtils;
import com.huawei.smart.server.widget.EnhanceRecyclerView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Response;

import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_VOLUME_ID;

/**
 */
public class LogicalDriveActivity extends BaseActivity {

    @BindView(R.id.container) EnhanceRecyclerView mRecyclerView;
    @BindView(R.id.empty_view) TextView emptyView;

    private LogicalDriveListAdapter adapter;
    private String volumesOdataId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logical_drive);
        this.initialize(R.string.title_volumes, true);
        final Bundle bundle = getIntent().getExtras();
        this.volumesOdataId = bundle.getString(BUNDLE_KEY_VOLUME_ID);
        initializeView();
    }

    private void initializeView() {
        this.adapter = new LogicalDriveListAdapter(this, new ArrayList<LogicalDrive>());
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(WidgetUtils.newDividerItemDecoration(this));
        mRecyclerView.setEmptyView(emptyView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(volumesOdataId)) {
            this.showLoadingDialog();
            onRefresh(null);
        }
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        if (!TextUtils.isEmpty(volumesOdataId)) {
            this.getRedfishClient().systems().getVolumes(volumesOdataId, RRLB.<Volumes>create(this).callback(
                new RedfishResponseListener.Callback<Volumes>() {
                    @Override
                    public void onResponse(Response okHttpResponse, Volumes volumes) {
                        final List<LogicalDrive> members = volumes.getMembers();
                        if (members.size() > 0) {
                            new LoadLogicalDriveListTask(LogicalDriveActivity.this, adapter).submit(members);
                        } else {
                            finishRefreshing(true);
                        }
                    }
                }
            ).build());
        } else {
            finishRefreshing(true);
        }
    }
}
