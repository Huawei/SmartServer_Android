package com.huawei.smart.server.task;

import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.BaseFragment;
import com.huawei.smart.server.adapter.BaseListItemAdapter;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.model.Storage;
import com.huawei.smart.server.redfish.model.StorageController;

import java.util.ArrayList;
import java.util.List;

public class LoadStorageListTask extends LoadListResourceTask<Storage> {

    public LoadStorageListTask(BaseFragment fragment, BaseListItemAdapter adapter) {
        super(fragment, adapter);
    }

    public LoadStorageListTask(BaseActivity activity, List<Storage> softwareInventories) {
        super(activity, softwareInventories);
    }

    @Override
    public void load(String odataId, RedfishResponseListener<Storage> listener) {
        getRedfishClient().systems().getStorage(odataId, listener);
    }

//    public List<?> convert(List<Storage> result) {
//        List<StorageController> controllers = new ArrayList<>();
//        for (Storage member : result) {
//            controllers.addAll(member.getStorageControllers());
//        }
//        return controllers;
//    }

}