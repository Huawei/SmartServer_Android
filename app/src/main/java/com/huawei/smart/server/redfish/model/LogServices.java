package com.huawei.smart.server.redfish.model;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class LogServices extends Resource<LogServices> {

    private String Name;
    @JsonProperty("Members@odata.count")
    private Integer count;
    private List<LogService> Members;

}
