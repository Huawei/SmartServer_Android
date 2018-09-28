package com.huawei.smart.server.task;

import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.Toast;

import com.androidnetworking.error.ANError;
import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.BaseFragment;
import com.huawei.smart.server.R;
import com.huawei.smart.server.adapter.BaseListItemAdapter;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishClient;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.model.ResourceId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Response;

public abstract class LoadListResourceTask<Model extends ResourceId> extends AsyncTask<String, Void, List<Model>> {

    public static final ExecutorService THREAD_POOL_EXECUTOR = Executors.newFixedThreadPool(1);

    protected final BaseActivity activity;
    protected final BaseListItemAdapter adapter;
    protected final List<Model> items;
    protected final BaseFragment fragment;
    protected CountDownLatch latch;

    public LoadListResourceTask(BaseActivity activity, BaseListItemAdapter adapter) {
        this(activity, null, adapter, null);
    }

    public LoadListResourceTask(BaseActivity activity, List<Model> items) {
        this(activity, null, null, items);
    }

    public LoadListResourceTask(BaseFragment fragment, BaseListItemAdapter adapter) {
        this(null, fragment, adapter, null);
    }

    public LoadListResourceTask(BaseActivity activity, BaseFragment fragment, BaseListItemAdapter adapter, List<Model> items) {
        this.activity = activity;
        this.fragment = fragment;
        this.adapter = adapter;
        this.items = items;
    }

    protected RedfishClient getRedfishClient() {
        if (this.activity != null) {
            return this.activity.getRedfishClient();
        }

        if (this.fragment != null) {
            return this.fragment.getBaseActivity().getRedfishClient();
        }

        return null;
    }

    @Override
    protected List<Model> doInBackground(String... odataList) {
        final List<Model> result = new ArrayList<>();
        if (odataList.length > 0) {
            final CountDownLatch _latch = new CountDownLatch(odataList.length);
            for (String odataId : odataList) {
                final RedfishResponseListener<Model> listener = RRLB.<Model>create(activity).callback(
                    new RedfishResponseListener.Callback<Model>() {
                        @Override
                        public void onResponse(Response okHttpResponse, Model Model) {
                            result.add(Model);
                            _latch.countDown();
                        }

                        @Override
                        public void onError(ANError anError) {
                            super.onError(anError);
                            _latch.countDown();
                        }
                    }).build();
                load(odataId, listener);
            }
            try {
                _latch.await(300, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                activity.showToast(R.string.msg_access_api_failed, Toast.LENGTH_SHORT, Gravity.CENTER);
            }
        }
        return result;
    }

    public AsyncTask<String, Void, List<Model>> submit(List<Model> resources) {
        if (resources != null && resources.size() > 0) {
            String[] odataList = new String[resources.size()];
            for (int idx = 0; idx < resources.size(); idx++) {
                odataList[idx] = resources.get(idx).getOdataId();
            }
            return this.executeOnExecutor(THREAD_POOL_EXECUTOR, odataList);
        } else {
            return null;
        }
    }

    public AsyncTask<String, Void, List<Model>> submit(List<Model> resources, CountDownLatch latch) {
        this.latch = latch;
        if (resources != null) {
            String[] odataList = new String[resources.size()];
            for (int idx = 0; idx < resources.size(); idx++) {
                odataList[idx] = resources.get(idx).getOdataId();
            }
            return this.executeOnExecutor(THREAD_POOL_EXECUTOR, odataList);
        } else {
            latch.countDown();
            return null;
        }
    }

    public abstract void load(String odataId, RedfishResponseListener<Model> listener);

    /**
     * convert response result list to required type, default no convert
     *
     * @param result
     * @return
     */
    public List<?> convert(List<Model> result) {
        return result;
    }


    @Override
    protected void onPostExecute(List<Model> result) {
        // user will use the items himself
        if (this.items != null && result != null) {
            this.items.addAll(result);
        }

        // user don't need to handle data, just add items to the adapter
        if (this.adapter != null && result != null) {
            this.adapter.resetItems(convert(result));

            if (this.activity != null) {
                this.activity.finishLoadingViewData(true);
            }

            if (this.fragment != null) {
                this.fragment.finishRefreshing(true);
            }
        }

        if (this.latch != null) {
            this.latch.countDown();
        }
    }
}