package com.hai.store.data;

import android.content.Context;
import android.util.Log;

import com.hai.store.bean.ClickInfo;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.PostRequest;

import java.util.List;

public class ReportLogic {
    public static String TAG = "ldl";

    /**
     * 上报展示时长和删除任务 或者其他人get上报
     */
    public static void report(Context context, String rtp_method, String url, boolean replace, long time, StringCallback stringCallback) {
        if (null != url) {
            if ("POST".equals(rtp_method)) {
                if (replace) {
                    if (url.contains("SZST_ST")) {
                        url = url.replace("SZST_ST", String.valueOf(time));
                    }
                }
                Log.d("ReportLogic", "POST : " + url);
                request(url, stringCallback);
            }
            if ("GET".equals(rtp_method)) {
                Log.d("ReportLogic", "GET : " + url);
                if (null == stringCallback) {
                    OkGo.<String>get(url)
                            .tag(url)
                            .execute(new StringCallback() {
                                @Override
                                public void onSuccess(Response<String> response) {
                                    Log.d("ReportLogic", "onSuccess : " + response.body());
                                }

                                @Override
                                public void onError(Response<String> response) {
                                    super.onError(response);
                                    Log.d("ReportLogic", "onError : " + response.body());
                                }
                            });
                } else {
                    OkGo.<String>get(url)
                            .tag(url)
                            .execute(stringCallback);
                }
            }
        }
    }

    /**
     * 上流上报接口
     * replace 0/1,
     * info 点击信息
     */
    public static void report(Context context, String rtp_method, List<String> urlList, int replace, ClickInfo info) {
        if (null != urlList) {
            if ("GET".equals(rtp_method)) {
                for (String url : urlList) {
                    Log.d("ReportLogic", "GET url : " + url);
                    OkGo.<String>get(url)
                            .tag(url)
                            .execute(new StringCallback() {
                                @Override
                                public void onSuccess(Response<String> response) {
                                    Log.d("ReportLogic", "onSuccess : " + response.body());
                                }

                                @Override
                                public void onError(Response<String> response) {
                                    super.onError(response);
                                    Log.d("ReportLogic", "onError : " + response.body());
                                }
                            });
                }
            }
            if ("POST".equals(rtp_method)) {
                if (info != null) {
                    Log.d(TAG, "我要开始上报的点击坐标x:" + info.x + "y:" + info.y);
                }

                if (0 == replace) {
                    noReplace(context, urlList, null);
                    Log.d(TAG, "走到这里不替换宏也就不上报坐标......");
                    return;
                }
                if (1 == replace) {
                    replace(context, urlList, info, null);
                    Log.d(TAG, "母猪的产后护理...");
                    if (info != null) {
                        Log.d(TAG, "上报点击坐标sucess了x:" + info.x + "y:" + info.y);
                    }

                }
            }
        }
    }

    /**
     * 不需要替换
     */
    private static void noReplace(Context context, List<String> urlList, StringCallback stringCallback) {
        for (String url : urlList) {
            if (url.contains("SZST_TS")) {
                url = url.replace("SZST_TS", String.valueOf(System.currentTimeMillis()));
            }
            Log.d("ReportLogic", "POST url : " + url);
            request(url, stringCallback);
        }
    }

    /**
     * 需要替换
     */
    private static void replace(Context context, List<String> urlList, ClickInfo info, StringCallback stringCallback) {
        for (String url : urlList) {
            if (null != info) {
                if (url.contains("SZST_DX")) {
                    url = url.replace("SZST_DX", String.valueOf(info.x));
                }
                if (url.contains("SZST_DY")) {
                    url = url.replace("SZST_DY", String.valueOf(info.y));
                }
                if (url.contains("SZST_UX")) {
                    url = url.replace("SZST_UX", String.valueOf(info.x));
                }
                if (url.contains("SZST_UY")) {
                    url = url.replace("SZST_UY", String.valueOf(info.y));
                }
            }
            if (url.contains("SZST_TS")) {
                url = url.replace("SZST_TS", String.valueOf(System.currentTimeMillis()));
            }
            Log.d("ReportLogic", "POST url : " + url);
            request(url, stringCallback);
        }
    }

    private static void request(String url, StringCallback stringCallback) {
        String content = url.substring(url.indexOf("?") + 1);
        url = url.substring(0, url.indexOf("?"));
        String[] params = content.split("&");//方法要改善，不能用这个
        PostRequest<String> request = OkGo.<String>post(url)
                .tag(url);
        for (String param : params) {
            Log.d("ReportLogic", "params : " + param);
            int index = param.indexOf("=");//不用split，怕里面有好几个=
            if (index == -1) {
                continue;
            }
            try {
                String p = param.substring(index + 1);
                request.params(param.substring(0, index), p);
            } catch (StringIndexOutOfBoundsException e) {
                request.params(param.substring(0, index), "");
            }
        }
        if (null == stringCallback) {
            request.execute(new StringCallback() {
                @Override
                public void onSuccess(Response<String> response) {
                    Log.d("ReportLogic", "onSuccess : " + response.body());
                }

                @Override
                public void onError(Response<String> response) {
                    super.onError(response);
                    Log.d("ReportLogic", "onError : " + response.message());
                }
            });
        } else {
            request.execute(stringCallback);
        }
    }
}
