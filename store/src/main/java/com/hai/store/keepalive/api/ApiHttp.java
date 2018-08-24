package com.hai.store.keepalive.api;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.hai.store.bean.StoreADInfo;
import com.hai.store.utils.Device;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;


final class ApiHttp extends ApiAbs implements IApi {

    private String TAG = "ApiHttp";
    public static final int ERROR_PARSE_JSON = 411;
    public static final int ERROR_OPEN_CONNECT = 600;
    public static final int ERROR_RESPONSE_FAILED = 500;
    static final int TIMEOUT = 5000;
    private String defaultUserAgent;
    private Context mContext;

    ApiHttp(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void fetchConfig(final Address.Area area, final Callback<StoreADInfo> c) {
        String url = Address.dgfly(mContext, Device.getCp(mContext), area);
        if (TextUtils.isEmpty(url)) {
            url = Address.dgfly(mContext, Device.getCp(mContext), area);
        }
        if (null == url) {
            Log.e(TAG, "URL ==== null");
            return;
        }
        final String finalUrl = url;
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                String uri = finalUrl;
                defaultUserAgent = Device.getDefaultUserAgent(mContext);
                if (null != COMPANY && !COMPANY.isEmpty()) {
                    StringBuilder sb = new StringBuilder(finalUrl);
                    sb.append("?");
                    for (String key : COMPANY.keySet())
                        sb.append(key).append("=").append(COMPANY.get(key)).append("&");
                    sb.append("action").append("=").append("notify").append("&");
                    sb.append("pan").append("=");
                    if (Address.Area.IN == area) {
                        sb.append("par625tang");
                    }
                    if (Address.Area.OS == area) {
                        sb.append("pat625rome");
                    }
                    uri = sb.toString();
                }
                if (uri.contains(" ")) uri = uri.replace(" ", "");
                String result = fetchWithUrl(uri);
                try {
                    callback(c, Integer.valueOf(result));
                } catch (NumberFormatException e) {
                    try {
                        StoreADInfo config = null;
                        JSONObject root = new JSONObject(result);
                        JSONObject cnf = root.getJSONObject("cnf");
                        JSONObject dgFly = cnf.getJSONObject("dgfly");
                        String adType = dgFly.getString("adtype");
                        if ("bb".equals(adType)) {
                            config = StoreADInfo.getStoreADInfo(dgFly.toString());
                        }
                        callback(c, config);
                    } catch (JSONException ex) {
                        Log.i(TAG, "data is JSONException");
                        callback(c, ERROR_PARSE_JSON);
                    }
                }
            }
        });
    }

    @Override
    public void report(final String url) {
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(url))
                    request(url);
            }
        });
    }

    @Override
    public void report(String url, String bbEvent) {
        if (!TextUtils.isEmpty(bbEvent)) {
            url = url + "&bb_event=" + bbEvent;
        }
        report(url);
    }

    private synchronized String fetchWithUrl(String url) {
        HttpURLConnection conn = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setReadTimeout(TIMEOUT);
            conn.setConnectTimeout(TIMEOUT);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent", defaultUserAgent);
            if (conn.getResponseCode() == HTTP_MOVED_TEMP) {
                Log.e("BGN_HTTP", "HTTP_MOVED_TEMP == 302");
            }

            if (conn.getResponseCode() != HTTP_OK)
                return String.valueOf(ERROR_RESPONSE_FAILED);
            is = conn.getInputStream();
            baos = new ByteArrayOutputStream();
            int len;
            byte[] b = new byte[1024];
            while ((len = is.read(b)) != -1) {
                baos.write(b, 0, len);
            }
            baos.flush();
            return baos.toString();
        } catch (IOException e) {
            return String.valueOf(ERROR_OPEN_CONNECT);
        } finally {
            try {
                if (null != baos) baos.close();
                if (null != is) is.close();
                if (null != conn) conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void request(String url) {
        HttpURLConnection conn = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setReadTimeout(TIMEOUT);
            conn.setConnectTimeout(TIMEOUT);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            if (conn.getResponseCode() != HTTP_OK) {
                return;
            }
            is = conn.getInputStream();
            baos = new ByteArrayOutputStream();
            int len;
            byte[] b = new byte[1024];
            while ((len = is.read(b)) != -1) {
                baos.write(b, 0, len);
            }
            baos.flush();
            String result = baos.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != baos) baos.close();
                if (null != is) is.close();
                if (null != conn) conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
