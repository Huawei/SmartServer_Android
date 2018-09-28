package com.huawei.smart.server.model;

import java.io.Serializable;
import java.util.Date;

import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;
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
public class SearchHistory implements RealmModel, Serializable {

    @PrimaryKey
    private String id;
    @Required
    private String searchContent;        // search content
    private Date createdOn;

}
