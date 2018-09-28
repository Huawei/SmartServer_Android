package com.huawei.smart.server.redfish.constants;

import com.huawei.smart.server.R;

/**
 * Created by DuoQi on 2018-02-15.
 */
public enum LogEntryStatus {

    Asserted(R.string.system_log_status_asserted),
    Deasserted(R.string.system_log_status_deasserted),;

    int displayResId;

    LogEntryStatus(int displayResId) {
        this.displayResId = displayResId;
    }

    public int getDisplayResId() {
        return displayResId;
    }

}
