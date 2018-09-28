package com.huawei.smart.server.model;

import java.io.Serializable;

import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * 用户偏好设置
 */
@Getter
@Setter
@ToString
@RealmClass
public class Preference implements RealmModel, Serializable {

    @PrimaryKey
    private Integer id;
    private Boolean acceptEventPush = false;        // 是否接受事件通知
    private String passcode;                           // 应用密码
    private Boolean isFingerprintEnabled = false;            // 是否开启指纹

}
