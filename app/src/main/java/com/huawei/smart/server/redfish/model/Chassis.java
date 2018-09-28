package com.huawei.smart.server.redfish.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.huawei.smart.server.redfish.constants.ChassisType;
import com.huawei.smart.server.redfish.constants.IndicatorState;
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
public class Chassis extends Resource<Chassis> {

    private String Id;
    private String Name;
    @JsonProperty("IndicatorLED")
    private IndicatorState indicatorState;
    private ChassisType ChassisType;
    private ResourceStatus Status;

    @WrappedWith("Huawei")
    @JsonDeserialize(using = WrappedObjectDeserializer.class)
    private Oem Oem;               // 产商自定义属性

    @Getter
    @Setter
    @ToString
    public static class Oem {
        private Summary DriveSummary;
        private Summary PowerSupplySummary;
    }


    @Getter
    @Setter
    @ToString
    public static class Summary {
        Integer Count;
        ResourceStatus Status;
    }

}