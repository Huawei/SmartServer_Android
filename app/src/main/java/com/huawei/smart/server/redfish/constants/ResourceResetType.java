package com.huawei.smart.server.redfish.constants;

import com.huawei.smart.server.R;

/**
 * Created by DuoQi on 2018-02-15.
 */
public enum ResourceResetType {

    On(R.string.pt_label_power_on),
    GracefulShutdown(R.string.pt_label_power_off),
    ForceOff(R.string.pt_label_force_off),
    ForceRestart(R.string.pt_label_force_restart),
    ForcePowerCycle(R.string.pt_label_force_recycle),
    Nmi(R.string.pt_label_NMI),
    ;

    int labelResId;

    ResourceResetType(int labelResId) {
        this.labelResId = labelResId;
    }

    public int getLabelResId() {
        return labelResId;
    }

}
