package com.hai.store.data;

import android.content.Context;

import com.hai.store.base.SConstant;
import com.hai.store.utils.Device;
import com.hai.store.utils.Utils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.request.PostRequest;

import java.util.Map;

public class MoreListLogic {

    private static final String TAG = "MoreListLogic_getAppList";

    public static void getAppList(Context context, String page, StringCallback stringCallback, String tMode) {
        String url;
        if (null == page) {
            url = SConstant.MARKET + SConstant.TYPE + SConstant.TYPE_LIST + SConstant.CID
                    + SConstant.CID_FOUND.CID_APP_LIST + SConstant.PAGE + 1 + SConstant.TMODE + tMode;
        } else {
            url = page + SConstant.TMODE + tMode;
        }
        Map<String, String> deviceInfo = Device.getDeviceInfo(context);
        PostRequest<String> request = OkGo.<String>post(url)
                .tag(TAG);
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
