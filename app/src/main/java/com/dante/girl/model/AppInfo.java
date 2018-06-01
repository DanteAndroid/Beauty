package com.dante.girl.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * For app update
 */

public class AppInfo {

    /**
     * lastest_version : 1.0
     * lastest_version_code : 2
     * message : 修复一些bug;
     * UI细节调整
     * attach_info :
     * apkName : app-armeabi-v7a-release.apk
     * forceUpdate : true
     * former_version : v1.0
     * share_app_description : 这个软件不错，图片适合做壁纸。https://github.com/DanteAndroid/Beauty/
     * "vip":[ "Google", "Runoob", "Taobao" ]
     */

    @SerializedName("lastest_version")
    private String version;
    @SerializedName("lastest_version_code")
    private int versionCode;
    private String message;
    @SerializedName("apk_name")
    private String apkName;
    private boolean forceUpdate;
    @SerializedName("former_version")
    private String formerVersion;
    @SerializedName("share_app_description")
    private String shareApp;
    private List<String> vip;
    private String announcement;

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "version='" + version + '\'' +
                ", versionCode=" + versionCode +
                ", message='" + message + '\'' +
                ", apkName='" + apkName + '\'' +
                ", forceUpdate=" + forceUpdate +
                ", formerVersion='" + formerVersion + '\'' +
                ", shareApp='" + shareApp + '\'' +
                ", vip=" + vip +
                '}';
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getApkName() {
        return apkName;
    }

    public void setApkName(String apkName) {
        this.apkName = apkName;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public String getFormerVersion() {
        return formerVersion;
    }

    public void setFormerVersion(String formerVersion) {
        this.formerVersion = formerVersion;
    }

    public String getShareApp() {
        return shareApp;
    }

    public void setShareApp(String shareApp) {
        this.shareApp = shareApp;
    }

    public List<String> getVip() {
        return vip;
    }

    public void setVip(List<String> vip) {
        this.vip = vip;
    }
}
