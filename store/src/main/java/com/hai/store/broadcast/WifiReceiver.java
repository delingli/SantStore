package com.hai.store.broadcast;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import com.hai.store.utils.Utils;
import com.hai.store.view.WifiADActivity;
import com.hai.store.view.WifiOneADActivity;

public class WifiReceiver extends BroadcastReceiver {

    private Handler mHandler;
    private String TAG = "WifiReceiver";
    private Context context;
    private boolean post;
    private int count;

    @Override
    public void onReceive(final Context context, Intent intent) {
        this.context = context;
//        Log.e(TAG, "mWifiReceiver");
//        if (intent != null) {
//            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
//                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
//                switch (wifiState) {
//                    case WifiManager.WIFI_STATE_DISABLED:
//                        post = false;
//                        break;
//                    case WifiManager.WIFI_STATE_ENABLED:
//                        post = true;
//                        count = 0;
//                        Log.e(TAG, "WIFI_STATE_ENABLED");
//                        mHandler = new Handler();
//                        mHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                mHandler.post(checkNet);
//                            }
//                        }, 6000);
//                        break;
//                }
//            }
//        }
    }

    private Runnable checkNet = new Runnable() {
        @Override
        public void run() {
            if (Utils.isWifiConnected(context)) {
                Intent adIntent = new Intent();
                adIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                adIntent.setClass(context, WifiOneADActivity.class);
                context.startActivity(adIntent);
//                WifiTipsActivity.getWifiTipsList(context, new StringCallback() {
//                    @Override
//                    public void onSuccess(Response<String> response) {
//                        WifiTipsInfo info = gson.fromJson(response.body(), WifiTipsInfo.class);
//                        Intent intent = new Intent();
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.putExtra("WIFI_TIPS", info);
//                        intent.setClass(context, WifiTipsActivity.class);
//                        context.startActivity(intent);
//                    }
//
//                    @Override
//                    public void onError(Response<String> response) {
//                        super.onError(response);
//                    }
//                });
            } else {
                if (post) {
                    count++;
                    if (count == 10)
                        return;
                    mHandler.postDelayed(checkNet, 2000);
                }
            }
        }
    };
}
