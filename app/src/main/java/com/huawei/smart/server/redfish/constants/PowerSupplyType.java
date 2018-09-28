package com.huawei.smart.server.redfish.constants;

import com.huawei.smart.server.R;

public enum PowerSupplyType {

    ACorDC(R.string.hardware_power_power_supply_type_ac_or_dc),
    AC(R.string.hardware_power_power_supply_type_ac),
    DC(R.string.hardware_power_power_supply_type_dc),
    ;

    int displayResId;

    PowerSupplyType(int displayResId) {
        this.displayResId = displayResId;
    }

    public int getDisplayResId() {
        return displayResId;
    }

}