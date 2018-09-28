package com.huawei.smart.server.redfish.constants;

import com.huawei.smart.server.R;

/**
 * Created by DuoQi on 2018-02-15.
 */
public enum InitializationMode {

    UnInit(R.string.logical_drive_im_UnInit),
    QuickInit(R.string.logical_drive_im_QuickInit),
    FullInit(R.string.logical_drive_im_FullInit),
    ;

    int labelResId;

    InitializationMode(int labelResId) {
        this.labelResId = labelResId;
    }

    public int getLabelResId() {
        return labelResId;
    }

}
