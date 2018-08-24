package com.hai.store.data;

import android.content.Context;
import android.util.Log;

import com.hai.store.utils.Device;
import com.hai.store.utils.Utils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.request.PostRequest;

import java.util.Map;

public class DetailLogic {

    public static void getAppDetail(Context context, String url, StringCallback stringCallback) {
        Map<String, String> deviceInfo = Device.getDeviceInfo(context);
        Log.d("DETAIL_RUL", "getAppDetail = " + url);
        PostRequest<String> request = OkGo.<String>post(url)
                .tag("DetailLogic_getAppDetail");
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
