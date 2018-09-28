package com.huawei.smart.server.redfish.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by DuoQi on 2018-02-15.
 */
@Getter
@Setter
@ToString
public class ResourceId<T> implements Serializable {

    @JsonProperty("@odata.id")
    private String odataId;

    public Class<T> getResourceType() {
        Class<T> resourceType = (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass())
            .getActualTypeArguments()[0];
        return resourceType;
    }

}
