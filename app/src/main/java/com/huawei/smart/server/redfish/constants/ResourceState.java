package com.huawei.smart.server.redfish.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.huawei.smart.server.R;

/**
 * Created by DuoQi on 2018-02-15.
 */
public enum ResourceState {

    Enabled(R.string.resource_state_enabled, R.mipmap.ic_status_ok),
    Disabled(R.string.resource_state_disabled, R.mipmap.ic_status_warning),
    StandbyOffline(R.string.resource_state_absent, R.mipmap.ic_status_warning),
    StandbySpare(R.string.resource_state_absent, R.mipmap.ic_status_warning),
    InTest(R.string.resource_state_absent, R.mipmap.ic_status_warning),
    Starting(R.string.resource_state_absent, R.mipmap.ic_status_warning),
    Absent(R.string.resource_state_absent, R.mipmap.ic_status_warning),
    Unknown(R.string.state_unknown, R.mipmap.ic_status_transparent),
    ;

    Integer iconResId;
    Integer labelResId;

    @JsonCreator
    public static ResourceState value(String v)
    {
        try {
            return valueOf(v);
        } catch (Exception e) {
            return Unknown;
        }
    }

    ResourceState(Integer labelResId, Integer iconResId) {
        this.labelResId = labelResId;
        this.iconResId = iconResId;
    }

    public Integer getLabelResId() {
        return labelResId;
    }

    public Integer getIconResId() {
        return iconResId;
    }

}
