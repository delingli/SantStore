package com.hai.store.notify;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;

public class NotifyController {
    // preference keys
    private static final String KEY_DISPLAY_DELAY = "6F5G478D4F5G7R5";
    private static final String KEY_MAX_DISPLAY_COUNT = "E698Q5W1E2S4D5F";
    private static final String KEY_CURRENT_DISPLAY_COUNT = "ZZ2X4A6S5D4D5W78";
    private static final String KEY_DISPLAY_HOUR_START = "E3Z2D4A5S8E7R4Q2";
    private static final String KEY_DISPLAY_HOUR_END = "A88ER7D554A2S47";
    private static final String KEY_POPUP_INTERVAL = "S6F79W5E4R78D3A";
    private static final String KEY_LAST_FETCH_TIME="SA5Q7WQE7A9S87D";
    private static final String KEY_FETCH_INTERVAL ="C2F4A5S7Q8E9R75";

    private static final String KEY_LAST_POPUP_MILLS = "SA7E43Q2W487E6R2";// 每次成功弹窗的时间戳

    private static final String PREF = "AS246Q3W1E2A1S54";
    private static final boolean DEBUG = true;
    private static final String TAG = "NotifyController";

    private volatile static NotifyController INSTANCE;

    private SharedPreferences mPref;// 配置文件

    public static NotifyController getInstance() {
        if (null == INSTANCE) synchronized (NotifyController.class) {
            if (null == INSTANCE) INSTANCE = new NotifyController();
        }
        return INSTANCE;
    }

    public void init(Context context) {
        // 初始化配置文件
        mPref = context.getSharedPreferences(PREF, MODE_PRIVATE);
    }

    private NotifyController() {}

    public Long getPopupInterval(){
        return mPref.getLong(KEY_POPUP_INTERVAL, 60*1000);
    }

    public void setFetchInterval(long lastInterval){
        mPref.edit().putLong(KEY_FETCH_INTERVAL,lastInterval).apply();
    }

    public Long getFetchInterval(){
        return mPref.getLong(KEY_FETCH_INTERVAL,60*1000);
    }

    public void setLastFetchTime(){
        mPref.edit().putLong(KEY_LAST_FETCH_TIME,System.currentTimeMillis()).apply();
    }

    public Long getLastFetchTime(){
        return mPref.getLong(KEY_LAST_FETCH_TIME,0);
    }

    private void setCurrentDisplayCount(int count, long ts) {
        mPref.edit().putInt(KEY_CURRENT_DISPLAY_COUNT, count).putLong(KEY_LAST_POPUP_MILLS, ts).apply();
    }

    private int getCurrentDisplayCount() {
        return mPref.getInt(KEY_CURRENT_DISPLAY_COUNT, 0);
    }

    private long getLastPopupMillis() {
        return mPref.getLong(KEY_LAST_POPUP_MILLS, 0);
    }

    private void setPopupConfig(PopupConfig pc) {
        if (DEBUG)
            Log.e(TAG, "setPopupConfig");
        mPref.edit().putInt(KEY_CURRENT_DISPLAY_COUNT, pc.currentDisplayCount)
                .putLong(KEY_LAST_POPUP_MILLS, pc.lastDisplayMillis).putInt(KEY_MAX_DISPLAY_COUNT, pc.maxDisplayCount)
                .putLong(KEY_DISPLAY_DELAY, pc.displayDelay).putInt(KEY_DISPLAY_HOUR_START, pc.displayHourStart)
                .putInt(KEY_DISPLAY_HOUR_END, pc.displayHourEnd).putLong(KEY_POPUP_INTERVAL, pc.interval).apply();
    }

    public PopupConfig getPopupConfig() {
        PopupConfig pc = new PopupConfig();
        pc.maxDisplayCount = mPref.getInt(KEY_MAX_DISPLAY_COUNT, -1);
        pc.currentDisplayCount = mPref.getInt(KEY_CURRENT_DISPLAY_COUNT, 0);
        pc.lastDisplayMillis = mPref.getLong(KEY_LAST_POPUP_MILLS, 0);
        pc.displayDelay = mPref.getLong(KEY_DISPLAY_DELAY, 5000);
        pc.displayHourStart = mPref.getInt(KEY_DISPLAY_HOUR_START, 0);
        pc.displayHourEnd = mPref.getInt(KEY_DISPLAY_HOUR_END, 23);
        pc.interval = mPref.getLong(KEY_POPUP_INTERVAL, 3600);
        if (pc.maxDisplayCount == -1) {
            return null;
        } else {
            return pc;
        }
    }

    public boolean shouldPopup() {

        long ts = System.currentTimeMillis();

        PopupConfig config = getPopupConfig();

        if (null == config) {
            if (DEBUG) {
                Log.e(TAG, "shouldPopup return false for no config");
            }
            return false;
        }
        Log.e(TAG, "toString = " + config.toString());
        long lastPopup = getLastPopupMillis();
        if (isDifferentDay(lastPopup, ts)) {
            config.currentDisplayCount = 0;
            config.lastDisplayMillis = 0;
        }
        Log.e(TAG, "last = " + lastPopup + ", config interval = " + config.interval);
        if (ts - lastPopup < config.interval) {
            if (DEBUG)
                Log.e(TAG, "shouldPopup return false for interval too short");
            return false;
        }
        if (config.currentDisplayCount >= config.maxDisplayCount) {
            if (DEBUG)
                Log.e(TAG, "shouldPopup return false for over display max count" + config.currentDisplayCount + "--" + config.maxDisplayCount);
            return false;
        }
        if (!isDisplayDuration(config, ts)) {
            if (DEBUG)
                Log.e(TAG, "shouldPopup return false for over display");
            return false;
        }
//        setCurrentDisplayCount(config.currentDisplayCount + 1, ts);
        if (DEBUG)
            Log.e(TAG, "shouldPopup : true");
        return true;
    }

    public void setSuccessDisplay(){
        PopupConfig config = getPopupConfig();
        if (null == config) {
            if (DEBUG)
                Log.e(TAG, "shouldPopup return false for no config");
            return;
        }
        setCurrentDisplayCount(config.currentDisplayCount + 1, System.currentTimeMillis());
    }
    private boolean isDisplayDuration(PopupConfig config, long ts) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ts);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int start = config.displayHourStart;
        int end = config.displayHourEnd;
        boolean ret = hour >= start && hour < end;
        if (DEBUG)
            Log.e(TAG, "isDisplayDuration " + ret);
        return ret;
    }

    private boolean isDifferentDay(long lastPopup, long ts) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ts);
        int today = c.get(Calendar.DAY_OF_YEAR);
        c = Calendar.getInstance();
        c.setTimeInMillis(lastPopup);
        int lastDay = c.get(Calendar.DAY_OF_YEAR);
        return today != lastDay;
    }

    public static class PopupConfig {

        int displayHourEnd; // 时间段结束
        int displayHourStart;// 时间段的开始
        int maxDisplayCount;// 一天最多显示的次数
        int currentDisplayCount;// 当天显示的次数
        long lastDisplayMillis; // 上次显示的时间
        public long displayDelay; // 显示延时
        long interval; // 下一次显示的间隔

        public static PopupConfig parseConfigJSON(JSONObject jo) {
            PopupConfig pc = null;
            try {
                if (DEBUG)
                    Log.e(TAG, "parse json " + jo.toString());
                pc = new PopupConfig();
                JSONObject popup = jo.getJSONObject("popup");
                pc.maxDisplayCount = popup.getInt("mdc");
                pc.displayDelay = popup.getLong("dd") * 1000;
                pc.displayHourStart = popup.getInt("st");
                pc.displayHourEnd = popup.getInt("et");
                pc.interval = popup.getLong("itv") * 1000;
            } catch (JSONException e) {
                if (DEBUG) {
                    Log.e(TAG, "error", e);
                }
            }
            return pc;
        }

        public void save() {
            this.currentDisplayCount = INSTANCE.getCurrentDisplayCount();// 获取配置时候,没有当前的展示次数,
            this.lastDisplayMillis = INSTANCE.getLastPopupMillis();
            INSTANCE.setPopupConfig(this);
        }

        @Override
        public String toString() {
            return "PopupConfig{" +
                    "displayHourEnd=" + displayHourEnd +
                    ", displayHourStart=" + displayHourStart +
                    ", maxDisplayCount=" + maxDisplayCount +
                    ", currentDisplayCount=" + currentDisplayCount +
                    ", lastDisplayMillis=" + lastDisplayMillis +
                    ", displayDelay=" + displayDelay +
                    ", interval=" + interval +
                    '}';
        }
    }

    public void saveConfig(JSONObject jo) {
        PopupConfig pc = PopupConfig.parseConfigJSON(jo);
        if (pc != null) {
            pc.save();
        }
    }

}