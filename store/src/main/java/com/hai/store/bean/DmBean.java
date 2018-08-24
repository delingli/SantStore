package com.hai.store.bean;

import java.util.ArrayList;

/**
 * 自定义的下载管理
 */
public class DmBean {
    public String appId;
    public String appName;
    public String packageName;
    public String versionCode;
    public String versionName;
    public String size;
    public String iconUrl;
    public String downUrl;
    public ArrayList<String> repDc; //下载成功
    public ArrayList<String> repInstall; //安装成功
    public ArrayList<String> repAc; //激活成功
    public String repDel; //删除
    public String method;

    public DmBean(){}

    public DmBean(String appId, String appName, String packageName, String versionCode,
                  String versionName, String size, String iconUrl, String downUrl,
                  ArrayList<String> repDc, ArrayList<String> repInstall, ArrayList<String> repAc,
                  String repDel, String method) {
        this.appId = appId;
        this.appName = appName;
        this.packageName = packageName;
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.size = size;
        this.iconUrl = iconUrl;
        this.downUrl = downUrl;
        this.repDc = repDc;
        this.repInstall = repInstall;
        this.repAc = repAc;
        this.repDel = repDel;
        this.method = method;
    }

    @Override
    public String toString() {
        return "DmBean{" +
                "appId='" + appId + '\'' +
                ", appName='" + appName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", versionCode='" + versionCode + '\'' +
                ", versionName='" + versionName + '\'' +
                ", size='" + size + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", downUrl='" + downUrl + '\'' +
                ", repDc=" + repDc +
                ", repInstall=" + repInstall +
                ", repAc=" + repAc +
                ", repDel='" + repDel + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}
