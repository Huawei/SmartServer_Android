package com.huawei.smart.server.task;

import com.huawei.smart.server.activity.HardwareActivity;
import com.huawei.smart.server.adapter.BaseListItemAdapter;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.SystemClient;
import com.huawei.smart.server.redfish.model.Storage;

import java.util.List;

public class LoadStoragesTask extends LoadListResourceTask<Storage> {

    public LoadStoragesTask(HardwareActivity activity, BaseListItemAdapter adapter) {
        super(activity, adapter);
    }

    public LoadStoragesTask(HardwareActivity activity, List<Storage> list) {
        super(activity, list);
    }

    @Override
    public void load(String odataId, RedfishResponseListener<Storage> listener) {
        final SystemClient client = activity.getRedfishClient().systems();
        client.getStorage(odataId, listener);
    }

    @Override
    protected void onPostExecute(List<Storage> result) {
        // user don't need to handle data, just add items to the adapter
        adapter.clearItems();
        int count = 0;
        for (Storage storage : result) {
            count += storage.getStorageControllers().size();
            adapter.addItems(storage.getStorageControllers());
        }
        ((HardwareActivity) activity).setBadgeToTab(4, count);
        this.activity.dismissLoadingDialog();

        if (this.latch != null) {
            this.latch.countDown();
        }
    }
}