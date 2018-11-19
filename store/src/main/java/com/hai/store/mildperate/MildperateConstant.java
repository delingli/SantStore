package com.hai.store.mildperate;

/*
一、新增广告位如下："app_homepage"=>"应用-首页打开推荐",
"app_trans"=>"应用-传输过程推荐",
"app_wash"=>"应用-洗包",
"app_hot"=>"应用-热传榜","app_discover"=>"应用-发现下应用","app_search"=>"应用-搜索结果","app_search2"=>"应用-搜索推荐"
        轻度运营yt2返回字段、竹蜻蜓请求时action字段都用它
        二、竹蜻蜓返回增加字段market为相应市场，例如Maapi为应用宝，调用应用圈接口时要拼上
        三、如果应用圈接口无应用返回，则调用竹蜻蜓请求时加上errmarket=无返回市场列表(用,分隔)，例如errmarket=Maapi,360，则竹蜻蜓将会排除掉应用宝和360的市场继续取其它应用市场
        四、我司市场应用补位返回方式还没定好
        五、关于出包定制时发现的配置*/
public class MildperateConstant {
    public static String APP_HOMEPAGE = "app_homepage";//应用-首页打开推荐
    public static String APP_TRANS = "app_trans";//应用-传输过程推荐

    public static String APP_DISCOVER = "app_discover";//应用-发现下应用
    public static String APP_SEARCH = "app_search";//应用-搜索结果
    public static String APP_SEARCH2 = "app_search2";//应用-搜索推荐

    public static String APP_HOT = "app_hot";//-热传榜列表
    public static String APP_SEARCH3 = "app_search3";//-热传榜搜索结果
    public static String APP_SEARCH4 = "app_search4";//-热传榜-搜索推荐

}
