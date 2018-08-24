package com.hai.store.keepalive.api;


import com.hai.store.bean.StoreADInfo;

interface IApi {

    void fetchConfig(Address.Area area, Callback<StoreADInfo> c);

    void report(String url);

    void report(String url, String bbEvent);
}
