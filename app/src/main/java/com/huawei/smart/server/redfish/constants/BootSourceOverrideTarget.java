package com.huawei.smart.server.redfish.constants;

import com.huawei.smart.server.R;

/**
 * Created by DuoQi on 2018-02-15.
 */
public enum BootSourceOverrideTarget {

    None(R.string.bs_boot_override_none),
    Pxe(R.string.bs_boot_override_pxe),
    Floppy(R.string.bs_boot_override_floppy),
    Cd(R.string.bs_boot_override_cd),
    Hdd(R.string.bs_boot_override_hdd),
    BiosSetup(R.string.bs_boot_override_bios_setup),;

    int labelResId;

    BootSourceOverrideTarget(int labelResId) {
        this.labelResId = labelResId;
    }

    public int getLabelResId() {
        return labelResId;
    }

}
