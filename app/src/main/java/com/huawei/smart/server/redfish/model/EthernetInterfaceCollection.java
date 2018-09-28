package com.huawei.smart.server.redfish.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Mapping to Redfish OdataResource with "/redfish/v1/managers/1/EthernetInterfaces"
 * Created by DuoQi on 2018-02-16.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class EthernetInterfaceCollection extends Resource<EthernetInterfaceCollection> {
    private String Name;
    List<EthernetInterface> Members;
}
