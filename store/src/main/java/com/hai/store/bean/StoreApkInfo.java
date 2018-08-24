package com.hai.store.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class StoreApkInfo implements Parcelable{
    public String appid; //应用id
    public String icon; //应用icon
    public String appname; //应用名
    public String apk; //应用包名
    public String downcount; //下载次数
    public String size; //apk大小
    public String versioncode; //版本号
    public String versionname; //版本名称
    public String href_download; //下载链接
    public String href_detail; //详情链接
    public ArrayList<String> rpt_ss;    //展示（单条上报
    public ArrayList<String> rpt_ct;    //点击详情
    public ArrayList<String> rpt_cd;    //点击下载
    public ArrayList<String> rpt_dc;    //下载完成
    public ArrayList<String> rpt_ic;    //安装成功
    public ArrayList<String> rpt_ac;    //激活
    public String rpt_dl;    //删除

    protected StoreApkInfo(Parcel in) {
        appid = in.readString();
        icon = in.readString();
        appname = in.readString();
        apk = in.readString();
        downcount = in.readString();
        size = in.readString();
        versioncode = in.readString();
        versionname = in.readString();
        href_download = in.readString();
        href_detail = in.readString();
        rpt_ss = in.createStringArrayList();
        rpt_ct = in.createStringArrayList();
        rpt_cd = in.createStringArrayList();
        rpt_dc = in.createStringArrayList();
        rpt_ic = in.createStringArrayList();
        rpt_ac = in.createStringArrayList();
        rpt_dl = in.readString();
    }

    public static final Creator<StoreApkInfo> CREATOR = new Creator<StoreApkInfo>() {
        @Override
        public StoreApkInfo createFromParcel(Parcel in) {
            return new StoreApkInfo(in);
        }

        @Override
        public StoreApkInfo[] newArray(int size) {
            return new StoreApkInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appid);
        dest.writeString(icon);
        dest.writeString(appname);
        dest.writeString(apk);
        dest.writeString(downcount);
        dest.writeString(size);
        dest.writeString(versioncode);
        dest.writeString(versionname);
        dest.writeString(href_download);
        dest.writeString(href_detail);
        dest.writeStringList(rpt_ss);
        dest.writeStringList(rpt_ct);
        dest.writeStringList(rpt_cd);
        dest.writeStringList(rpt_dc);
        dest.writeStringList(rpt_ic);
        dest.writeStringList(rpt_ac);
        dest.writeString(rpt_dl);
    }
}
