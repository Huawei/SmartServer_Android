package com.huawei.smart.server.redfish.model;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by DuoQi on 2018-02-13.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class MemoryView extends Resource<MemoryView> {

    String Name;

    @JsonProperty("Members@odata.count")
    Integer count;

    @JsonProperty("Information")
    List<Memory> Members;

    @JsonProperty("Members@odata.nextLink")
    String nextLink;

    @Data
    @ToString
    public static class Memory implements Comparable<Memory> {

        String Id;
        String Name;
        Integer CapacityMiB;                // 容量
        Integer OperatingSpeedMhz;         // 主频
        String Manufacturer;                // 产商
        String SerialNumber;                // 序列号
        String MemoryDeviceType;            // 类型 - DDR4

        ResourceStatus Status;
        Integer DataWidthBits;
        Integer RankCount;
        String PartNumber;
        String BaseModuleType;
        String DeviceLocator;

        Integer MinVoltageMillivolt;
        String Position;
        String Technology;

        Integer Socket;
        Integer Controller;
        Integer Channel;
        Integer Slot;

        @Override
        public int compareTo(@NonNull Memory t) {
            return this.getId().compareTo(t.getId());
        }

    }

}
