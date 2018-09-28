package com.huawei.smart.server.redfish.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.huawei.smart.server.R;

/**
 * Created by DuoQi on 2018-02-15.
 */
public enum HealthRollupState {

    OK(R.string.ds_health_state_ok, R.mipmap.ic_status_normal),
    Warning(R.string.ds_health_state_warning, R.mipmap.ic_status_warning_2),
    Critical(R.string.ds_health_state_critical, R.mipmap.ic_status_critical_2),
    Unknown(R.string.state_unknown, R.mipmap.ic_status_transparent)
    ;


    Integer iconResId;
    Integer displayResId;

    HealthRollupState(Integer displayResId, Integer iconResId) {
        this.displayResId = displayResId;
        this.iconResId = iconResId;
    }

    @JsonCreator
    public static HealthRollupState forValue(String v) {
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

    public static void main(String[] args) {
        System.out.println(HealthRollupState.forValue("Warning"));
    }

}
