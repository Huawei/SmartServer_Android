package com.huawei.smart.server.redfish.constants;

import com.huawei.smart.server.R;

/**
 * Created by DuoQi on 2018-02-15.
 */
public enum PatrolState {
//    重构状态	已停止	DoneOrNotRebuilt
//    正在重构	Rebuilding

    DoneOrNotPatrolled(R.string.drive_patroll_state_done_or_not_patrolled),
    Patrolling(R.string.drive_patroll_state_patrolling),
    ;

    int labelResId;

    PatrolState(int labelResId) {
        this.labelResId = labelResId;
    }

    public int getLabelResId() {
        return labelResId;
    }

}
