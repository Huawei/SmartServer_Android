package com.huawei.smart.server.redfish.model;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.huawei.smart.server.redfish.constants.NetworkAdapterPortType;
import com.huawei.smart.server.redfish.constants.NetworkPortLinkStatus;
import com.huawei.smart.server.redfish.jackson.WrappedObjectDeserializer;
import com.huawei.smart.server.redfish.jackson.WrappedWith;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Mapping to Redfish OdataResource with "/redfish/v1/Chassis/1/NetworkAdapters/mainboardNIC1"
 */
@Getter
@Setter
@ToString(callSuper = true)
public class NetworkPort extends Resource<NetworkPort> implements Comparable<NetworkPort> {

    private String Id;
    private String Name;
    private String PhysicalPortNumber;
    private NetworkPortLinkStatus LinkStatus;
    private List<String> AssociatedNetworkAddresses;

    @WrappedWith("Huawei")
    @JsonDeserialize(using = WrappedObjectDeserializer.class)
    private Oem Oem;               // 产商自定义属性

    @Override
    public int compareTo(@NonNull NetworkPort networkPort) {
        return Integer.valueOf(PhysicalPortNumber).compareTo(Integer.valueOf(networkPort.PhysicalPortNumber));
    }


    @Getter
    @Setter
    @ToString
    public static class Oem {
        private NetworkAdapterPortType PortType;
    }

}
