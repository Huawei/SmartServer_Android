package com.huawei.smart.server.redfish.jackson;

import com.fasterxml.jackson.annotation.JacksonAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by DuoQi on 2018-02-18.
 */
@JacksonAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface WrappedWith {
    String value();
}