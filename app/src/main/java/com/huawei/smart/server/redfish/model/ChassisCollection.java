package com.huawei.smart.server.redfish.model;

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
public class ChassisCollection extends Resource<ChassisCollection> {

    private String Name;
    private List<Chassis> Members;

}