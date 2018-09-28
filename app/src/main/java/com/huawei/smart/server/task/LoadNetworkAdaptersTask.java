package com.huawei.smart.server.task;

import com.huawei.smart.server.BaseFragment;
import com.huawei.smart.server.adapter.BaseListItemAdapter;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.model.NetworkAdapter;

public class LoadNetworkAdaptersTask extends LoadListResourceTask<NetworkAdapter> {
    
    public LoadNetworkAdaptersTask(BaseFragment fragment, BaseListItemAdapter adapter) {
        super(fragment, adapter);
    }

    @Override
    public void load(String odataId, RedfishResponseListener<NetworkAdapter> listener) {
        getRedfishClient().chassis().getNetworkAdapter(odataId, listener);
    }
}