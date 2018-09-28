package com.huawei.smart.server.upgrade;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * 版本信息
 */
@Getter
@Setter
public class AppUpgrade implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private boolean hasNewVersion;
    private String newVersion;  //新版本号

    private String apkFileUrl; //新app下载地址
    private String apkFileSize; //新app大小
    private String apkFileMD5; //md5

    private String appStoreZh;  // 中文商店地址
    private String appStoreEn;  // 英文商店地址

    private String updateContentZh; //更新日志
    private String updateContentEn; //更新日志

    private boolean constraint; //是否强制更新

    //网络工具，内部使用
    private HttpManager httpManager;
    private String targetPath;

}
