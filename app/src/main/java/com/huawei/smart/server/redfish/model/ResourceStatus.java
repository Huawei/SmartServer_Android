package com.huawei.smart.server.redfish.model;

import com.huawei.smart.server.redfish.constants.HealthRollupState;
import com.huawei.smart.server.redfish.constants.HealthState;
import com.huawei.smart.server.redfish.constants.ResourceState;

import lombok.Setter;
import lombok.ToString;

/**
 * Created by DuoQi on 2018-02-15.
 */
@Setter
@ToString
public class ResourceStatus {

    private ResourceState State;
    private HealthState Health;
    private HealthRollupState HealthRollup;


    public ResourceState getState() {
        return this.State == null ? ResourceState.Unknown : this.State;
    }

    public HealthState getHealth() {
        return this.Health == null ? HealthState.Unknown : this.Health;
    }

    public HealthRollupState getHealthRollup() {
        return this.HealthRollup == null ? HealthRollupState.Unknown : this.HealthRollup;
    }
}


