package com.huawei.smart.server.utils;

import android.os.Bundle;

import java.io.Serializable;

/**
 * Created by DuoQi on 2018-02-20.
 */
public class BundleBuilder {

    private Bundle bundle = new Bundle();

    public static BundleBuilder instance() {
        return new BundleBuilder();
    }

    public BundleBuilder with(String key, Serializable value) {
        this.bundle.putSerializable(key, value);
        return this;
    }

    public BundleBuilder with(String key, String value) {
        this.bundle.putString(key, value);
        return this;
    }

    public Bundle build() {
        return this.bundle;
    }
}