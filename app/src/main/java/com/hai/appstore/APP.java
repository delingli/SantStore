package com.hai.appstore;

import android.app.Application;

/**
 * create by ldl2018/8/23 0023
 */

public class APP extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        com.hai.store.Application.init(this,true);
    }
}
