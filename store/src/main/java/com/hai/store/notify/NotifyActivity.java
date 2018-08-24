package com.hai.store.notify;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hai.store.R;
import com.hai.store.activity.DetailActivity;
import com.hai.store.bean.DmBean;
import com.hai.store.bean.StoreADInfo;
import com.hai.store.data.DownloadLogic;
import com.hai.store.data.ReportLogic;
import com.hai.store.keepalive.api.Address;
import com.hai.store.keepalive.api.Api;
import com.hai.store.sqlite.PublicDao;
import com.hai.store.utils.ApkUtils;
import com.hai.store.utils.Device;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONException;
import org.json.JSONObject;

import static android.text.TextUtils.isEmpty;
import static android.view.Gravity.TOP;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
import static com.hai.store.base.SConstant.APP_NAME;
import static com.hai.store.base.SConstant.PKG_NAME;

public class NotifyActivity extends Activity {

    private String TAG = "NOTIFY_ACTIVITY";
    private ImageView mIcon;
    private TextView mTitle, mContent;
    private RelativeLayout mRoot;
    private StoreADInfo mInfo;
    private Handler mHandler = new Handler();
    public static boolean VISIBLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);
        VISIBLE = true;
        Window window = getWindow();//在setContentView之前则可以全屏
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = MATCH_PARENT;
        params.height = WRAP_CONTENT;
        params.flags = FLAG_NOT_TOUCH_MODAL;
        params.windowAnimations = 0;
        params.gravity = TOP;
        findView();
        loadData();
    }

    private void loadData() {
        Api.getInstance().fetchConfig(Address.Area.IN, new com.hai.store.keepalive.api.Callback<StoreADInfo>() {
            @Override
            public void onFinish(boolean isSuccess, final StoreADInfo adConfig, int code, Object tag) {

                Log.e(TAG, "handlerData onFinish, isSuccess = " + isSuccess + ", adConfig = " + adConfig);
                if (!isSuccess && adConfig == null) {
                    finish();
                    return;
                }

                if (ApkUtils.DOWNLOAD != ApkUtils.checkNeedDownload(NotifyActivity.this, adConfig.apk, Integer.valueOf(adConfig.versioncode))) {
                    Log.e(TAG, "本机已存在此应用，不再展示");
                    finish();
                    return;
                }
                Log.e(TAG, "show_type = " + adConfig.show_type);
                if (null != adConfig.show_type && adConfig.show_type.endsWith("bb_notify_app")) {
                    mInfo = adConfig;
                    Log.e(TAG, "buildMessage");
                    loadIconImage();
                }
            }
        });
    }

    private void loadIconImage() {
        ImageLoader imageLoader = ImageLoader.getInstance();
        if (mInfo.icon_img == null) {
            Log.e(TAG, "icon_url == null");
            return;
        }
        imageLoader.loadImage(mInfo.icon_img, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTitle.setText(getNotifyTitle());
                        mContent.setText(getNotifyContent());
                        mIcon.setImageBitmap(loadedImage);
                        mRoot.setVisibility(View.VISIBLE);
                        NotifyController.getInstance().setSuccessDisplay();
                        ReportLogic.report(NotifyActivity.this, "POST", mInfo.s_rpt, 0, null);
                        mRoot.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                buildIntent();
                                finish();
                            }
                        });
                    }
                });
                updateFetch();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 20000);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                super.onLoadingFailed(imageUri, view, failReason);

            }
        });
    }

    private void buildIntent() {
//        ReportLogic.report(this, "POST", mInfo.d_rpt, 0, null);
//        startDown();
        ReportLogic.report(this, "POST", mInfo.c_rpt, 0, null);
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(PKG_NAME, mInfo.href);
        bundle.putString(APP_NAME, mInfo.name);
        intent.putExtra(DetailActivity.DETAIL, bundle);
        intent.setClass(this, DetailActivity.class);
        startActivity(intent);
    }

    private String getNotifyTitle() {
        return "【有人@你】" + mInfo.name + " 送来惊喜";
    }

    private String getNotifyContent() {
        return " 精彩内容一刷就有！";
    }

    private void startDown() {
        DownloadLogic.getInstance().startDownload(this, mInfo.down_url, mInfo.name, mInfo.appid, mInfo.icon_img,
                mInfo.apk, mInfo.versioncode, mInfo.dc_rpt, null, "POST");
        PublicDao.insert(buildDmBean(mInfo));
    }

    private DmBean buildDmBean(StoreADInfo info) {
        DmBean dmBean = new DmBean();
        dmBean.packageName = info.apk;
        dmBean.appId = info.appid;
        dmBean.appName = info.name;
        dmBean.iconUrl = info.icon_img;
        dmBean.downUrl = info.down_url;
        dmBean.size = info.size;
        dmBean.versionCode = info.versioncode;
        dmBean.versionName = info.versionname;
        dmBean.repDc = info.dc_rpt;
        dmBean.repInstall = info.i_rpt;
        dmBean.repAc = info.a_rpt;
        dmBean.method = "POST";
        return dmBean;
    }

    private void updateFetch() {
        String url = Address.index(this, Device.getCp(this), Address.Area.IN);
        Log.e(TAG, "URL = " + url);
        OkGo.<String>get(url)
                .tag("GrayService")
                .params("prcs", "yt2")
                .params(Device.getDeviceInfo(this))
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        String body = response.body();
                        if (!isEmpty(body)) {
                            try {
                                JSONObject root = new JSONObject(body);
                                Long interval = root.optLong("ri", 60 * 1000);
                                JSONObject notify = root.getJSONObject("cnf").getJSONObject("yt2").getJSONObject("notify");
                                NotifyController.getInstance().setFetchInterval(interval);
                                NotifyController.getInstance().setLastFetchTime();
                                NotifyController.getInstance().saveConfig(notify);
                            } catch (JSONException e) {
                                Log.e(TAG, "notify configure parse wrong--" + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        Log.d("ReportLogic", "onError : " + response.body());
                    }
                });
    }

    private void findView() {
        mIcon = (ImageView) findViewById(R.id.notify_ac_icon);
        mTitle = (TextView) findViewById(R.id.notify_ac_title);
        mContent = (TextView) findViewById(R.id.notify_ac_content);
        mRoot = (RelativeLayout) findViewById(R.id.notify_ac_root);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VISIBLE = false;
    }
}
