package com.huawei.smart.server.redfish.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.huawei.smart.server.redfish.constants.EntryType;
import com.huawei.smart.server.redfish.constants.EventType;
import com.huawei.smart.server.redfish.constants.Severity;
import com.huawei.smart.server.redfish.jackson.WrappedObjectDeserializer;
import com.huawei.smart.server.redfish.jackson.WrappedWith;
import com.huawei.smart.server.widget.SimpleMenuSheetView;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by DuoQi on 2018-02-13.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class LogService extends Resource<LogService> {

    private String Id;
    private String Name;
    private Integer MaxNumberOfRecords;
    private String OverWritePolicy;
    private Date DateTime;
    private String DateTimeLocalOffset;
    private Boolean ServiceEnabled;

    @WrappedWith("Huawei")
    @JsonDeserialize(using = WrappedObjectDeserializer.class)
    private Oem Oem;               // 产商自定义属性

    @Deprecated
    private LogEntries Entries;              // 废弃
    private List<Subject> EventSubject;     // 主体类型列表


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Subject extends SimpleMenuSheetView.MenuItemSource {
        private Integer Id;
        private String Label;

        @Override
        public String getMenuText() {
            return getLabel();
        }

        @Override
        public String toString() {
            return getLabel();
        }
    }

    @Getter
    @Setter
    @ToString
    public static class Oem {
        @JsonProperty("HealthEvent")
        private List<HealthEvent> healthEvents;
    }

    @Getter
    @Setter
    @ToString
    public static class HealthEvent {
        private String EventId;
        private EventType EventType;    // 事件类型
        private EntryType EntryType;    // 日志类型
        private String EventSubject;    // 事件源标识
        private String Created;
        private Severity Severity;      // 严重性级别
        private String Message;         // 日志描述
        private String MessageId;
        private List<String> MessageArgs;
    }

}
