package com.hai.store.sqlite;

interface DBConstant {

    String DB_NAME = "st_store.db";
    int VERSION = 1;

    String NAME = "dm_list";
    String S_ID = "_id"; // 0
    String S_PKG = "_pkg"; //app package name 1
    String S_DID = "_did"; //app id 2
    String S_NAME = "_name"; //app name 3
    String S_VC = "_vc"; //version name 4
    String S_VN = "_vn"; //version code 5
    String S_SIZE = "_size"; //size 6
    String S_ICON = "_icon"; //icon url 7
    String S_URL = "_url"; //下载 url 8
    String RPT_DOWN_ED = "_ded"; //下载完成上报 9
    String RPT_INSTALL = "_ins"; //安装上报 10
    String RPT_AC = "_ac"; //激活上报 11
    String RPT_DEL = "_del"; //删除上报 12
    String RPT_MET = "_met"; //上报请求方式 13

    String CREATE_SQL = "create table " + NAME + " ("// 建表
            + S_ID + " integer primary key autoincrement,"
            + S_PKG + " varchar unique , "
            + S_DID + " varchar , "
            + S_NAME + " varchar , "
            + S_VC + " varchar , "
            + S_VN + " varchar , "
            + S_SIZE + " varchar , "
            + S_ICON + " varchar , "
            + S_URL + " varchar , "
            + RPT_DOWN_ED + " varchar , "
            + RPT_INSTALL + " varchar , "
            + RPT_AC + " varchar , "
            + RPT_DEL + " varchar , "
            + RPT_MET + " varchar"
            + ")";
}
