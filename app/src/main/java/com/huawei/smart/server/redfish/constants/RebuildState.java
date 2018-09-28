package com.huawei.smart.server.redfish.constants;

import com.huawei.smart.server.R;

/**
 * Created by DuoQi on 2018-02-15.
 */
public enum RebuildState {
//    重构状态	已停止	DoneOrNotRebuilt
//    正在重构	Rebuilding

    DoneOrNotRebuilt(R.string.drive_rebuild_state_done_or_not_rebuilt),
    Rebuilding(R.string.drive_rebuild_state_building),
    ;

    int labelResId;

    RebuildState(int labelResId) {
        this.labelResId = labelResId;
    }

    public int getLabelResId() {
        return labelResId;
    }

}
