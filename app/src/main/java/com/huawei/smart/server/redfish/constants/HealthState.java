package com.huawei.smart.server.redfish.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.huawei.smart.server.R;

/**
 * Created by DuoQi on 2018-02-15.
 */
public enum HealthState {

    OK(R.string.ds_health_state_ok, R.mipmap.ic_status_normal, R.mipmap.ic_temp_ok),
    Warning(R.string.ds_health_state_warning, R.mipmap.ic_status_warning_2, R.mipmap.ic_temp_warning),
    Critical(R.string.ds_health_state_critical, R.mipmap.ic_status_critical_2, R.mipmap.ic_temp_critical),
    Unknown(R.string.state_unknown, R.mipmap.ic_status_transparent, R.mipmap.ic_status_transparent),;


    Integer iconResId;
    Integer displayResId;
    Integer tempIconResId;

    HealthState(Integer displayResId, Integer iconResId, Integer tempIconResId) {
        this.displayResId = displayResId;
        this.iconResId = iconResId;
        this.tempIconResId = tempIconResId;
    }

    @JsonCreator
    public static HealthState value(String v) {
        try {
            return valueOf(v);
        } catch (Exception e) {
            return Unknown;
        }
    }


    public Integer getDisplayResId() {
        return displayResId;
    }

    public Integer getIconResId() {
        return iconResId;
    }

    public Integer getTempIconResId() {
        return tempIconResId;
    }

    public static void main(String[] args) {
        System.out.print(valueOf(null));
    }
}
