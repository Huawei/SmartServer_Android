package com.huawei.smart.server.redfish.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.huawei.smart.server.redfish.constants.ProcessorType;
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
public class Processor extends Resource<Processor> {

    private String Id;
    private String Name;
    private ProcessorType ProcessorType;          // 处理器类型
    private String ProcessorArchitecture;       // 处理器架构 x86
    private String InstructionSet;              // 指令集
    private String Manufacturer;                // 产商
    private String Model;                       // 型号
    private ProcessorId ProcessorId;            // 处理器
    private Integer MaxSpeedMHz;                // 主频
    private Integer TotalCores;                 // 核数
    private Integer TotalThreads;               // 线程数
    private Integer Socket;
    private ResourceStatus Status;

    @WrappedWith("Huawei")
    @JsonDeserialize(using = WrappedObjectDeserializer.class)
    private Oem Oem;               // 产商自定义属性

    @Getter
    @Setter
    @ToString
    public static class ProcessorId {
        private String IdentificationRegisters;     // 处理器
        private String EffectiveFamily;
        private String EffectiveModel;
        private String MicrocodeInfo;
        private String Step;
        private String VendorId;
    }

    @Getter
    @Setter
    @ToString
    public static class Oem {
        private Integer L1CacheKiB;
        private Integer L2CacheKiB;
        private Integer L3CacheKiB;
        private String DeviceLocator;
        private String Position;
        private Integer Temperature;
        private Boolean EnabledSetting;
        private String PartNumber;              // 部件编码
        private String FrequencyMHz;
    }

}
