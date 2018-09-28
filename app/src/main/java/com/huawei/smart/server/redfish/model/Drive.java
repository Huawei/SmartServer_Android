package com.huawei.smart.server.redfish.model;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.huawei.smart.server.redfish.constants.HotspareType;
import com.huawei.smart.server.redfish.constants.IndicatorLED;
import com.huawei.smart.server.redfish.constants.PatrolState;
import com.huawei.smart.server.redfish.constants.RebuildState;
import com.huawei.smart.server.redfish.jackson.WrappedObjectDeserializer;
import com.huawei.smart.server.redfish.jackson.WrappedWith;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
public class Drive extends Resource<Drive> {
    String Id;
    String Name;
    String Model;
    String Revision;
    ResourceStatus Status;
    Long CapacityBytes;
    Boolean FailurePredicted;
    String Protocol;
    String MediaType;
    String Manufacturer;
    String SerialNumber;
    Integer CapableSpeedGbs;
    Integer NegotiatedSpeedGbs;
    String PredictedMediaLifeLeftPercent;
    IndicatorLED IndicatorLED;
    HotspareType HotspareType;
    String StatusIndicator;
    String FirmwareVersion;
    List<String> SupportedDeviceProtocols;

    @WrappedWith("Huawei")
    @JsonDeserialize(using = WrappedObjectDeserializer.class)
    Oem Oem;

    @Getter
    @Setter
    @ToString
    public static class Oem {
        Integer DriveID;
        RebuildState RebuildState;
        String RebuildProgress;
        PatrolState PatrolState;
        String FirmwareStatus;
        Integer TemperatureCelsius;
        List<String> SASAddress;
        String Type;
        String Position;
        Integer HoursOfPoweredUp;
    }

    @Override
    public int compareTo(@NonNull Drive t) {
        return this.getOem().getDriveID().compareTo(t.getOem().getDriveID());
    }

}