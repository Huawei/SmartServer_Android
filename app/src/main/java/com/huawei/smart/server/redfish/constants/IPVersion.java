package com.huawei.smart.server.redfish.constants;

import com.huawei.smart.server.R;

/**
 * Created by DuoQi on 2018-02-16.
 */
public enum IPVersion {
    IPv4AndIPv6(R.string.ns_network_ip_version_ipv4_and_ipv6),
    IPv4(R.string.ns_network_ip_version_ipv4),
    IPv6(R.string.ns_network_ip_version_ipv6),
    ;

    int displayResId;

    IPVersion(int displayResId) {
        this.displayResId = displayResId;
    }

    public int getDisplayResId() {
        return displayResId;
    }

}