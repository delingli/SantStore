package com.hai.store.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class StoreListInfo implements Parcelable{

    public String err;
    public String href_next;
    public String rpt_st;  //页面停留时间上报url, 时长替换宏SZST_ST;
    public int flag_replace; //是否需要替换宏（不包括页面停留时长替换）0/1, 0 : 不需要替换 1: 需要替换
    public String rtp_method; //上报方式
    public int flag_download; //下载标记 0/1, 0:直接下载 1: 拿到上报返回的地址再下载
    public List<StoreApkInfo> list;

    protected StoreListInfo(Parcel in) {
        err = in.readString();
        href_next = in.readString();
        rpt_st = in.readString();
        flag_replace = in.readInt();
        rtp_method = in.readString();
        flag_download = in.readInt();
        list = in.createTypedArrayList(StoreApkInfo.CREATOR);
    }

    public static final Creator<StoreListInfo> CREATOR = new Creator<StoreListInfo>() {
        @Override
        public StoreListInfo createFromParcel(Parcel in) {
            return new StoreListInfo(in);
        }

        @Override
        public StoreListInfo[] newArray(int size) {
            return new StoreListInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(err);
        dest.writeString(href_next);
        dest.writeString(rpt_st);
        dest.writeInt(flag_replace);
        dest.writeString(rtp_method);
        dest.writeInt(flag_download);
        dest.writeTypedList(list);
    }
}
