package com.hai.store.keepalive.api;

public interface Callback<T> {

    void onFinish(boolean isSuccess, T t, int code, Object tag);
}
