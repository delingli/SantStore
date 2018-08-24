package com.hai.store.bean;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class StoreADInfo implements Serializable {
    public String adtype; //bb_xxx_store
    public String name; //appname
    public String show_type; //bb_notify_app
    public String icon_img; //icon
    public ArrayList<String> ad_img; //***
    public String desc; //简介
    public String w; //**
    public String h; //**
    public long s_dur; //**
    public String down_url; //下载连接
    public String dplnk; //***
    public boolean rtp; //***
    public boolean rtp1; //***
    public int ia; //0
    public ArrayList<String> s_rpt; //展示
    public ArrayList<String> c_rpt; //点击详情
    public ArrayList<String> d_rpt; //点击下载
    public ArrayList<String> dc_rpt; //下载完成
    public ArrayList<String> i_rpt; //安装成功
    public ArrayList<String> a_rpt; //激活
    public ArrayList<String> o_rpt; //fu***k yo*
    public String ad_pack; //**
    public String ad_ver; //**
    public String appid;
    public String apk; //包名
    public String size;
    public String downcount;
    public String versioncode;
    public String versionname;
    public String href; //详情页链接
    public boolean vsb; //***ck y**u
    public boolean dlsign; //???mother fu**k ?
    public boolean logo; //???
    public boolean in_broser; //???
    public boolean bb_area; //???
    public String ci; //???
    public int cl; //???

    public static StoreADInfo getStoreADInfo(String data) {
        StoreADInfo config = null;
        try {
            config = new StoreADInfo();
            JSONObject root = new JSONObject(data);
            config.adtype = root.getString("adtype");
            config.show_type = root.getString("show_type");
            config.appid = root.optString("appid");
            config.apk = root.optString("apk");
            config.name = root.optString("name");
            config.versioncode = root.optString("versionName");
            config.versionname = root.optString("versionname");
            config.downcount = root.optString("downcount");
            config.size = root.optString("size");
            config.icon_img = root.optString("icon_img");
            config.href = root.optString("href");
            config.down_url = root.optString("down_url");
            config.desc = root.optString("desc");
            config.s_dur = root.optLong("s_dur");
            config.s_rpt = getList(root.optJSONArray("s_rpt"));
            config.c_rpt = getList(root.optJSONArray("c_rpt"));
            config.d_rpt = getList(root.optJSONArray("d_rpt"));
            config.dc_rpt = getList(root.optJSONArray("dc_rpt"));
            config.i_rpt = getList(root.optJSONArray("i_rpt"));
            config.a_rpt = getList(root.optJSONArray("a_rpt"));
        } catch (JSONException e) {
            Log.i("JSON", "data is wrong");
        }
        return config;
    }

    private static ArrayList<String> getList(JSONArray array) throws JSONException {
        ArrayList<String> urls = new ArrayList<>();
        if (null != array) {
            for (int i = 0; i < array.length(); i++)
                urls.add(array.getString(i));
        }
        return urls;
    }
}
