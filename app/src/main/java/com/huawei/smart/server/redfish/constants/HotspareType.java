package com.huawei.smart.server.redfish.constants;

import com.huawei.smart.server.R;

/**
 * Created by DuoQi on 2018-02-15.
 */
public enum HotspareType {
//    	无	None
//    全局	Global
//    局部	Dedicated
    None(R.string.drive_hotspare_none),
    Global(R.string.drive_hotspare_global),
    Dedicated(R.string.drive_hotspare_dedicated),
    ;

    int labelResId;

    HotspareType(int labelResId) {
        this.labelResId = labelResId;
    }

    public int getLabelResId() {
        return labelResId;
    }

}
