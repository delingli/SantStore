package com.hai.store.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hai.store.R;
import com.hai.store.activity.DetailActivity;
import com.hai.store.activity.MoreListActivity;
import com.hai.store.base.SConstant;
import com.hai.store.bean.StoreApkInfo;
import com.hai.store.bean.StoreListInfo;
import com.hai.store.data.ReportLogic;
import com.hai.store.keepalive.GrayService;
import com.hai.store.utils.ApkUtils;
import com.hai.store.utils.Device;
import com.hai.store.utils.Utils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.PostRequest;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.hai.store.base.SConstant.APP_NAME;
import static com.hai.store.base.SConstant.DETAIL_ELSE;
import static com.hai.store.base.SConstant.LIST_MODE;
import static com.hai.store.base.SConstant.PKG_NAME;
import static com.hai.store.base.SConstant.TMODE_WIFI;
import static com.hai.store.base.SConstant.TMODE_WIFI2;
import static com.hai.store.utils.ApkUtils.DOWNLOAD;

public class WifiOneADActivity extends Activity {

    private ImageView mIcon;
    private TextView mName, mNumberAndSize, mVersionCode;
    private StoreListInfo mListInfo;
    private List<StoreApkInfo> tempList = new ArrayList<>();
    private Gson gson = new Gson();
    private StoreApkInfo mInfo;
    private Random RANDOM = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_wifi_one_ad_dialog);
        findView();
        loadData();
    }

    private void loadData() {
        getAppList(this, GrayService.nextPage, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                handleData(response);
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                finish();
            }
        });
    }

    private void handleData(Response<String> response) {
        mListInfo = gson.fromJson(response.body(), StoreListInfo.class);
        if (null != mListInfo && null == mListInfo.err && mListInfo.list.size() > 0) {
            GrayService.nextPage = mListInfo.href_next;
            Log.d("DB_WifiOneADActivity", "next page " + mListInfo.href_next);
            for (StoreApkInfo info : mListInfo.list) {
                if (DOWNLOAD == ApkUtils.getStatus(this, info.appid, info.apk, Integer.valueOf(info.versioncode))) {
                    tempList.add(info);
                }
            }
            if (tempList.size() >= 1) {
                show();
            } else {
                reload();
            }
        } else {
            finish();
        }
    }

    private void show() {
        mInfo = tempList.get(RANDOM.nextInt(tempList.size()));
        Picasso.with(this).load(mInfo.icon).placeholder(R.drawable.ic_loading).error(R.drawable.ic_loading).into(mIcon);
        mName.setText(mInfo.appname);
        mVersionCode.setText(Utils.versionName(mInfo.versionname));
        String numberAndSize;
        try {
            double count = Double.valueOf(mInfo.downcount);
            numberAndSize = Utils.downloadNum(count) + "  " + Utils.readableFileSize(mInfo.size);
            mNumberAndSize.setText(numberAndSize);
        } catch (NumberFormatException e) {
            numberAndSize = mInfo.downcount + "  " + Utils.readableFileSize(mInfo.size);
            mNumberAndSize.setText(numberAndSize);
        }
    }

    private void reload() {
        if (null != GrayService.nextPage) {
            Log.e("WifiOneADActivity", GrayService.nextPage);
            getAppList(this, GrayService.nextPage, new StringCallback() {
                @Override
                public void onSuccess(Response<String> response) {
                    handleData(response);
                }

                @Override
                public void onError(Response<String> response) {
                    super.onError(response);
                    finish();
                }
            });
        }
    }

    private void getAppList(Context context, String next, StringCallback stringCallback) {
        String url;
        if (null == next) {
            url = SConstant.MARKET + SConstant.TYPE + SConstant.TYPE_LIST + SConstant.CID
                    + SConstant.CID_WIFI + SConstant.PAGE + 1 + SConstant.TMODE + SConstant.TMODE_WIFI;
        } else {
            url = next + SConstant.TMODE + SConstant.TMODE_WIFI;
        }
        Map<String, String> deviceInfo = Device.getDeviceInfo(context);
        PostRequest<String> request = OkGo.<String>post(url)
                .tag("WifiADViewLogic_getAppList");
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

    private void findView() {
        ImageView close = (ImageView) findViewById(R.id.wifi_ad_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mInfo) {
                    ReportLogic.report(WifiOneADActivity.this, "POST", mInfo.rpt_dl, false, 0, null);
                }
                finish();
            }
        });

        TextView oneKey = (TextView) findViewById(R.id.wifi_one_key);
        oneKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mInfo) {
                    ReportLogic.report(WifiOneADActivity.this, "POST", mInfo.rpt_ct, 0, null);
                    buildDetailIntent();
                }
                finish();
            }
        });

        TextView gotoMore = (TextView) findViewById(R.id.wifi_goto_more);
        gotoMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mInfo) {
                    buildListIntent();
                }
                finish(); //不需要上报
            }
        });

        mName = (TextView) findViewById(R.id.wifi_app_name);
        mIcon = (ImageView) findViewById(R.id.wifi_app_icon);
        mNumberAndSize = (TextView) findViewById(R.id.down_number_and_size);
        mVersionCode = (TextView) findViewById(R.id.wifi_app_vc);
    }

    private void buildDetailIntent() {
        Intent intent = new Intent(this, DetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(PKG_NAME, mInfo.href_detail);
        bundle.putString(APP_NAME, mInfo.appname);
        bundle.putString(DETAIL_ELSE, TMODE_WIFI);
        intent.putExtra(DetailActivity.DETAIL, bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void buildListIntent() {
        Intent intent = new Intent(this, MoreListActivity.class);
        intent.putExtra(LIST_MODE, TMODE_WIFI2);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
