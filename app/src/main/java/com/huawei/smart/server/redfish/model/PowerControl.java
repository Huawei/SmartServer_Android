package com.huawei.smart.server.redfish.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.huawei.smart.server.redfish.jackson.WrappedObjectDeserializer;
import com.huawei.smart.server.redfish.jackson.WrappedObjectSerializer;
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
public class PowerControl extends Resource<PowerControl> {

    private String MemberId;
    private String Name;
    private Integer PowerConsumedWatts;
    private PowerMetrics PowerMetrics;
    private PowerLimit PowerLimit;

    @Getter
    @Setter
    @ToString
    public static class PowerMetrics {
        private Integer MinConsumedWatts;
        private Integer MaxConsumedWatts;
        private Integer AverageConsumedWatts;
    }

    @Getter
    @Setter
    @ToString
    public static class PowerLimit {
        private String LimitInWatts;
        private String LimitException;
    }


    @WrappedWith("Huawei")
    @JsonSerialize(using = WrappedObjectSerializer.class)
    @JsonDeserialize(using = WrappedObjectDeserializer.class)
    private Oem Oem;               // 产商自定义属性

    @Getter
    @Setter
    @ToString
    public static class Oem {

    }

}