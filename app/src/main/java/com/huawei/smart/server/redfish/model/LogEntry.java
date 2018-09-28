package com.huawei.smart.server.redfish.model;

import com.huawei.smart.server.redfish.constants.LogEntryStatus;
import com.huawei.smart.server.redfish.constants.Severity;

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
public class LogEntry extends Resource<LogEntry> {

    private String Id;
    private String EventSubject;
    private Severity Severity;
    private String Message;
    private String EventId;
    private LogEntryStatus Status;  // Asserted
    private Date Created;
    private String Suggest;

}
