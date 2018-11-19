//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hai.store.data;

import android.text.TextUtils;
import android.util.Log;

import com.hai.store.Application;
import com.hai.store.base.SConstant;
import com.hai.store.utils.Device;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.request.PostRequest;

import java.util.Iterator;
import java.util.Map;

public class SearchLogic {
    private static final String TAG = "SearchLogic";
    private static final String SEARCH_TAG = "search";
    private static final String RECOMMEDN_TAG = "recommend_search";

    public SearchLogic() {
    }

    public static void getSearchContent(String market, String search, StringCallback callback) {
        String url;
        if (TextUtils.isEmpty(market)) {
            url = SConstant.MARKET+SConstant.TYPE+SConstant.TYPE_SEARCH+SConstant.SEARCH +search+ SConstant.CID + SConstant.CID_FOUND.CID_APP_SEARCHRESULT;
            Log.d("ldl", "market为null直接不传");
        } else {
            url =  SConstant.MARKET+SConstant.TYPE+SConstant.TYPE_SEARCH+SConstant.SEARCH + search + SConstant.CID + SConstant.CID_FOUND.CID_APP_SEARCHRESULT + "&market=" + market;
        }
        request(url, "search", callback);
    }

    public static void getHotSearch(String market, StringCallback callback) {
        String url;
        if(TextUtils.isEmpty(market)){
            url=SConstant.MARKET+SConstant.TYPE+SConstant.TYPE_LIST+SConstant.CID+"-30";
        }else{
            url=SConstant.MARKET+SConstant.TYPE+SConstant.TYPE_LIST+SConstant.CID+"-30"+SConstant.APP_MARKET+market;
        }
        request(url, "hot", callback);
    }

    public static void getRecommend(StringCallback callback) {
        String url=SConstant.MARKET+SConstant.TYPE+SConstant.TYPE_LIST+SConstant.CID+"-30"+SConstant.PAGE_SIZE+"5";
        request(url, "recommend_search", callback);
    }

    public static void stopSearch() {
        OkGo.getInstance().cancelTag("search");
    }

    public static void stopRecommend() {
        OkGo.getInstance().cancelTag("recommend_search");
    }

    private static void request(String url, String tag, StringCallback callback) {
        Map<String, String> deviceInfo = Device.getDeviceInfo(Application.getContext());
        PostRequest<String> request = (PostRequest) OkGo.post(url).tag(tag);
        Iterator var5 = deviceInfo.keySet().iterator();

        while (var5.hasNext()) {
            String key = (String) var5.next();
            request.params(key, (String) deviceInfo.get(key), new boolean[0]);
        }

        request.execute(callback);
    }
}
