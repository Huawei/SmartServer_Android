package com.huawei.smart.server.redfish.constants;

import com.huawei.smart.server.R;

/**
 * Created by DuoQi on 2018-02-15.
 */
public enum IndicatorState {

    Unknown(R.string.indicator_state_unknown, R.mipmap.ic_light_off),
    Lit(R.string.indicator_state_lit, R.mipmap.ic_light_on),
    Blinking(R.string.indicator_state_blinking, R.mipmap.ic_light_on),
    Off(R.string.indicator_state_off, R.mipmap.ic_light_off),
    ;

    int iconResId;
    int displayResId;

    IndicatorState(int displayResId, int iconResId) {
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
