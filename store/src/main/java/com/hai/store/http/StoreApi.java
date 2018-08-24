package com.hai.store.http;


import android.content.Context;

import com.hai.store.base.SConstant;
import com.hai.store.utils.ApkUtils;
import com.hai.store.utils.Device;
import com.hai.store.utils.Utils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.request.PostRequest;

import java.util.List;
import java.util.Map;

public class StoreApi {

    /*
    * 请求洗包接口, 返回当前设备不存在的应用
    * */
    public static void requestRecommend(Context context, String cid, String tag, StringCallback stringCallback) {
        String url = SConstant.MARKET + SConstant.TYPE + SConstant.TYPE_RECOMMEND_AD;
        if (null != cid) {
            url = url + SConstant.CID + cid;
        }
        Map<String, String> deviceInfo = Device.getDeviceInfo(context);
        PostRequest<String> request = OkGo.<String>post(url)
                .tag(tag);
        for (String key : deviceInfo.keySet()) {
            if ("mac".equals(key)) {
                if ("".equals(deviceInfo.get(key))) {
                    request.params(key, Utils.getMacAddress(context));
                    continue;
                }
            }
            request.params(key, deviceInfo.get(key));
        }
        List<String> allApps = ApkUtils.scanAllInstallAppList(context);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < allApps.size(); i++) {
            if (i == allApps.size() - 1) {
                builder.append(allApps.get(i));
            } else {
                builder.append(allApps.get(i)).append(",");
            }
        }
        request.params(SConstant.APP_LIST, builder.toString());
        request.execute(stringCallback);
    }

    /*
    * 请求列表接口
    * */
    public static void requestAppList(Context context, String cid, String tag, StringCallback stringCallback) {
        String url = SConstant.MARKET + SConstant.TYPE + SConstant.TYPE_LIST;
        if (null != cid) {
            url = url + SConstant.CID + cid;
        }
        Map<String, String> deviceInfo = Device.getDeviceInfo(context);
        PostRequest<String> request = OkGo.<String>post(url)
                .tag(tag);
        for (String key : deviceInfo.keySet()) {
            if ("mac".equals(key)) {
                if ("".equals(deviceInfo.get(key))) {
                    request.params(key, Utils.getMacAddress(context));
                    continue;
                }
            }
            request.params(key, deviceInfo.get(key));
        }
        request.execute(stringCallback);
    }
}
