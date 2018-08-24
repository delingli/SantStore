package com.hai.store.notify;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.hai.store.R;
import com.hai.store.activity.DetailActivity;
import com.hai.store.base.SConstant;
import com.hai.store.bean.StoreADInfo;
import com.hai.store.data.ReportLogic;
import com.hai.store.keepalive.api.Address;
import com.hai.store.keepalive.api.Api;
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

import java.util.Random;

import static android.text.TextUtils.isEmpty;

public class NotifyAppServer extends Service {

    private static final String TAG = "NotifyAppServer";

    private static final Random RANDOM = new Random();
    private NotificationManager mNM;
    private NotificationCompat.Builder mBuilder;
    private RemoteViews mRemoteView;
    private Handler handler = new Handler();
    private StoreADInfo mInfo;
    private int mCount; //请求次数

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mRemoteView = new RemoteViews(getPackageName(), R.layout.view_notify);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        handlerData();
        return super.onStartCommand(intent, flags, startId);
    }

    private void handlerData() {
        Log.d(TAG, "mCount : " + mCount);
        mCount++;
        if (mCount >= 5) {
            stopSelf();
            return;
        }
        Api.getInstance().fetchConfig(Address.Area.IN, new com.hai.store.keepalive.api.Callback<StoreADInfo>() {
            @Override
            public void onFinish(boolean isSuccess, final StoreADInfo adConfig, int code, Object tag) {

                Log.e(TAG, "handlerData onFinish, isSuccess = " + isSuccess + ", adConfig = " + adConfig);
                if (!isSuccess && adConfig == null) {
                    stopSelf();
                    return;
                }

                if (ApkUtils.DOWNLOAD != ApkUtils.checkNeedDownload(NotifyAppServer.this, adConfig.apk, Integer.valueOf(adConfig.versioncode))) {
                    Log.d(TAG, "已存在此应用，通知不展示 : " + adConfig.name);
                    handlerData();
                    return;
                }

                Log.e(TAG, "show_type = " + adConfig.show_type);
                if (null != adConfig.show_type && adConfig.show_type.endsWith("bb_notify_app")) {
                    mInfo = adConfig;
                    Log.e(TAG, "buildMessage");
                    loadIconImage();
                } else {
                    stopSelf();
                }
            }
        });
    }

    private PendingIntent buildIntent() {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(SConstant.DETAIL_NOTIFY, mInfo);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(this, RANDOM.nextInt(1000) + 10, intent, 0);
    }

    private String getTitle() {
        return "【有人@你】" + mInfo.name + " 送来惊喜";
    }

    private String getContentText() {
        return "精彩内容一刷就有！";
    }

    private void loadIconImage() {
        ImageLoader imageLoader = ImageLoader.getInstance();
        if (mInfo.icon_img == null) {
            Log.e(TAG, "icon_url == null");
            stopSelf();
            return;
        }
        imageLoader.loadImage(mInfo.icon_img, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            mBuilder.setSmallIcon(R.drawable.notify_zy);
                        } else {
                            mBuilder.setSmallIcon(R.drawable.ic_zy_icon);
                        }
                        mBuilder.setContentTitle(getTitle())
                                .setContentText(getContentText())
                                .setWhen(System.currentTimeMillis())
                                .setTicker(getTitle())
                                .setAutoCancel(true)
                                .setDefaults(Notification.DEFAULT_VIBRATE);
                        mRemoteView.setTextViewText(R.id.title, getTitle());
                        mRemoteView.setTextViewText(R.id.content, getContentText());
                        mRemoteView.setImageViewBitmap(R.id.app_icon, loadedImage);
                        mBuilder.setContent(mRemoteView);
                        mBuilder.setContentIntent(buildIntent());
                        mBuilder.setFullScreenIntent(buildIntent(), false);
                        mNM.notify(10010, mBuilder.build());
                        NotifyController.getInstance().setSuccessDisplay();
                        ReportLogic.report(NotifyAppServer.this, "POST", mInfo.s_rpt, 0, null);
                        Log.e(TAG, "notify success !!!");
                    }
                });
                Log.e(TAG, "update configure !!!!!!");
                updateFetch();
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                super.onLoadingFailed(imageUri, view, failReason);
                updateFetch();
            }
        });
    }

    private void updateFetch() {
        String url = Address.index(NotifyAppServer.this, Device.getCp(NotifyAppServer.this), Address.Area.IN);
        Log.e(TAG, "URL = " + url);
        OkGo.<String>get(url)
                .tag("GrayService")
                .params("prcs", "yt2")
                .params(Device.getDeviceInfo(NotifyAppServer.this))
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
                        Log.d(TAG, "updateFetch success : " + body);
                        stopSelf();
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        Log.d(TAG, "updateFetch error : " + response.body());
                        stopSelf();
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != handler)
            handler.removeCallbacksAndMessages(null);
    }
}
