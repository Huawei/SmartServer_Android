package com.huawei.smart.server.task;

import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.BaseFragment;
import com.huawei.smart.server.adapter.BaseListItemAdapter;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.model.Processor;

import java.util.List;

public class LoadProcessorsTask extends LoadListResourceTask<Processor> {

    public LoadProcessorsTask(BaseActivity activity, BaseFragment fragment, BaseListItemAdapter adapter) {
        super(activity, fragment, adapter, null);
    }

    public LoadProcessorsTask(BaseActivity activity, List<Processor> list) {
        super(activity, list);
    }

    @Override
    public void load(String odataId, RedfishResponseListener<Processor> listener) {
        getRedfishClient().systems().getProcessor(odataId, listener);
    }


}