package com.hai.store.keepalive.api;

import android.content.Context;
import android.text.TextUtils;

import com.hai.store.bean.StoreADInfo;

public final class Api implements IApi {

    /**
     * 蓝蝴蝶权限用户
     */
    public static final String HASPERMISSION = "10";
    /**
     * 无蓝蝴蝶权限用户
     */
    public static final String HASNOTPERMISSION = "11";

    private volatile static Api mApi;

    private IApi mApiHttp;

    private Api() {
    }

    public static Api getInstance() {
        if (null == mApi) {
            synchronized (Api.class) {
                if (null == mApi) {
                    mApi = new Api();
                }
            }
        }
        return mApi;
    }

    public void init(Context context) {
        mApiHttp = new ApiHttp(context);
    }

    @Override
    public void fetchConfig(Address.Area area, Callback<StoreADInfo> c) {
        mApiHttp.fetchConfig(area, c);
    }

    @Override
    public void report(String url) {
        if (!TextUtils.isEmpty(url))
            mApiHttp.report(url);
    }

    @Override
    public void report(String url, String bbEvent) {
        if (!TextUtils.isEmpty(url))
            mApiHttp.report(url, bbEvent);
    }
}
