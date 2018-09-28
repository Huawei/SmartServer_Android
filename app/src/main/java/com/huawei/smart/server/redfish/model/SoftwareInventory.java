package com.huawei.smart.server.redfish.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by DuoQi on 2018-02-13.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SoftwareInventory extends Resource<SoftwareInventory> {

    private String Id;
    private String Name;
    private String Version;
    private Boolean Updateable;
    private String SoftwareId;
    private ResourceStatus Status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SoftwareInventory)) return false;

        SoftwareInventory that = (SoftwareInventory) o;

        return getOdataId() != null ? getOdataId().equals(that.getOdataId()) : that.getOdataId() == null;
    }

    @Override
    public int hashCode() {
        return getOdataId() != null ? getOdataId().hashCode() : 0;
    }
}