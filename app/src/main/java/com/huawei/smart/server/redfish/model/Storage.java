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
public class Storage extends Resource<Storage> {

    private String Id;
    private String Name;

    List<StorageController> StorageControllers;
    List<Drive> Drives;
    Volumes Volumes;

}
