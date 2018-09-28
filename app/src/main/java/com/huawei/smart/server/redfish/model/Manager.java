package com.huawei.smart.server.redfish.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.huawei.smart.server.redfish.jackson.WrappedObjectDeserializer;
import com.huawei.smart.server.redfish.jackson.WrappedObjectSerializer;
import com.huawei.smart.server.redfish.jackson.WrappedWith;

/**
 * <pre>
 * {
 * "@odata.context": "/redfish/v1/$metadata#Managers/Members/$entity",
 * "@odata.id": "/redfish/v1/Managers/1",
 * "@odata.accessMethod": "#Manager.v1_0_2.Manager",
 * "Id": "1",
 * "Name": "Manager",
 * "ManagerType": "BMC",
 * "FirmwareVersion": "2.35",
 * "DateTime": "2018-02-15T11:54:25+00:00",
 * "DateTimeLocalOffset": "+00:00",
 * "NetworkProtocol": {
 * "@odata.id": "/redfish/v1/Managers/1/NetworkProtocol"
 * },
 * "EthernetInterfaces": {
 * "@odata.id": "/redfish/v1/Managers/1/EthernetInterfaces"
 * },
 * "Actions": {
 * "#Manager.Reset": {
 * "target": "/redfish/v1/Managers/1/Actions/Manager.Reset",
 * "ResetType@Redfish.AllowableValues": [
 * "ForceRestart"
 * ]
 * }
 * },
 * "Links": {
 * "ManagerForServers": [
 * {
 * "@odata.id": "/redfish/v1/Systems/1"
 * }
 * ],
 * "ManagerForChassis": [
 * {
 * "@odata.id": "/redfish/v1/Chassis/1"
 * }
 * ]
 * }
 * }
 * </pre>
 * <p>
 * Created by DuoQi on 2018-02-13.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class Manager extends Resource<Manager> {

    private String Id;
    private String Name;
    private String ManagerType;
    private String FirmwareVersion;
    private Date DateTime;
    private String DateTimeLocalOffset;

    @WrappedWith("Huawei")
    @JsonDeserialize(using = WrappedObjectDeserializer.class)
    @JsonSerialize(using = WrappedObjectSerializer.class)
    private Oem Oem;               // 产商自定义属性

    private EthernetInterfaceCollection EthernetInterfaces;

    @Getter
    @Setter
    @ToString
    public static class Oem {
        private String DeviceLocation;
    }

}