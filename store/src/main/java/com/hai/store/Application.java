package com.hai.store;

import android.content.Context;

import com.hai.store.data.DownloadCart;
import com.hai.store.keepalive.GrayService;
import com.hai.store.keepalive.api.Api;
import com.hai.store.notify.NotifyController;
import com.hai.store.utils.Device;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.HttpHeaders;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class Application extends android.app.Application {

    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init(this, true);
    }

    public static void init(final android.app.Application context, boolean keepAlive) {
        mContext = context;
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(context));
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);

        HttpHeaders headers = new HttpHeaders();
        headers.put("User-Agent", Device.getDefaultUserAgent(context));
//        headers.put("commonHeaderKey1", "commonHeaderValue1");    //header不支持中文，不允许有特殊字符
//        headers.put("commonHeaderKey2", "commonHeaderValue2");
//        HttpParams params = new HttpParams();
//        params.put("commonParamsKey1", "commonParamsValue1");     //param支持中文,直接传,不要自己编码
//        params.put("commonParamsKey2", "这里支持中文参数");
        Api.getInstance().init(context);
        NotifyController.getInstance().init(context);
        OkGo.getInstance().init(context)                       //必须调用初始化
                .setOkHttpClient(builder.build())               //建议设置OkHttpClient，不设置将使用默认的
                .setCacheMode(CacheMode.NO_CACHE)               //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(3)                              //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
                .addCommonHeaders(headers);                     //全局公共头
//                .addCommonParams(params);

        new Thread(new Runnable() {
            @Override
            public void run() {
                DownloadCart.initStore(context);
            }
        }).start();
        if (keepAlive) GrayService.start(context);
    }
}
