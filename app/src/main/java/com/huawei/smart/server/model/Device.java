package com.huawei.smart.server.model;

import com.huawei.smart.server.R;

import java.io.Serializable;
import java.util.Date;

import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RealmClass
public class Device implements RealmModel, Serializable {

    @PrimaryKey
    String id;  // use a generated UUID as PK

    @Required
    private String alias;        // device alias
    @Required
    private String hostname;    // Redfish IP or hostname
    @Required
    private Integer port;       // Redfish port, default 443
    @Required
    private String connectType = ConnectType.WIFI.name();

    @Required
    private String username;        //
    private String password;
    private String domain;
    private Boolean rememberPwd;
    private String token;
    private String sessionOdataId;

    private String serialNo;            // Product SN (产品序列号)
    private Integer warning;                // warning event amount
    private Date createdOn;
    private Date lastUpdatedOn;
    private Boolean switchable;         // 是否在切换设备列表中


    public enum ConnectType {

        WIFI(R.string.ad_connect_type_network, R.mipmap.ic_wifi),       // wifi
        Bluetooth(R.string.ad_connect_type_bluetooth, R.mipmap.ic_bluetooth),  // bluetooth
        Mobile(R.string.ad_connect_type_mobile, R.mipmap.ic_mobile);

        int displayResId;
        int iconResId;

        ConnectType(int displayResId, int iconResId) {
            this.displayResId = displayResId;
            this.iconResId = iconResId;
        }

        public int getDisplayResId() {
            return displayResId;
        }

        public int getIconResId() {
            return iconResId;
        }

        public static ConnectType from(String name) {
            final ConnectType[] values = ConnectType.values();
            for (ConnectType ct : values) {
                if (ct.name().equalsIgnoreCase(name)) {
                    return ct;
                }
            }
            return null;
        }
    }
}
