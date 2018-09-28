package com.huawei.smart.server.redfish.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
public class NetworkAdapter extends Resource<NetworkAdapter> {

    private String Id;
    private String Name;
    private String Manufacturer;
    private String Model;
    private ResourceStatus Status;

    @WrappedWith("Huawei")
    @JsonDeserialize(using = WrappedObjectDeserializer.class)
    private Oem Oem;               // 产商自定义属性

    private List<Controller> Controllers;


    @Getter
    @Setter
    @ToString
    public static class Oem {
        private String Name;
        private String DriverName;
        private String DriverVersion;
        private String CardManufacturer;
        private String CardModel;
        private String DeviceLocator;
        private String Position;
        private List<String> NetworkTechnology;
    }

    @Getter
    @Setter
    @ToString
    public static class Controller {
        private Link Link;
    }

    @Getter
    @Setter
    @ToString
    public static class Link {
        private List<NetworkPort> NetworkPorts;
    }

}
