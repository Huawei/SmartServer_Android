package com.huawei.smart.server.redfish.constants;

import com.huawei.smart.server.R;

/**
 * Created by DuoQi on 2018-02-15.
 */
public enum Severity {
    OK(R.mipmap.ic_status_ok),
    Warning(R.mipmap.ic_status_warning),
    Critical(R.mipmap.ic_status_critical),;

    int iconResId;

    Severity(int iconResId) {
        this.iconResId = iconResId;
    }

    public int getIconResId() {
        return iconResId;
    }

}
