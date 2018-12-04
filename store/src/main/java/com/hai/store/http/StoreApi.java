//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hai.store.http;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.hai.store.base.SConstant;
import com.hai.store.utils.ApkUtils;
import com.hai.store.utils.Device;
import com.hai.store.utils.Utils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.request.PostRequest;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StoreApi {
    public StoreApi() {
    }

    public static void requestRecommend(Context context, String tag, String nextcur, StringCallback stringCallback) {
        String url = SConstant.MARKET + SConstant.TYPE + SConstant.TYPE_RECOMMEND_AD;
        url = url + "&nextcur=" + nextcur;

        Map<String, String> deviceInfo = Device.getDeviceInfo(context);
        PostRequest<String> request = (PostRequest) OkGo.post(url).tag(tag);
        Iterator var8 = deviceInfo.keySet().iterator();

        while (true) {
            while (var8.hasNext()) {
                String key = (String) var8.next();
                if ("mac".equals(key) && "".equals(deviceInfo.get(key))) {
                    request.params(key, Utils.getMacAddress(context), new boolean[0]);
                } else {
                    request.params(key, (String) deviceInfo.get(key), new boolean[0]);
                }
            }
            request.execute(stringCallback);
            return;
        }
    }

    public static void requestAppList(Context context, String cid, String tag, String market, StringCallback stringCallback) {
        String url;
        if (TextUtils.isEmpty(market)) {
            url = SConstant.MARKET + SConstant.TYPE + SConstant.TYPE_LIST;
        } else {
            url = SConstant.MARKET + SConstant.TYPE + SConstant.TYPE_LIST + SConstant.APP_MARKET + market;
        }
        if (null != cid) {
            url = url + "&cid=" + cid;
        }

        Map<String, String> deviceInfo = Device.getDeviceInfo(context);
        PostRequest<String> request = (PostRequest) OkGo.post(url).tag(tag);
        Iterator var7 = deviceInfo.keySet().iterator();

        while (true) {
            while (var7.hasNext()) {
                String key = (String) var7.next();
                if ("mac".equals(key) && "".equals(deviceInfo.get(key))) {
                    request.params(key, Utils.getMacAddress(context), new boolean[0]);
                } else {
                    request.params(key, (String) deviceInfo.get(key), new boolean[0]);
                }
            }

            request.execute(stringCallback);
            return;
        }
    }
}
