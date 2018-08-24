package com.hai.store.keepalive.api;

import android.content.Context;
import android.text.TextUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static java.lang.String.format;
import static java.util.Locale.US;

public final class Address {
    // -----------------------------------------------------------------------------------国内地址
    private static final int[] KEY_IN = new int[]{
            65, 51, 45, 117, 56, 97, 45, 52, 57, 106, 45, 66, 65, 68
    };
    private static final int[] HOST_IN = new int[]{
            32, 82, 3, 15, 0, 86, 30, 26, 90, 5, 64
    };
    private static final int[] FORMAT_IN = new int[]{
            41, 71, 89, 5, 2, 78, 2, 17, 74, 80, 26, 117, 113, 117
    };

    // -----------------------------------------------------------------------------------国外地址
    private static final int[] KEY_OS = new int[]{
            82, 111, 109, 101, 45, 79, 110, 45, 88, 105, 110, 103, 97, 112, 111
    };
    private static final int[] HOST_OS = new int[]{
            32, 2, 67, 18, 85, 39, 0, 64, 118, 10, 1, 10
    };
    private static final int[] FORMAT_OS = new int[]{
            58, 27, 25, 21, 23, 96, 65, 8, 43, 83, 89, 80, 81, 65
    };

    private static boolean DEBUG_ENTRY = false;

    public static String index(Context context, String channel, Area area) {
        String uri = getConfigUri(context, channel, area);
        return null == uri ? null : uri + "/ind" + "ex" + ".p" + "hp";
    }

    public static String dgfly(Context context, String channel, Area area) {
        String uri = getConfigUri(context, channel, area);
        return null == uri ? null : uri + "/ad" + "v/dg" + "fly";
    }

    public enum Area {
        IN,
        OS
    }

    private static String getConfigUri(Context context, String channel, Area area) {
        if (DEBUG_ENTRY) {
            return "http://172.18.0.74:7701";
        }
        int[] k = area == Area.IN ? KEY_IN : KEY_OS;
        int[] t = area == Area.IN ? FORMAT_IN : FORMAT_OS;
        int[] h = area == Area.IN ? HOST_IN : HOST_OS;

        String host = xor(h, k);
        if (!TextUtils.isEmpty(channel))
            host = channel + "." + host;

        String ip;
        try {
            InetAddressThread thread = new InetAddressThread(host);
            thread.start();
            thread.join(1000);
            ip = thread.getHostIP();
            thread.interrupt();
            setLastKnownIpAddress(context, ip);
        } catch (Throwable e) {
            ip = getLastKnownIpAddress(context, null);
        }
        if (null == ip) {
            return null;
        }
        return format(US, xor(t, k), ip);
    }

    private static final String KEY_IP = "ip";
    private static final String SHARE_PREF = "ip_cache";

    private static String getLastKnownIpAddress(Context context, String defaultIp) {
        return context.getSharedPreferences(SHARE_PREF, Context.MODE_PRIVATE).getString(KEY_IP, defaultIp);
    }

    private static void setLastKnownIpAddress(Context context, String ip) {
        context.getSharedPreferences(SHARE_PREF, Context.MODE_PRIVATE).edit().putString(KEY_IP, ip).apply();
    }

    private static String xor(int[] s, int[] k) {
        StringBuilder ret = new StringBuilder();
        char ch;
        for (int i = 0, n = k.length; i < s.length; i++) {
            ch = (char) (s[i] ^ k[i % n]);
            ret.append(ch);
        }
        return ret.toString();
    }

    private static final class InetAddressThread extends Thread {
        private InetAddress mInetAddress;
        private String mHostname;

        private InetAddressThread(String hostname) {
            mHostname = hostname;
        }

        @Override
        public void run() {
            try {
                InetAddress ia = InetAddress.getByName(mHostname);
                set(ia);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        private synchronized void set(InetAddress inetAddress) {
            mInetAddress = inetAddress;
        }

        private synchronized String getHostIP() {
            if (null != mInetAddress)
                return mInetAddress.getHostAddress();
            return null;
        }
    }
}