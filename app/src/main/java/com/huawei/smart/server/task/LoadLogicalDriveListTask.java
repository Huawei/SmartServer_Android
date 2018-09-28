package com.huawei.smart.server.task;

import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.BaseFragment;
import com.huawei.smart.server.adapter.BaseListItemAdapter;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.model.LogicalDrive;

import java.util.List;

public class LoadLogicalDriveListTask extends LoadListResourceTask<LogicalDrive> {

    public LoadLogicalDriveListTask(BaseActivity activity, BaseListItemAdapter adapter) {
        super(activity, adapter);
    }

    @Override
    public void load(String odataId, RedfishResponseListener<LogicalDrive> listener) {
        getRedfishClient().systems().getLogicalDrive(odataId, listener);
    }

}