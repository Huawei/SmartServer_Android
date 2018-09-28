package com.huawei.smart.server.redfish.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.huawei.smart.server.redfish.constants.TaskState;
import com.huawei.smart.server.redfish.jackson.WrappedObjectDeserializer;
import com.huawei.smart.server.redfish.jackson.WrappedWith;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by DuoQi on 2018-02-13.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class Task extends Resource<Task> {

    private String Id;
    private String Name;
    private TaskState TaskState;       // Completed
    private String TaskStatus;      // OK
    private Date StartTime;
    private Date EndTime;

    @WrappedWith("Huawei")
    @JsonDeserialize(using = WrappedObjectDeserializer.class)
    private Oem Oem;

    @Getter
    @Setter
    @ToString
    public static class Oem {
        String TaskPercentage;
    }

}