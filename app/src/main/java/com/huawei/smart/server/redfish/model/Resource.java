package com.huawei.smart.server.redfish.model;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by DuoQi on 2018-02-15.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class Resource<T extends Resource> extends ResourceId<T> implements Comparable<T> {

    @JsonProperty("@odata.context")
    private String odataContext;

    @JsonProperty("@odata.type")
    private String odataType;

    @JsonIgnore
    private String etag;        // resource etag header

    @Override
    public int compareTo(@NonNull T t) {
        return this.getOdataId().compareTo(t.getOdataId());
    }
}
