package com.huawei.smart.server.redfish.constants;

import com.huawei.smart.server.R;

/**
 * Created by DuoQi on 2018-02-15.
 */
public enum NetworkPortLinkStatus {

    Up(R.string.hardware_network_port_link_status_up, R.mipmap.ic_status_ok),
    Down(R.string.hardware_network_port_link_status_down, R.mipmap.ic_status_warning),
    ;

    int iconResId;
    int displayResId;

    NetworkPortLinkStatus(int displayResId, int iconResId) {
        this.displayResId = displayResId;
        this.iconResId = iconResId;
    }


    public int getDisplayResId() {
        return displayResId;
    }

    public int getIconResId() {
        return iconResId;
    }


}
