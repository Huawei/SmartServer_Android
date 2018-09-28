package com.huawei.smart.server.redfish.constants;

import com.huawei.smart.server.R;

/**
 * Created by DuoQi on 2018-02-16.
 */
public enum NetworkPortType {
    Dedicated(R.string.ns_port_port_type_dedicated, R.string.ns_port_port_type_dedicated_prefix),      // 专用
    Aggregation(R.string.ns_port_port_type_aggregation, R.string.ns_port_port_type_aggregation_prefix),    // 汇聚
    LOM(R.string.ns_port_port_type_LOM, R.string.ns_port_port_type_LOM_prefix),             // 板载
    ExternalPCIe(R.string.ns_port_port_type_External_PCIe, R.string.ns_port_port_type_External_PCIe_prefix),  //  PCIE 扩展网口
    LOM2(R.string.ns_port_port_type_LOM2, R.string.ns_port_port_type_LOM2_prefix),           //  板载网口2
    ;

    int prefixResId;
    int displayResId;

    NetworkPortType(int displayResId, int prefixResId) {
        this.displayResId = displayResId;
        this.prefixResId = prefixResId;
    }

    public int getDisplayResId() {
        return displayResId;
    }

    public int getPrefixResId() {
        return prefixResId;
    }

}