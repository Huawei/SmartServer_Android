package com.huawei.smart.server.task;

import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.adapter.BaseListItemAdapter;
import com.huawei.smart.server.adapter.SoftwareInventoryListAdapter;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.model.SoftwareInventory;

import java.util.List;

public class LoadSoftwareInventoryListTask extends LoadListResourceTask<SoftwareInventory> {

    public LoadSoftwareInventoryListTask(BaseActivity activity, BaseListItemAdapter adapter) {
        super(activity, adapter);
    }

    public LoadSoftwareInventoryListTask(BaseActivity activity, List<SoftwareInventory> softwareInventories) {
        super(activity, softwareInventories);
    }

    @Override
    public void load(String odataId, RedfishResponseListener<SoftwareInventory> listener) {
        activity.getRedfishClient().updateService().getSoftwareInventory(odataId, listener);
    }

    @Override
    protected void onPostExecute(List<SoftwareInventory> items) {
        // user will use the items himself
        if (this.items != null && items != null) {
            this.items.addAll(items);
        }

        // user don't need to handle data, just add items to the adapter
        if (adapter != null && items != null) {
            ((SoftwareInventoryListAdapter) adapter).updateItems(items);
            activity.finishLoadingViewData(true);
        }

        if (latch != null) {
            latch.countDown();
        }
    }
}