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
public class LogEntries extends Resource<LogEntries> {

    private String Name;
    private Integer Count;
    private List<LogEntry> EntryList;

}
