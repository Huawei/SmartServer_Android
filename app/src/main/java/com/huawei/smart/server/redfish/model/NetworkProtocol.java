package com.huawei.smart.server.redfish.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by DuoQi on 2018-02-13.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class NetworkProtocol extends Resource<NetworkProtocol> {

    Protocol SSH;
    private String Id;
    private String Name;

    @Getter
    @Setter
    @ToString
    public static class Protocol {
        Boolean ProtocolEnabled;
        Integer Port;
    }

}