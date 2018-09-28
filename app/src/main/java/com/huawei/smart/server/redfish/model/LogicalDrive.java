package com.huawei.smart.server.redfish.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.huawei.smart.server.R;
import com.huawei.smart.server.redfish.constants.HealthState;
import com.huawei.smart.server.redfish.constants.InitializationMode;
import com.huawei.smart.server.redfish.jackson.WrappedObjectDeserializer;
import com.huawei.smart.server.redfish.jackson.WrappedWith;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by DuoQi on 2018-02-13.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class LogicalDrive extends Resource<LogicalDrive> {

    String Id;
    String Name;
    String VolumeType;
    Long CapacityBytes;
    Long OptimumIOSizeBytes;
    ResourceStatus Status;

    Links Links;

    @WrappedWith("Huawei")
    @JsonDeserialize(using = WrappedObjectDeserializer.class)
    Oem Oem;

    @Getter
    @Setter
    @ToString
    public static class Oem {
        String VolumeName;
        Integer RaidControllerID;
        String VolumeRaidLevel;
        String DefaultReadPolicy;
        String DefaultWritePolicy;
        String DefaultCachePolicy;
        Boolean ConsistencyCheck;
        Integer SpanNumber;
        Integer NumDrivePerSpan;
        String CurrentReadPolicy;
        String CurrentWritePolicy;
        String CurrentCachePolicy;
        String AccessPolicy;
        Boolean BootEnable;
        Boolean BGIEnable;
        Boolean SSDCachecadeVolume;
        Boolean SSDCachingEnable;
        List AssociatedCacheCadeVolume;
        String DriveCachePolicy;
        String OSDriveName;
        InitializationMode InitializationMode;
    }


    @Getter
    @Setter
    @ToString
    public static class Links {
        List<Drive> Drives;
    }

    @Setter
    @Getter
    @ToString
    public class ResourceStatus {
        private ResourceState State;
        private HealthState Health;
        public ResourceState getState() {
            return this.State == null ? ResourceState.Unknown : this.State;
        }

    }

    public enum ResourceState {

        Enabled(R.string.logical_drive_state_enabled, R.mipmap.ic_status_ok),
        StandbyOffline(R.string.logical_drive_state_offline, R.mipmap.ic_status_warning),
        Quiesced(R.string.logical_drive_state_degraded, R.mipmap.ic_status_warning),
        Unknown(R.string.state_unknown, R.mipmap.ic_status_transparent),
        ;

        Integer iconResId;
        Integer labelResId;

        @JsonCreator
        public static ResourceState value(String v){
            try {
                return valueOf(v);
            } catch (Exception e) {
                return Unknown;
            }
        }

        ResourceState(Integer labelResId, Integer iconResId) {
            this.labelResId = labelResId;
            this.iconResId = iconResId;
        }

        public Integer getLabelResId() {
            return labelResId;
        }

        public Integer getIconResId() {
            return iconResId;
        }
    }

}
