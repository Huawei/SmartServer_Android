package com.huawei.smart.server.redfish.constants;

import com.huawei.smart.server.R;

/**
 * Created by DuoQi on 2018-02-15.
 */
public enum PowerState {

    On(R.string.ds_power_state_on, R.mipmap.ic_power_on),
    Off(R.string.ds_power_state_off, R.mipmap.ic_power_off),
    //Reset(),
    ;


    int iconResId;
    int displayResId;

    PowerState(int displayResId, int iconResId) {
        this.displayResId = displayResId;
        this.iconResId = iconResId;
    }

    public int getDisplayResId() {
        return displayResId;
    }

    public int getIconResId() {
        return iconResId;
    }

    public static PowerState from(String state) {
        final PowerState[] values = PowerState.values();
        for (PowerState ps : values) {
            if (ps.name().equalsIgnoreCase(state)) {
                return ps;
            }
        }
        return null;
    }
}

