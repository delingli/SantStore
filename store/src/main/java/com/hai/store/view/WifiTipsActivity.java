package com.hai.store.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hai.store.R;
import com.hai.store.activity.MoreListActivity;
import com.hai.store.base.SConstant;
import com.hai.store.bean.WifiTipsInfo;
import com.hai.store.data.ReportLogic;
import com.hai.store.utils.Device;
import com.hai.store.utils.Utils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.request.PostRequest;

import java.util.Map;

public class WifiTipsActivity extends Activity {

    private String TAG = "WifiTips";
    private WifiTipsInfo mInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_wifi_tips_dialog);
        findView();
        loadData();
    }

    private void loadData() {
        mInfo = (WifiTipsInfo) getIntent().getSerializableExtra("WIFI_TIPS");
        Log.e(TAG, "info = " + mInfo);
        if (null != mInfo) {
            ReportLogic.report(this, "GET", mInfo.rpt_ss, false, 0, null);
        }
    }

    private void findView() {
        TextView mGotoStore = (TextView) findViewById(R.id.wifi_tips_store);
        mGotoStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mInfo) {
                    ReportLogic.report(WifiTipsActivity.this, "GET", mInfo.rpt_ct, false, 0, null);
                }
//                Intent intent = new Intent();
//                intent.putExtra(SConstant.LIST_RPT, SConstant.WIFI_CP);
//                intent.setClass(WifiTipsActivity.this, MoreListActivity.class);
//                startActivity(intent);
                finish();
            }
        });
        ImageView mClose = (ImageView) findViewById(R.id.wifi_tips_close);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mInfo) {
                    ReportLogic.report(WifiTipsActivity.this, "GET", mInfo.rpt_dl, false, 0, null);
                }
                finish();
            }
        });
    }

    public static void getWifiTipsList(Context context, StringCallback stringCallback) {
        String url = SConstant.MARKET + SConstant.TYPE + SConstant.TYPE_WIFI;
        Map<String, String> deviceInfo = Device.getDeviceInfo(context);
        PostRequest<String> request = OkGo.<String>post(url)
                .tag("WifiTipsViewLogic");
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
