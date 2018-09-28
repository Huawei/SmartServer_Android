package com.huawei.smart.server.redfish;

import com.huawei.smart.server.BaseActivity;

public class RRLB<T> {

    private BaseActivity activity;
    private RedfishResponseListener.Callback<T> callback;

    RRLB() {
    }

    public static <T> RRLB<T> create(BaseActivity activity) {
        return new RRLB<T>().activity(activity);
    }

    public RRLB<T> activity(BaseActivity activity) {
        this.activity = activity;
        return this;
    }

    public RRLB<T> callback(RedfishResponseListener.Callback<T> callback) {
        this.callback = callback;
        return this;
    }

    public RedfishResponseListener<T> build() {
        return new RedfishResponseListener<T>(activity, callback);
    }

}
