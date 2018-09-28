package com.huawei.smart.server.redfish.model;

import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.huawei.smart.server.redfish.constants.DNSAddressOrigin;
import com.huawei.smart.server.redfish.constants.IPVersion;
import com.huawei.smart.server.redfish.constants.IPv4AddressOrigin;
import com.huawei.smart.server.redfish.constants.IPv6AddressOrigin;
import com.huawei.smart.server.redfish.constants.LinkStatus;
import com.huawei.smart.server.redfish.constants.NetworkPortMode;
import com.huawei.smart.server.redfish.constants.NetworkPortType;
import com.huawei.smart.server.redfish.jackson.WrappedObjectDeserializer;
import com.huawei.smart.server.redfish.jackson.WrappedObjectSerializer;
import com.huawei.smart.server.redfish.jackson.WrappedWith;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Mapping to Redfish OdataResource with "/redfish/v1/managers/1/EthernetInterfaceCollection"
 * Created by DuoQi on 2018-02-16.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class EthernetInterface extends Resource<EthernetInterface> {

    private String Id;
    private String Name;
    private String PermanentMACAddress;     // MAC地址
    private String HostName;        // 主机名
    private String FQDN;            // 全称域名
    private VLAN VLAN;

    private List<IPv4Address> IPv4Addresses;
    private List<IPv6Address> IPv6Addresses;
    private List<IPv6StaticAddress> IPv6StaticAddresses;    // TODO: don't know the difference with IPv6Addresses
    private String IPv6DefaultGateway;
    private List<String> NameServers;       // DNS server

    @WrappedWith("Huawei")
    @JsonDeserialize(using = WrappedObjectDeserializer.class)
    @JsonSerialize(using = WrappedObjectSerializer.class)
    private Oem Oem;       // DNS server


    public IPv4Address getIPv4() {
        final List<EthernetInterface.IPv4Address> iPv4Addresses = getIPv4Addresses();
        for (EthernetInterface.IPv4Address ip : iPv4Addresses) {
            if (!TextUtils.isEmpty(ip.getAddress())) {
                return ip;
            }
        }
        return null;
    }

    public IPv6Address getIPv6() {
        final List<EthernetInterface.IPv6Address> iPv6Addresses = getIPv6Addresses();
        for (EthernetInterface.IPv6Address ip : iPv6Addresses) {
            if (!ip.getAddressOrigin().equals(IPv6AddressOrigin.LinkLocal)) {
                return ip;
            }
        }
        return null;
    }

    public IPv6Address getIPv6LinkLocal() {
        final List<EthernetInterface.IPv6Address> iPv6Addresses = getIPv6Addresses();
        for (EthernetInterface.IPv6Address ip : iPv6Addresses) {
            if (ip.getAddressOrigin().equals(IPv6AddressOrigin.LinkLocal)) {
                return ip;
            }
        }
        return null;
    }


    @Getter
    @Setter
    @ToString
    public static class VLAN {
        @JsonProperty("VLANId")
        private Integer id;
        @JsonProperty("VLANEnable")
        private Boolean enabled;
    }

    @Getter
    @Setter
    @ToString
    public static class IPv4Address {
        private String Address;
        private String SubnetMask;
        private String Gateway;
        private IPv4AddressOrigin AddressOrigin;
    }

    @Getter
    @Setter
    @ToString
    public static class IPv6Address {
        private String Address;
        private Integer PrefixLength;
        private IPv6AddressOrigin AddressOrigin;
    }

    @Getter
    @Setter
    @ToString
    public static class IPv6StaticAddress {
        private String Address;
        private Integer PrefixLength;
    }

    @Getter
    @Setter
    @ToString
    public static class NetworkPort {
        NetworkPortType Type;          // 网口链路状态  (少了个ENUM)
        Integer PortNumber;           // 丝印号
        LinkStatus LinkStatus;        // 网口链路状态
        boolean AdaptiveFlag;       // 自适应标志
    }

    @Getter
    @Setter
    @ToString
    public static class Oem {
        IPVersion IPVersion;
        NetworkPortMode NetworkPortMode;
        NetworkPort ManagementNetworkPort;

        @JsonProperty("ManagementNetworkPort@Redfish.AllowableValues")
        List<NetworkPort> AllowableNetworkPortValues;
        List<NetworkPort> AdaptivePort;
        DNSAddressOrigin DNSAddressOrigin;
    }

}
