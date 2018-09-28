package com.huawei.smart.server.redfish.constants;

import com.huawei.smart.server.R;

/**
 * Created by DuoQi on 2018-02-16.
 */
public enum NetworkPortMode {

    Automatic(R.string.ns_port_port_mode_automatic),
    Fixed(R.string.ns_port_port_mode_fixed),;


    int displayResId;

    NetworkPortMode(int displayResId) {
        this.displayResId = displayResId;
    }

    public int getDisplayResId() {
        return displayResId;
    }

}