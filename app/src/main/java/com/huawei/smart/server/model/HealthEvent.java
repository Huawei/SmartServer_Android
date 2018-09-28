package com.huawei.smart.server.model;

import com.huawei.smart.server.redfish.model.LogService;

import java.io.Serializable;
import java.util.Date;

import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RealmClass
public class HealthEvent implements RealmModel, Serializable {

    @PrimaryKey
    String id;
    String deviceId;

    private String EventId;
    private String EventType;    // 事件类型
    private String EntryType;    // 日志类型
    private String EventSubject;    // 事件源标识


    private String Created;
    private String Severity;      // 严重性级别
    private String Message;         // 日志描述
    private String MessageId;


    public HealthEvent() {
    }

    public HealthEvent(LogService.HealthEvent event) {
        this.EventId = event.getEventId();
        this.EntryType = event.getEntryType().name();
        this.EventType = event.getEventType().name();
        this.EventSubject = event.getEventSubject();
        this.Created = event.getCreated();
        this.Severity = event.getSeverity().name();
        this.Message = event.getMessage();
        this.MessageId = event.getMessageId();
    }

}
