package com.huawei.smart.server.redfish.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.huawei.smart.server.redfish.jackson.WrappedObjectDeserializer;
import com.huawei.smart.server.redfish.jackson.WrappedWith;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by DuoQi on 2018-02-13.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class Memory extends Resource<Memory> {

    private String Id;
    private String Name;
    private Integer CapacityMiB;                // 容量
    private Integer OperatingSpeedMhz;         // 主频
    private String Manufacturer;                // 产商
    private String SerialNumber;                // 序列号
    private String MemoryDeviceType;            // 类型 - DDR4

    private ResourceStatus Status;
    private Integer DataWidthBits;
    private Integer RankCount;
    private String PartNumber;
    private String BaseModuleType;
    private String DeviceLocator;
    private MemoryLocation MemoryLocation;


    @WrappedWith("Huawei")
    @JsonDeserialize(using = WrappedObjectDeserializer.class)
    private Oem Oem;               // 产商自定义属性


    @Getter
    @Setter
    @ToString
    public static class MemoryLocation {
        private Integer Socket;
        private Integer Controller;
        private Integer Channel;
        private Integer Slot;
    }

    @Getter
    @Setter
    @ToString
    public static class Oem {
        private Integer MinVoltageMillivolt;
        private String Technology;
        private String Position;
    }

}
