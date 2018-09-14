package com.hai.store.data;

import com.hai.store.Application;
import com.hai.store.base.SConstant;
import com.hai.store.utils.Device;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.request.PostRequest;

import java.util.Map;

public class SearchLogic {

    private static final String TAG = "SearchLogic";
    private static final String SEARCH_TAG = "search";
    private static final String RECOMMEDN_TAG = "recommend_search";

    public static void getSearchContent(String search, StringCallback callback) {
        String url = SConstant.MARKET + SConstant.TYPE + SConstant.TYPE_SEARCH + SConstant.SEARCH + search;
        request(url, SEARCH_TAG, callback);
    }

    public static void getHotSearch(StringCallback callback) {
        String url = SConstant.MARKET + SConstant.TYPE + SConstant.TYPE_LIST + SConstant.CID + "-11";
        request(url, "hot", callback);
    }

    public static void getRecommend(StringCallback callback) {
        String url = SConstant.MARKET + SConstant.TYPE + SConstant.TYPE_LIST + SConstant.CID + "-13" + SConstant.PAGE_SIZE + 5;
        request(url, RECOMMEDN_TAG, callback);
    }

    public static void stopSearch() {
        OkGo.getInstance().cancelTag(SEARCH_TAG);
    }

    public static void stopRecommend() {
        OkGo.getInstance().cancelTag(RECOMMEDN_TAG);
    }

    private static void request(String url, String tag, StringCallback callback) {
        Map<String, String> deviceInfo = Device.getDeviceInfo(Application.getContext());
        PostRequest<String> request = OkGo.<String>post(url)
                .tag(tag);
        for (String key : deviceInfo.keySet()) {
            request.params(key, deviceInfo.get(key));
        }
        request.execute(callback);
    }
}
