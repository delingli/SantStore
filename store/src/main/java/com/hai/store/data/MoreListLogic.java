//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hai.store.data;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.hai.store.utils.Device;
import com.hai.store.utils.Utils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.request.PostRequest;

import java.util.Iterator;
import java.util.Map;

public class MoreListLogic {
    private static final String TAG = "MoreListLogic_getAppList";

    public MoreListLogic() {
    }

    public static void getAppList(Context context, String page, int cid, String market, StringCallback stringCallback, String tMode) {
        String url;
        if (TextUtils.isEmpty(page)) {
            if (TextUtils.isEmpty(market)) {
                Log.d("ldl", "market市场为null直接不传去取数据....");
                url = "http://adapi.yiticm.com:7701/market.php?type=list&cid=" + cid + "&page=" + 1 + "&tmode=" + tMode;
            } else {
                Log.d("ldl", "market市场不为null取数据...."+market);
                url = "http://adapi.yiticm.com:7701/market.php?type=list&cid=" + cid + "&page=" + 1 + "&tmode=" + tMode + "&market=" + market;
            }
        } else {
            url = page + "&tmode=" + tMode;
        }

        Map<String, String> deviceInfo = Device.getDeviceInfo(context);
        PostRequest<String> request = (PostRequest) OkGo.post(url).tag("MoreListLogic_getAppList");
        Iterator var9 = deviceInfo.keySet().iterator();

        while (true) {
            while (var9.hasNext()) {
                String key = (String) var9.next();
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
