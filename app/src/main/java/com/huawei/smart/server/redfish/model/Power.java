package com.huawei.smart.server.redfish.model;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.huawei.smart.server.redfish.constants.PowerSupplyType;
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
public class Power extends Resource<Power> {

    private String Id;
    private String Name;
    private List<PowerControl> PowerControl;
    private List<PowerSupply> PowerSupplies;

    @WrappedWith("Huawei")
    @JsonDeserialize(using = WrappedObjectDeserializer.class)
    private Oem Oem;               // 产商自定义属性

    @Getter
    @Setter
    @ToString
    public static class PowerSupply implements Comparable<PowerSupply> {
        String MemberId;
        String Name;
        ResourceStatus Status;
        PowerSupplyType PowerSupplyType;
        Integer LineInputVoltage;
        Integer PowerCapacityWatts;
        String Model;
        String FirmwareVersion;
        String SerialNumber;
        String Manufacturer;
        String PartNumber;

        @Override
        public int compareTo(@NonNull PowerSupply o) {
            return this.getMemberId().compareTo(o.getMemberId());
        }
    }

    @Getter
    @Setter
    @ToString
    public static class Oem {

    }

}