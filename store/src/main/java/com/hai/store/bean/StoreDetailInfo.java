package com.hai.store.bean;

import android.os.Parcel;

import java.util.List;

public class StoreDetailInfo extends StoreApkInfo {
    public String err; //详情页错误
    public String rating; //评分
    public String ratingperson; //评分人数
    public List<String> screenshots; //截图
    public String description; //详情介绍
    public String updateinfo; //更新内容
    public String updatetime; //更新时间
    public String os; //最低系统要求
    public String developer; //开发者
    public int flag_replace; //替换标记
    public int flag_download; //详情页下载标记
    public String rpt_st;    //详情页停留时间上报url，时长替换宏SZST_ST,
    public String rtp_method; //详情页上报方式

    protected StoreDetailInfo(Parcel in) {
        super(in);
    }
}
