package com.hai.store.utils;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.android.internal.telephony.IPhoneSubInfo;
import com.hai.store.data.ReportLogic;

import java.lang.reflect.Constructor;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static android.content.Context.TELEPHONY_SERVICE;

public class Device {
    private static final String KEY_YUNOS_VERSION = "yosv";
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = Device.class.getSimpleName();
    private static String CP = null;
    private static String SP_NAME = "p";

    private Device() {
    }

    public static void setPluginSharePreferenceName(String fn) {
        synchronized (Device.class) {
            SP_NAME = fn;
        }
    }

    private static String getPluginSharePreferenceName() {
        synchronized (Device.class) {
            return SP_NAME;
        }
    }

    public static final String KEY_CP = "cp"; // CP的编号

    public static void setForceCp(String cp) {
        synchronized (Device.class) {
            CP = cp;
        }
    }

    public static String getCp(Context context) {
        return getChannel(context);
    }

    private static String getChannel(Context context) {
        String name = "UMENG_CHANNEL";
        String value = "N/A";
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            value = appInfo.metaData.getString(name);
        } catch (Exception e) {
            e.printStackTrace();
            return value;
        }
        return value;
    }

    public static boolean hasPermission(Context context, String permission) {
        return PackageManager.PERMISSION_GRANTED == context.checkCallingOrSelfPermission(permission);
    }

    public static boolean isSystemApp(Context context, String packageName) {
        return isInRom(context, packageName);
    }

    public static boolean hasApplication(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        boolean ret = true;
        try {
            pm.getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            ret = false;
        }
        return ret;
    }

    @SuppressLint("NewApi")
    public static String getDefaultUserAgent(Context context) {
        if (Build.VERSION.SDK_INT >= 17) {
            try {
                return WebSettings.getDefaultUserAgent(context);
            } catch (Exception e) {
                return System.getProperty("http.agent");
            }
        }

        try {
            Constructor<WebSettings> constructor = WebSettings.class.getDeclaredConstructor(Context.class,
                    WebView.class);
            constructor.setAccessible(true);
            try {
                WebSettings settings = constructor.newInstance(context, null);
                return settings.getUserAgentString();
            } finally {
                constructor.setAccessible(false);
            }
        } catch (Exception e) {
            return new WebView(context).getSettings().getUserAgentString();
        }

    }

    public static String getDefaultBrowsePackageName(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        Uri uri = Uri.parse("http://");
        intent.setDataAndType(uri, null);

        List<ResolveInfo> l = pm.queryIntentActivities(intent, 0);
        if (null != l) {
            for (ResolveInfo ri : l) {
                if ("com.sohu.newsclient".equals(ri.activityInfo.packageName))
                    continue;
                return ri.activityInfo.packageName;
            }
        }
        return null;
    }

    public static boolean openBrowser(Context context, String homeUri) {
        String pkgName = getDefaultBrowsePackageName(context);
        if (null == pkgName) {
            if (DEBUG)
                Log.e(LOG_TAG, "Device.openBrowser can not finad browser package");
            return false;
        }
        Intent i = new Intent();
        Uri uri = Uri.parse(homeUri);
        i.setData(uri).setAction(Intent.ACTION_VIEW).setPackage(pkgName).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PackageManager pm = context.getPackageManager();
        ResolveInfo ri = pm.resolveActivity(i, 0);
        if (ri == null) {
            if (DEBUG)
                Log.e(LOG_TAG, "Device.openBrowser can not resolve intent=" + i);
            return false;
        }
        try {
            context.startActivity(i);
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(LOG_TAG, "" + e);
            }
            return false;
        }
        if (DEBUG)
            Log.e(LOG_TAG, "Device.openBrowser uri=" + homeUri);
        return true;
    }

    public static String getLabel(Context context) {
        PackageManager pm = context.getPackageManager();
        return context.getApplicationInfo().loadLabel(pm).toString();
    }

    public static String getSerialNumber() {
        return Build.SERIAL;
    }

    private static final String KEY_RELEASE = "rel"; // ROM版本
    private static final String KEY_SDK = "sdk"; // SDK INT
    private static final String KEY_SDK_INT = "sdki"; // SDK INT
    private static final String KEY_LANGUAGE = "l"; // 语言
    private static final String KEY_COUNTRY = "cc"; // 国家
    private static final String KEY_BUILD_ID = "bid"; // 构建ID
    private static final String KEY_BUILD_DISPLAY = "bdsp"; // 构建ID
    private static final String KEY_MANUFACTURER = "mfr"; // 生产商
    private static final String KEY_BRAND = "brnd"; // 手机品牌
    private static final String KEY_MODEL = "mdl"; // 手机型号
    private static final String KEY_DEVICE_ID = "did"; // 设备ID
    private static final String KEY_SIM_SERIAL_NUMBER = "ssn"; // SIM序列号
    private static final String KEY_SIM_SERIAL_NUMBER2 = "ssn2"; // SIM2序列号
    private static final String KEY_SUBSCRIBER_ID = "si"; // 用户ID
    private static final String KEY_SUBSCRIBER2_ID = "si2"; // 用户ID
    private static final String KEY_NETWORK_OPERATOR = "no"; // 运营商
    private static final String KEY_ANDROID_ID = "aid"; // 安卓ID，谷歌说是唯一的
    private static final String KEY_NETWORK = "n"; // 网络类型wifi,3G等
    private static final String KEY_APN = "apn"; // APN
    private static final String KEY_NETWORK_TYPE = "nt"; // 网络类型
    private static final String KEY_MAC = "mac";
    private static final String KEY_DISPLAY_METRICS = "dm"; // 屏幕尺寸 宽x高
    private static final String KEY_ROM = "rom"; // ROM /data大小
    // 剩余KB|总计KB
    private static final String KEY_ROM_SYS = "sys"; // ROM /system大小
    // 剩余KB|总计KB
    private static final String KEY_APP_NAME = "an"; // app packageName
    private static final String KEY_APP_VERSION = "av"; // app版本
    private static final String KEY_APP_VERSION_CODE = "avc"; // app版本
    private static final String KEY_PLUGIN_APK_NAME = "pan"; // Plugin文件名
    private static final String KEY_IS_IN_ROM = "iir";
    private static final String KEY_HAS_INSTALL_PACKAGE_PERMISSION = "hipp";
    private static final String KEY_HAS_DELETE_PACKAGE_PERMISSION = "hdpp";
    private static final String KEY_HAS_SDCARD = "sdc";
    private static final String KEY_REFER = "refer";
    private static final String KEY_LAUNCHED = "lch";

    @SuppressWarnings("deprecation")
    public static Map<String, String> getDeviceInfo(Context context) {
        Map<String, String> ret = new TreeMap<String, String>();
        ret.put(KEY_CP, getCp(context));
        String verName = "1.0.1";
        String verCode = "1";
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            verName = pi.versionName;
            verCode = Integer.toString(pi.versionCode);
        } catch (NameNotFoundException e1) {
        }
        ret.put(KEY_APP_VERSION, verName);
        ret.put(KEY_APP_VERSION_CODE, verCode);
        String pluginApkName = getCurrentApkName(context);
        if (DEBUG) {
            Log.e(LOG_TAG, "Device.getDeviceInfo pluginApkName=" + pluginApkName);
        }
        if (null != pluginApkName) {
            ret.put(KEY_PLUGIN_APK_NAME, pluginApkName);
        }
        String refer = getRefer(context);
        if (refer != null) {
            ret.put(KEY_REFER, refer);
        }
        String launched = getLaunched(context);
        if (null != launched) {
            ret.put(KEY_LAUNCHED, launched);
        }
        ret.put(KEY_IS_IN_ROM, Boolean.toString(isInRom(context)));
        ret.put(KEY_HAS_INSTALL_PACKAGE_PERMISSION,
                Boolean.toString(hasPermission(context, permission.INSTALL_PACKAGES)));
        ret.put(KEY_HAS_DELETE_PACKAGE_PERMISSION, Boolean.toString(hasPermission(context, permission.DELETE_PACKAGES)));
        ret.put(KEY_APP_NAME, context.getPackageName());
        ret.put(KEY_RELEASE, Build.VERSION.RELEASE);
        ret.put(KEY_SDK, Build.VERSION.CODENAME);
        ret.put(KEY_SDK_INT, "" + Build.VERSION.SDK_INT);
        ret.put(KEY_LANGUAGE, Locale.getDefault().getLanguage());
        ret.put(KEY_COUNTRY, Locale.getDefault().getCountry());
        ret.put(KEY_BUILD_ID, Build.ID);
        ret.put(KEY_BUILD_DISPLAY, Build.DISPLAY);
        ret.put(KEY_MANUFACTURER, Build.MANUFACTURER);
        ret.put(KEY_BRAND, Build.BRAND);
        ret.put(KEY_MODEL, Build.MODEL);
        ret.put(KEY_ANDROID_ID, getAid(context));
        ret.put(KEY_HAS_SDCARD,
                Boolean.toString(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)));
        DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
        ret.put(KEY_DISPLAY_METRICS, "" + dm.widthPixels + "x" + dm.heightPixels);
        StatFs sf = new StatFs(Environment.getDataDirectory().getPath());
        long blockSize = sf.getBlockSize();
        long blockCount = sf.getBlockCount();
        long availCount = sf.getAvailableBlocks();
        long total = blockSize * blockCount / 1024;
        long free = blockSize * availCount / 1024;
        ret.put(KEY_ROM, "" + free + "|" + total);
        sf = new StatFs(Environment.getRootDirectory().getPath());
        blockSize = sf.getBlockSize();
        blockCount = sf.getBlockCount();
        availCount = sf.getAvailableBlocks();
        total = blockSize * blockCount / 1024;
        free = blockSize * availCount / 1024;
        ret.put(KEY_ROM_SYS, "" + free + "|" + total);
        if (hasPermission(context, permission.READ_PHONE_STATE)) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
            ret.put(KEY_DEVICE_ID, tm.getDeviceId());
            ret.put(KEY_SIM_SERIAL_NUMBER, tm.getSimSerialNumber());
            ret.put(KEY_SIM_SERIAL_NUMBER2, getSim2SerialNo());
            ret.put(KEY_SUBSCRIBER_ID, tm.getSubscriberId());
            ret.put(KEY_SUBSCRIBER2_ID, getSubscriber2Id());
            ret.put(KEY_NETWORK_OPERATOR, tm.getNetworkOperator());
        }
        if (hasPermission(context, permission.ACCESS_NETWORK_STATE)) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (null != ni) {
                ret.put(KEY_NETWORK, ni.getTypeName());
                ret.put(KEY_APN, ni.getExtraInfo());
                ret.put(KEY_NETWORK_TYPE, getNetworkType(context, ni));
            }
        }
        if (hasPermission(context, permission.ACCESS_WIFI_STATE)) {
            WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = null;
            if (null != wifiMgr) {
                info = wifiMgr.getConnectionInfo();
            }
            ret.put(KEY_MAC, (null == info) ? null : info.getMacAddress());
        }

        if (ret.get(KEY_MAC) == null) {
            String wlanMac = getMACAddress("wlan0");
            if (wlanMac != null) {
                ret.put(KEY_MAC, wlanMac);
            } else {
                ret.put(KEY_MAC, getMACAddress("eth0"));
            }
        }

        String yunosVesion = SystemProperties.get("ro.yunos.version");
        if (!TextUtils.isEmpty(yunosVesion)) {
            ret.put(KEY_YUNOS_VERSION, yunosVesion);
        }

        ret.put("serialno", Device.getSerialNumber());
        ret.put("screen_density", String.valueOf(Device.getDip(context)));

        return ret;
    }

    public static final int NETWORK_TYPE_UNKNOWN = 0;
    public static final int NETWORK_TYPE_GPRS = 1;
    public static final int NETWORK_TYPE_EDGE = 2;
    public static final int NETWORK_TYPE_UMTS = 3;
    public static final int NETWORK_TYPE_CDMA = 4;
    public static final int NETWORK_TYPE_EVDO_0 = 5;
    public static final int NETWORK_TYPE_EVDO_A = 6;
    public static final int NETWORK_TYPE_1xRTT = 7;
    public static final int NETWORK_TYPE_HSDPA = 8;
    public static final int NETWORK_TYPE_HSUPA = 9;
    public static final int NETWORK_TYPE_HSPA = 10;
    public static final int NETWORK_TYPE_IDEN = 11;
    public static final int NETWORK_TYPE_EVDO_B = 12;
    public static final int NETWORK_TYPE_LTE = 13;
    public static final int NETWORK_TYPE_EHRPD = 14;
    public static final int NETWORK_TYPE_HSPAP = 15;

    private static String getNetworkType(Context ctx, NetworkInfo ni) {
        int type = ni.getType();
        try {
            if (type == ConnectivityManager.TYPE_MOBILE) {
                TelephonyManager tm = (TelephonyManager) ctx.getSystemService(TELEPHONY_SERVICE);
                type = tm.getNetworkType();
                switch (type) {
                    case NETWORK_TYPE_GPRS:
                    case NETWORK_TYPE_EDGE:
                    case NETWORK_TYPE_CDMA:
                    case NETWORK_TYPE_1xRTT:
                    case NETWORK_TYPE_IDEN:
                        return "2g";
                    case NETWORK_TYPE_UMTS:
                    case NETWORK_TYPE_EVDO_0:
                    case NETWORK_TYPE_EVDO_A:
                    case NETWORK_TYPE_HSDPA:
                    case NETWORK_TYPE_HSUPA:
                    case NETWORK_TYPE_HSPA:
                    case NETWORK_TYPE_EVDO_B:
                    case NETWORK_TYPE_EHRPD:
                    case NETWORK_TYPE_HSPAP:
                        return "3g";
                    case NETWORK_TYPE_LTE:
                        return "4g";
                }
            } else if (type == ConnectivityManager.TYPE_WIFI) {
                return "wifi";
            }
        } catch (Throwable t) {
            if (DEBUG) {
                Log.e(LOG_TAG, "" + t);
            }
        }
        return "na_" + type;
    }

    private static String getLaunched(Context context) {
        SharedPreferences sp = context.getSharedPreferences("plugin", Context.MODE_PRIVATE);
        return sp.getString("lnchd", null);
    }

    public static String getRefer(Context context) {// 获取google referrer
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences("plugin", Context.MODE_PRIVATE);
        return sp.getString("refer", null);
    }

    private static final boolean isInRom(Context context) {
        return isInRom(context, context.getPackageName());
    }

    private static final boolean isInRom(Context context, String pkgName) {
        PackageManager pm = context.getPackageManager();
        boolean ret = false;
        try {
            ret = (ApplicationInfo.FLAG_SYSTEM & pm.getPackageInfo(pkgName, PackageManager.GET_PERMISSIONS).applicationInfo.flags) == ApplicationInfo.FLAG_SYSTEM;
        } catch (NameNotFoundException e) {
            ret = false;
        }
        return ret;
    }

    private static String getSim2SerialNo() {
        String ret = null;
        try {
            IPhoneSubInfo i = IPhoneSubInfo.Stub.asInterface(ServiceManager.getService("iphonesubinfo2"));
            if (null != i) {
                ret = i.getIccSerialNumber(null);
            }
        } catch (Throwable ex) {
        }
        return ret;
    }

    private static String getSubscriber2Id() {
        String ret = null;
        try {
            IPhoneSubInfo i = IPhoneSubInfo.Stub.asInterface(ServiceManager.getService("iphonesubinfo2"));
            if (null != i) {
                ret = i.getSubscriberId(null);
            }
        } catch (Throwable ex) {
        }
        return ret;
    }

    public static final int HEIGHT = 1;
    public static final int WIDTH = 0;

    public static int[] getScreenSize(Context context) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        int[] size = {width, height};
        return size;
    }

    public static String getAid(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static boolean isNetworkConnected(Context context) {
        if (!hasPermission(context, permission.INTERNET)) {
            return false;
        }
        if (!hasPermission(context, permission.ACCESS_NETWORK_STATE)) {
            if (DEBUG)
                Log.e(LOG_TAG, "no " + permission.ACCESS_NETWORK_STATE);
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return null != ni && ni.isConnected();
    }

    public static boolean isExternalStorageMounted(Context context) {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private static final String APK_NAME = "an";

    private static String getCurrentApkName(Context context) {
        String fn = getPluginSharePreferenceName();
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(fn, Context.MODE_PRIVATE);
        String name = sp.getString(APK_NAME, null);
        return name;
    }

    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName))
                        continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null)
                    return "";
                StringBuilder buf = new StringBuilder();
                for (int idx = 0; idx < mac.length; idx++)
                    buf.append(String.format("%02X:", mac[idx]));
                if (buf.length() > 0)
                    buf.deleteCharAt(buf.length() - 1);
                return buf.toString();
            }
        } catch (Exception ex) {
        }
        return "";
    }

    /*================getIP start==============*/
    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                         en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//wifi
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
                return intIP2StringIP(dhcpInfo.ipAddress);//得到IPV4地址
            }
        } else {
            //当前无网络连接,需要打开网络
        }
        return "";
    }

    private static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }
    /*================getIP end==============*/

    public static String getScreenDPI(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return String.valueOf(dm.densityDpi);
    }

    public static float getDip(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.density;
    }

    public static int getWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int getHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    public static int dp2px(Context context, int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)((float)dp * scale + 0.5F);
    }

}
