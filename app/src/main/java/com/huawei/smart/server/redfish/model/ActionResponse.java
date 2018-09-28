package com.huawei.smart.server.redfish.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huawei.smart.server.redfish.constants.Severity;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by DuoQi on 2018-02-15.
 */
@Getter
@Setter
@ToString
public class ActionResponse implements Serializable {

    @JsonProperty("error")
    Wrapped error;

    @Getter
    @Setter
    @ToString
    public static class Wrapped {
        private String code;
        private String message;

        @JsonProperty("@Message.ExtendedInfo")
        List<ExtendedInfo> extendedInfoList;
    }

    @Getter
    @Setter
    @ToString
    public static class ExtendedInfo {
        @JsonProperty("@odata.type")
        String odataType;
        String MessageId;
        List<String> RelatedProperties;
        String Message;
        List<String> MessageArgs;
        Severity Severity;
        String Resolution;
    }
}
