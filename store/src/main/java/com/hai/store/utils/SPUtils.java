package com.hai.store.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtils {
    private static final Object LOCK = new Object();
    private static SharedPreferences instance;

    public static SharedPreferences getInstance(Context context) {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    String SP_NAME = "AppStore";
                    instance = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
                }
            }
        }
        return instance;
    }

    /*============================================================================================*/
    // 保存一个Boolean值
    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences sp = getInstance(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    // 获取一个Boolean值, 默认值是false
    public static boolean getBoolean(Context context, String key) {
        return getBoolean(context, key, false);
    }

    // 获取一个Boolean值
    public static boolean getBoolean(Context context, String key, boolean defValue) {
        SharedPreferences sp = getInstance(context);
        return sp.getBoolean(key, defValue);
    }

    /*============================================================================================*/
    // 保存一个String值
    public static void putString(Context context, String key, String value) {
        SharedPreferences sp = getInstance(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    // 获取一个String值, 默认值是null
    public static String getString(Context context, String key) {
        return getString(context, key, null);
    }

    // 获取一个String值
    public static String getString(Context context, String key, String defValue) {
        SharedPreferences sp = getInstance(context);
        return sp.getString(key, defValue);
    }

    /*============================================================================================*/
    // 保存一个int值
    public static void putInt(Context context, String key, int value) {
        SharedPreferences sp = getInstance(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    // 获取一个int值, 默认值是-1
    public static int getInt(Context context, String key) {
        return getInt(context, key, -1);
    }

    // 获取一个int值
    public static int getInt(Context context, String key, int defValue) {
        SharedPreferences sp = getInstance(context);
        return sp.getInt(key, defValue);
    }

    /*============================================================================================*/
    // 保存一个long值
    public static void putLong(Context context, String key, long value) {
        SharedPreferences sp = getInstance(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    // 获取一个long值, 默认值是-1
    public static long getLong(Context context, String key) {
        return getLong(context, key, -1);
    }

    // 获取一个long值
    public static long getLong(Context context, String key, long defValue) {
        SharedPreferences sp = getInstance(context);
        return sp.getLong(key, defValue);
    }
}
