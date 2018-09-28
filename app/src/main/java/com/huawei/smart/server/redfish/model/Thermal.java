package com.huawei.smart.server.redfish.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.huawei.smart.server.redfish.jackson.WrappedObjectDeserializer;
import com.huawei.smart.server.redfish.jackson.WrappedWith;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 2.5.4 查询指定机箱散热资源信息
 */
@Getter
@Setter
@ToString(callSuper = true)
public class Thermal extends Resource<Thermal> {

    private String Id;
    private String Name;
    private List<Temperature> Temperatures;     // 温度列表

    @WrappedWith("Huawei")
    @JsonDeserialize(using = WrappedObjectDeserializer.class)
    private Oem Oem;               // 产商自定义属性

    @Getter
    @Setter
    @ToString
    public static class Oem {
        private Summary FanSummary;
        private Summary TemperatureSummary;
    }

    @Getter
    @Setter
    @ToString
    public static class Summary {
        Integer Count;
        ResourceStatus Status;
    }

    @Getter
    @Setter
    @ToString
    public static class Temperature {
        String MemberId;
        String SensorNumber;
        String Name;
        Integer ReadingCelsius;
        Integer LowerThresholdNonCritical;      // 指定温度传感器的低温轻微告警阈值
        Integer LowerThresholdCritical;         // 指定温度传感器的低温严重告警阈值
        Integer LowerThresholdFatal;            // 指定温度传感器的低温紧急告警阈值
        Integer UpperThresholdNonCritical;
        Integer UpperThresholdCritical;
        Integer UpperThresholdFatal;
        Integer MinReadingRangeTemp;            // 指定温度传感器可读取的最低温度
        Integer MaxReadingRangeTemp;
        ResourceStatus Status;
    }

}