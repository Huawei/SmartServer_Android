package com.huawei.smart.server.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.TextView;

import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.R;
import com.huawei.smart.server.adapter.BaseListItemAdapter;
import com.huawei.smart.server.adapter.DriveListAdapter;
import com.huawei.smart.server.adapter.NetworkPortListAdapter;
import com.huawei.smart.server.redfish.ChassisClient;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.model.Drive;
import com.huawei.smart.server.redfish.model.NetworkPort;
import com.huawei.smart.server.redfish.model.ResourceId;
import com.huawei.smart.server.task.LoadListResourceTask;
import com.huawei.smart.server.utils.WidgetUtils;
import com.huawei.smart.server.widget.EnhanceRecyclerView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.ArrayList;

import butterknife.BindView;

import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_DEVICE_ID;
import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_DRIVE_LIST;
import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_NETWORK_PORT_LIST;

/**
 */
public class DriveActivity extends BaseActivity {

    @BindView(R.id.container) EnhanceRecyclerView mRecyclerView;
    @BindView(R.id.empty_view) TextView emptyView;

    private DriveListAdapter adapter;
    private ArrayList<Drive> resourceIds;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);
        this.initialize(R.string.title_drive, true);

        final Bundle bundle = getIntent().getExtras();
        this.resourceIds = (ArrayList<Drive>) bundle.getSerializable(BUNDLE_KEY_DRIVE_LIST);
        initializeView();
    }

    private void initializeView() {
        this.adapter = new DriveListAdapter(this, new ArrayList<Drive>());
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(WidgetUtils.newDividerItemDecoration(this));
        mRecyclerView.setEmptyView(emptyView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.showLoadingDialog();
        onRefresh(null);
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        new LoadItemListTask(this, adapter).submit(resourceIds);
    }

    public class LoadItemListTask extends LoadListResourceTask<Drive> {

        public LoadItemListTask(BaseActivity activity, BaseListItemAdapter adapter) {
            super(activity, adapter);
        }

        @Override
        public void load(String odataId, RedfishResponseListener<Drive> listener) {
            activity.getRedfishClient().chassis().getDrive(odataId, listener);
        }
    }

}
