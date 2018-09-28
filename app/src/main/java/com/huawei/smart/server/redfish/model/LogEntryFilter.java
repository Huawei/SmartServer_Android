package com.huawei.smart.server.redfish.model;

import android.text.TextUtils;

import com.huawei.smart.server.redfish.constants.Severity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by DuoQi on 2018-02-13.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@ToString(callSuper = true)
@AllArgsConstructor
public class LogEntryFilter extends Resource<LogEntryFilter> implements Cloneable {

    public static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    String keyword;

    Severity level;
    Integer subject;
    Date startTime;
    Date endTime;

    Integer offset;
    Integer length;

    public LogEntryFilter() {

    }


    public Map<String, String> convertToMap() {
        Map<String, String> filter = new HashMap<String, String>();
        if (!TextUtils.isEmpty(keyword)) {
            filter.put("keyword", keyword);
        }

        if (level != null) {
            filter.put("level", level.name());
        }

        if (subject != null) {
            filter.put("subject", String.valueOf(subject));
        }

        if (startTime != null) {
            filter.put("startTime", formatter.format(startTime));
        }

        if (endTime != null) {
            filter.put("endTime", formatter.format(endTime));
        }

        if (offset != null) {
            filter.put("offset", String.valueOf(offset));
        }

        filter.put("length", length == null ? "32" : String.valueOf(length));
        return filter;
    }

    public boolean isSameStartTime(Date startTime) {
        if (this.startTime == null && startTime == null) {
            return true;
        } else if (this.startTime == null && startTime != null) {
            return false;
        } else if (this.startTime != null && startTime == null) {
            return false;
        } else {
            return formatter.format(this.startTime).equals(formatter.format(startTime));
        }
    }

    public boolean isSameEndTime(Date endTime) {
        if (this.endTime == null && endTime == null) {
            return true;
        } else if (this.endTime == null && endTime != null) {
            return false;
        } else if (this.endTime != null && endTime == null) {
            return false;
        } else {
            return formatter.format(this.endTime).equals(formatter.format(endTime));
        }
    }
}
