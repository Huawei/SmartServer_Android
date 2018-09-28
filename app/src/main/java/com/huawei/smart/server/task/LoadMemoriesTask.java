package com.huawei.smart.server.task;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseAndParsedRequestListener;
import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.BaseFragment;
import com.huawei.smart.server.adapter.BaseListItemAdapter;
import com.huawei.smart.server.fragment.MemoryFragment;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.model.Memory;

import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Date;

import okhttp3.Response;

public class LoadMemoriesTask extends LoadListResourceTask<Memory> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(LoadMemoriesTask.class.getSimpleName());

    boolean append = false;

    public LoadMemoriesTask(BaseActivity activity, BaseFragment fragment, BaseListItemAdapter adapter, boolean append) {
        super(activity, fragment, adapter, null);
        this.append = append;
    }

    public LoadMemoriesTask(BaseActivity activity, List<Memory> list) {
        super(activity, list);
    }

    @Override
    public void load(final String odataId, final RedfishResponseListener<Memory> listener) {
        final long start = System.currentTimeMillis();
        LOG.info("Start load memory: {}", odataId);
        getRedfishClient().systems().getMemory(odataId, new OkHttpResponseAndParsedRequestListener<Memory>() {
            @Override
            public void onResponse(Response okHttpResponse, Memory response) {
                LOG.info("load memory {} finished, costs {}ms", odataId, System.currentTimeMillis() -  start);
                listener.onResponse(okHttpResponse, response);
            }
            @Override
            public void onError(ANError anError) {
                LOG.info("failed to load memory: " + odataId, anError.getCause());
                listener.onError(anError);
            }
        });
    }

    @Override
    protected void onPostExecute(List<Memory> result) {
        // user will use the items himself
        if (this.items != null && result != null) {
            this.items.addAll(result);
        }

        // user don't need to handle data, just add items to the adapter
        if (this.adapter != null && result != null) {
            if (append) {
                this.adapter.addItems(convert(result));
            } else {
                this.adapter.resetItems(convert(result));
            }

            if (this.activity != null) {
                this.activity.finishLoadingViewData(true);
            }

            if (this.fragment != null) {
                this.fragment.finishRefreshing(true);
                if (this.fragment instanceof MemoryFragment) {
                    ((MemoryFragment) this.fragment).getRefresher().finishLoadMore(true);
                }
            }
        }

        if (this.latch != null) {
            this.latch.countDown();
        }
    }
}