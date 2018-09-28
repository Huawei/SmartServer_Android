package com.huawei.smart.server.redfish.constants;

import com.huawei.smart.server.R;

/**
 * Created by DuoQi on 2018-02-15.
 */
public enum IndicatorLED {
//    重构状态	已停止	DoneOrNotRebuilt
//    正在重构	Rebuilding

    Off(R.string.drive_patroll_state_done_or_not_patrolled),
    Blinking(R.string.drive_patroll_state_patrolling),
    ;

    int labelResId;

    IndicatorLED(int labelResId) {
        this.labelResId = labelResId;
    }

    public int getLabelResId() {
        return labelResId;
    }

}
