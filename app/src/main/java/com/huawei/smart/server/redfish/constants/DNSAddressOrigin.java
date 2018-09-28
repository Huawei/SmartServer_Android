package com.huawei.smart.server.redfish.constants;

import android.content.Context;

import com.huawei.smart.server.R;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by DuoQi on 2018-02-16.
 */
public enum DNSAddressOrigin {

    Static(R.string.ns_network_dns_addr_origin_static),    // 手动配置
    IPv4(R.string.ns_network_dns_addr_origin_ipv4),      // 从 IPv4 协议栈自动获取
    IPv6(R.string.ns_network_dns_addr_origin_ipv6),      // 从 IPv6 协议栈自动获取
    ;

    int displayResId;

    DNSAddressOrigin(int displayResId) {
        this.displayResId = displayResId;
    }

    public int getDisplayResId() {
        return displayResId;
    }

    public static Map<DNSAddressOrigin, String> toMap(Context context) {
        final Map<DNSAddressOrigin, String> map = new TreeMap<>();
        final DNSAddressOrigin[] values = DNSAddressOrigin.values();
        for (DNSAddressOrigin origin : values) {
            map.put(origin, context.getString(origin.getDisplayResId()));
        }
        return map;
    }

}