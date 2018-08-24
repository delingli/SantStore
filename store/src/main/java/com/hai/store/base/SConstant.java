package com.hai.store.base;

public interface SConstant {

    /*==============================================Bundle key========================================*/

    String PKG_NAME = "packageName";
    String APP_NAME = "appName";
    String DETAIL_NOTIFY = "detail_notify";
    String DETAIL_ELSE = "detail_else";
    String LIST_MODE = "list_mode";
    String DETAIL_MODE = "detail_mode";

    /*=============================================test values========================================*/

    String APPPKG = "com.ak.juhe.demo";
    String AD_SPACE_ID = "aF5QjCWBqc";
    String CH = "110123";
    String FROM = "test";
    String QI_HOO = "qihoo";
    String SEARCH_WORD = "同城旅游";

    /*========================================网络请求常量==============================================*/

    //    String MARKET = "http://172.18.0.19/cyan/tang/trunk/api/src/market.php"; //测试接口
    String MARKET = "http://adapi.yiticm.com:7701/market.php"; //正式接口

    String TYPE = "?type=";
    String TYPE_CATEGORY = "category"; //获取分类/栏目
    String TYPE_LIST = "list"; //获取列表
    String TYPE_RECOMMEND_AD = "recommendad"; //洗包
    String TYPE_WIFI = "wifi";
    String TYPE_SEARCH = "search";

    String APP_LIST = "applist"; //洗包列表 eg:type=?type=recommendad&applist=com.wuba,com.rasoft.bubble
    //post 不需要符号 '='

    String CID = "&cid=";

    int CID_APP_LIST = -1; // 应用列表
    int CID_HOT = -2; //热门应用
    int CID_HOT_SEARCH_RECOMMEND = -11; //热门搜索应用

    int CID_WIFI = -4; //wifi ad
    String TMODE = "&tmode=";

    //         "-27"=>"发现-应用列表页","-28"=>"发现-应用详情页", "-29"=>"发现搜索结果", "-30"=>"发现-搜索推荐"
    interface CID_FOUND {
        int CID_APP_LIST = -27;//发现-应用列表页
        int CID_APP_INFO = -28;//发现-应用详情页
        int CID_APP_SEARCHRESULT = -29;//发现搜索结果
        int CID_APP_SEARCH_RECOMMENDED = -27;//发现-搜索推荐

    }

    String TMODE_WIFI = "wifi"; //wifi直接下载
    String TMODE_WIFI2 = "wifi2"; //wifi进入列表和详情
    String TMODE_NOTIFY = "notify";
    String TMODE_ICON = "icon";

    String PAGE = "&page=";

    String SEARCH = "&search=";

    /*==============================================SP KEY============================================*/

    String NOTIFY = "notify";
}
