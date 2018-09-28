package com.huawei.smart.server.redfish.constants;

import com.huawei.smart.server.R;

public enum NetworkAdapterPortType {

    OpticalPort(R.string.hardware_network_port_port_type_optical),
    ElectricalPort(R.string.hardware_network_port_port_type_electrical),
    ;

    int displayResId;

    NetworkAdapterPortType(int displayResId) {
        this.displayResId = displayResId;
    }

    public int getDisplayResId() {
        return displayResId;
    }

}