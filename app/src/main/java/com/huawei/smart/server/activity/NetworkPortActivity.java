package com.huawei.smart.server.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.TextView;

import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.R;
import com.huawei.smart.server.adapter.BaseListItemAdapter;
import com.huawei.smart.server.adapter.NetworkPortListAdapter;
import com.huawei.smart.server.redfish.ChassisClient;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.model.NetworkPort;
import com.huawei.smart.server.task.LoadListResourceTask;
import com.huawei.smart.server.utils.WidgetUtils;
import com.huawei.smart.server.widget.EnhanceRecyclerView;

import java.util.ArrayList;

import butterknife.BindView;

import static com.huawei.smart.server.HWConstants.BUNDLE_KEY_NETWORK_PORT_LIST;

/**
 */
public class NetworkPortActivity extends BaseActivity {

    @BindView(R.id.container)
    EnhanceRecyclerView mRecyclerView;
    @BindView(R.id.empty_view)
    TextView emptyView;

    private NetworkPortListAdapter adapter;
    private ArrayList<NetworkPort> resourceIds;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_port);
        this.initialize(R.string.title_network_port, true);

        final Bundle bundle = getIntent().getExtras();
        this.resourceIds = (ArrayList<NetworkPort>) bundle.getSerializable(BUNDLE_KEY_NETWORK_PORT_LIST);
        initializeView();
    }

    private void initializeView() {
        // setup list view
        this.adapter = new NetworkPortListAdapter(this, new ArrayList<NetworkPort>());
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(WidgetUtils.newDividerItemDecoration(this));
        mRecyclerView.setEmptyView(emptyView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.showLoadingDialog();
        new LoadItemListTask(this, adapter).submit(resourceIds);
    }

    public class LoadItemListTask extends LoadListResourceTask<NetworkPort> {

        public LoadItemListTask(BaseActivity activity, BaseListItemAdapter adapter) {
            super(activity, adapter);
        }

        @Override
        public void load(String odataId, RedfishResponseListener<NetworkPort> listener) {
            final ChassisClient client = activity.getRedfishClient().chassis();
            client.getNetworkPort(odataId, listener);
        }
    }

}
