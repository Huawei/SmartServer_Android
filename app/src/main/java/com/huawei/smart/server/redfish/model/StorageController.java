package com.huawei.smart.server.redfish.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.huawei.smart.server.redfish.jackson.WrappedObjectDeserializer;
import com.huawei.smart.server.redfish.jackson.WrappedWith;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StorageController extends Resource<StorageController> {
    private String MemberId;
    private String Name;
    private String Description;
    private ResourceStatus Status;
    private Integer SpeedGbps;
    private String FirmwareVersion;
    private List<String> SupportedDeviceProtocols;
    private String Manufacturer;
    private String Model;

    @WrappedWith("Huawei")
    @JsonDeserialize(using = WrappedObjectDeserializer.class)
    private Oem Oem;

    @Getter
    @Setter
    @ToString
    public static class Oem {
        private List<String> SupportedRAIDLevels;
        private ResourceId AssociatedCard;
        private String SASAddress;
        private String ConfigurationVersion;
        private Integer MemorySizeMiB;
        private Boolean MaintainPDFailHistory;
        private Boolean CopyBackState;
        private Boolean SmarterCopyBackState;
        private Boolean JBODState;
        private Boolean OOBSupport;
        private String CapacitanceName;
        private ResourceStatus CapacitanceStatus;
        private Long MinStripeSizeBytes;
        private Long MaxStripeSizeBytes;
    }

}