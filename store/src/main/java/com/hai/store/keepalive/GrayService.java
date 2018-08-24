package com.hai.store.keepalive;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.hai.store.keepalive.api.Address;
import com.hai.store.keepalive.jss.PulseService;
import com.hai.store.notify.NotifyAppServer;
import com.hai.store.notify.NotifyController;
import com.hai.store.utils.Device;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import static android.text.TextUtils.isEmpty;

public class GrayService extends Service {

    private static final String TAG = "GrayService";
    public static final String ACTION = "gray";
    private final static int GRAY_SERVICE_ID = 1001101;
    private boolean mTag = false;
    private TimerTask mTimerTask;
    private Timer mTimer;
    private Handler mHandle = new Handler();
    public static String nextPage;

    public static Intent start(Context context) {
        Intent intent = new Intent(context, GrayService.class);
        context.startService(intent);
        return intent;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
        mTag = false;
    }

    private final Runnable fetchConfig = new Runnable() {
        @Override
        public void run() {
            long time = NotifyController.getInstance().getFetchInterval();
            Log.e(TAG, "notify time = " + time);
            mHandle.postDelayed(fetchConfig, time);

            if (System.currentTimeMillis() - NotifyController.getInstance().getLastFetchTime() < NotifyController.getInstance().getFetchInterval()) {
                Log.e(TAG, "notify has't enough time to fetch configure");
                return;
            }
            Log.e(TAG, "update configure !!!!!!");
            String url = Address.index(GrayService.this, Device.getCp(GrayService.this), Address.Area.IN);
            Log.e(TAG, "URL = " + url);
            if (null == url) {
                return;
            }
            OkGo.<String>get(url)
                    .tag("GrayService")
                    .params("prcs", "yt2")
                    .params(Device.getDeviceInfo(GrayService.this))
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            String body = response.body();
                            if (!isEmpty(body)) {
                                try {
                                    JSONObject root = new JSONObject(body);
                                    Long interval = root.optLong("ri", 5 * 60 * 1000);
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
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mTag) {
            mHandle.post(fetchConfig);
            if (Build.VERSION.SDK_INT < 18) {
                startForeground(GRAY_SERVICE_ID, new Notification());//API < 18 ，此方法能有效隐藏Notification上的图标
            } else {
                Intent innerIntent = new Intent(this, GrayInnerService.class);
                startService(innerIntent);
                startForeground(GRAY_SERVICE_ID, new Notification());
            }
//            timerTask();
        }
        mTag = true;
        return START_STICKY;
    }

    private void timerTask() {
        if (null == mTimer) {
            mTimer = new Timer();
        }
        if (null == mTimerTask) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    Log.e(TAG, "KEEP_ALIVE");
                    if (NotifyController.getInstance().shouldPopup()) {
                        startService(new Intent(GrayService.this, NotifyAppServer.class));
//                    if (NotifyController.getInstance().shouldPopup() && !NotifyActivity.VISIBLE && Utils.isWifiConnected(GrayService.this)) {
//                        Intent intent = new Intent();
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.setClass(GrayService.this, NotifyActivity.class);
//                        startActivity(intent);
                    } else {
                        Log.e(TAG, "NO POPUP");
                    }
                }
            };
            mTimer.schedule(mTimerTask, 0, PulseService.KEEP_ALIVE_INTERVAL);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
        }
    }

    public static class GrayInnerService extends Service {
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }
    }
}
