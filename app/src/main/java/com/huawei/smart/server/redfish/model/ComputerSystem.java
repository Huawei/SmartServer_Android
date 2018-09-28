package com.huawei.smart.server.redfish.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.huawei.smart.server.redfish.constants.BootSourceOverrideTarget;
import com.huawei.smart.server.redfish.constants.PowerState;
import com.huawei.smart.server.redfish.jackson.WrappedObjectDeserializer;
import com.huawei.smart.server.redfish.jackson.WrappedObjectSerializer;
import com.huawei.smart.server.redfish.jackson.WrappedWith;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import com.huawei.smart.server.redfish.constants.BootSourceOverrideEnabled;
import com.huawei.smart.server.redfish.constants.BootSourceOverrideMode;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;

/**
 * Created by DuoQi on 2018-02-13.
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ComputerSystem extends Resource<ComputerSystem> {

    private String Model;                    // 服务器类型 - 产品型号
    private PowerState PowerState;          // 上电状态
    private String SerialNumber;            // Product SN (产品序列号)
    private String AssetTag;                // 资产标签
    private String BiosVersion;             // BIOS版本号
    private BootSourceOverride Boot;                // 系统启动信息
    private ProcessorSummary ProcessorSummary;      // 处理器
    private MemorySummary MemorySummary;
    private ResourceStatus Status;                   // 系统状态

    @WrappedWith("Huawei")
    @JsonDeserialize(using = WrappedObjectDeserializer.class)
    @JsonSerialize(using = WrappedObjectSerializer.class)
    private Oem Oem;               // 产商自定义属性

    private Resource Memory;
    private Resource EthernetInterfaces;
    private Resource NetworkInterfaces;
    private Resource Storage;
    private Resource LogServices;

    @Getter
    @Setter
    @ToString
    public static class BootSourceOverride {
        /**
         * 启动介质
         */
        @JsonProperty("BootSourceOverrideTarget")
        private BootSourceOverrideTarget overrideTarget;

        /**
         * 是否长久生效
         */
        @JsonProperty("BootSourceOverrideEnabled")
        private BootSourceOverrideEnabled overrideEnabled;

        /**
         * 启动模式
         */
        @JsonProperty("BootSourceOverrideMode")
        private BootSourceOverrideMode overrideMode;

        /**
         *  可选启动介质项
         */
        @JsonProperty("BootSourceOverrideTarget@Redfish.AllowableValues")
        private List<BootSourceOverrideTarget> AllowableValues;
    }

    @Getter
    @Setter
    @ToString
    public static class ProcessorSummary {
        private Integer Count;  // CPU 数目
        private String Model;   // CPU 型号
        private ResourceStatus Status;  // CPU 状态
    }

    @Getter
    @Setter
    @ToString
    public static class MemorySummary {
        private Integer TotalSystemMemoryGiB;  // CPU 数目
        private ResourceStatus Status;  // CPU 状态
    }

    @Getter
    @Setter
    @ToString
    public static class Oem {
        private String ProductAlias;        // 别名
        private Integer SafePowerOffTimoutSeconds; // 下电时限
        private Boolean BootModeConfigOverIpmiEnabled;
        private Boolean BootModeChangeEnabled;
        private String ProductVersion;
    }

}
