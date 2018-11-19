package com.hai.store.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

public class Utils {

    public static float dipToPixels(float dipValue, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    /**
     * 删除 文件
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    public static String versionName(String code) {
        return "版本: " + String.valueOf(code);
    }

    /**
     * 生成int随机数
     */
    public static int randomInt() {
        Random random = new Random();
        return random.nextInt(2147483647) + 1;
    }

    public static int getTimeForS() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public static String readableFileSize(String along) {
        Long size = Long.valueOf(along);
        if (size <= 0)
            return "0";
        final String[] units = new String[]{" B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String downloadNum(double num) {
        double i = num / 10000.00;
        BigDecimal b = new BigDecimal(i);
        double v = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        if (v > 10000.00) {
            i = v / 10000.00;
            BigDecimal bi = new BigDecimal(i);
            v = bi.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            return String.valueOf(v) + "亿人在用";
        } else {
            return String.valueOf(v) + "万人在用";
        }
    }

    public static String getAndroidRom(int i) {
        switch (i) {
            case 1:
                return "1.0+";
            case 2:
                return "1.1+";
            case 3:
                return "1.5+";
            case 4:
                return "1.6+";
            case 5:
                return "2.0+";
            case 6:
                return "2.0.1+";
            case 7:
                return "2.1+";
            case 8:
                return "2.2+";
            case 9:
                return "2.3.2+";
            case 10:
                return "2.3.4+";
            case 11:
                return "3.0+";
            case 12:
                return "3.1+";
            case 13:
                return "3.2+";
            case 14:
                return "4.0+";
            case 15:
                return "4.0.3+";
            case 16:
                return "4.1+";
            case 17:
                return "4.2+";
            case 18:
                return "4.3+";
            case 19:
                return "4.4+";
            case 20:
                return "4.4W+";
            case 21:
                return "5.0+";
            case 22:
                return "5.1+";
            case 23:
                return "6.0+";
            case 24:
                return "7.0+";
            default:
                break;
        }
        return "+";
    }

    public static String dateString(long dateTaken) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(new Date(dateTaken));
    }

    public static String map2Json(Map<String, String> map) {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        return gson.toJson(map);
    }

    public static String getMacAddress(Context context) {
        WifiManager wifiMng = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfor = wifiMng.getConnectionInfo();
        return wifiInfor.getMacAddress();
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) && activeNetwork.isConnected();
    }
}
