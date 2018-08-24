package com.hai.store.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hai.store.R;
import com.hai.store.activity.MoreListActivity;
import com.hai.store.base.SConstant;
import com.hai.store.bean.ClickInfo;
import com.hai.store.bean.DmBean;
import com.hai.store.bean.StoreApkInfo;
import com.hai.store.bean.StoreListInfo;
import com.hai.store.data.DownloadLogic;
import com.hai.store.data.ReportLogic;
import com.hai.store.sqlite.PublicDao;
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

import static com.hai.store.utils.ApkUtils.DOWNLOAD;

public class BannerAppListView extends FrameLayout {

    private ImageView icon1, icon2, icon3, icon4;
    private TextView name1, name2, name3, name4;
    private LinearLayout layout1, layout2, layout3, layout4, content;
    private LinearLayout mAll, contentLayout;
    private StoreListInfo appListInfo;
    private int x;
    private int y;
    private List<StoreApkInfo> tempList = new ArrayList<>();
    private Gson gson = new Gson();

    public BannerAppListView(Context context) {
        super(context);
    }

    public BannerAppListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.dc_frame_applist, this);
        icon1 = (ImageView) findViewById(R.id.icon1);
        icon2 = (ImageView) findViewById(R.id.icon2);
        icon3 = (ImageView) findViewById(R.id.icon3);
        icon4 = (ImageView) findViewById(R.id.icon4);
        name1 = (TextView) findViewById(R.id.appName1);
        name2 = (TextView) findViewById(R.id.appName2);
        name3 = (TextView) findViewById(R.id.appName3);
        name4 = (TextView) findViewById(R.id.appName4);
        layout1 = (LinearLayout) findViewById(R.id.layout1);
        layout2 = (LinearLayout) findViewById(R.id.layout2);
        layout3 = (LinearLayout) findViewById(R.id.layout3);
        layout4 = (LinearLayout) findViewById(R.id.layout4);
        content = (LinearLayout) findViewById(R.id.content);
        contentLayout = (LinearLayout) findViewById(R.id.content_layout);
        mAll = (LinearLayout) findViewById(R.id.all);
    }

    public void show(final Context context) {
        getAppList(context, null, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                handleData(response, context);
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
            }
        });
    }

    private void handleData(Response<String> response, Context context) {
        appListInfo = gson.fromJson(response.body(), StoreListInfo.class);
        if (null != appListInfo && null == appListInfo.err && appListInfo.list.size() > 0) {
            for (StoreApkInfo info : appListInfo.list) {
                if (DOWNLOAD == ApkUtils.getStatus(context, info.appid, info.apk, Integer.valueOf(info.versioncode))) {
                    tempList.add(info);
                }
            }
            if (tempList.size() >= 4) {
                showView(context);
            } else {
                reload(context);
            }
        }
    }

    private void reload(final Context context) {
        if (null != appListInfo.href_next) {
            getAppList(context, appListInfo.href_next, new StringCallback() {
                @Override
                public void onSuccess(Response<String> response) {
                    handleData(response, context);
                }

                @Override
                public void onError(Response<String> response) {
                    super.onError(response);

                }
            });
        }
    }

    private void showView(final Context context) {
//        ReportLogic.report(context, appListInfo.rpt_sb, false, 0);
        final StoreApkInfo info1 = tempList.get(0);
        final StoreApkInfo info2 = tempList.get(1);
        final StoreApkInfo info3 = tempList.get(2);
        final StoreApkInfo info4 = tempList.get(3);
        contentLayout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                x = (int) motionEvent.getX();
                y = (int) motionEvent.getY();
                return false;
            }
        });
        if (null != info1) {
            Picasso.with(context).load(info1.icon).placeholder(R.drawable.ic_loading).error(R.drawable.ic_loading).into(icon1);
            name1.setText(info1.appname);
            ReportLogic.report(context, appListInfo.rtp_method, info1.rpt_ss, 0, null);
        }
        if (null != info2) {
            Picasso.with(context).load(info2.icon).placeholder(R.drawable.ic_loading).error(R.drawable.ic_loading).into(icon2);
            name2.setText(info2.appname);
            ReportLogic.report(context, appListInfo.rtp_method, info2.rpt_ss, 0, null);
        }
        if (null != info3) {
            Picasso.with(context).load(info3.icon).placeholder(R.drawable.ic_loading).error(R.drawable.ic_loading).into(icon3);
            name3.setText(info3.appname);
            ReportLogic.report(context, appListInfo.rtp_method, info3.rpt_ss, 0, null);
        }
        if (null != info4) {
            Picasso.with(context).load(info4.icon).placeholder(R.drawable.ic_loading).error(R.drawable.ic_loading).into(icon4);
            name4.setText(info4.appname);
            ReportLogic.report(context, appListInfo.rtp_method, info4.rpt_ss, 0, null);
        }
        content.setVisibility(VISIBLE);
        layout1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != info1) {
                    reportAndSave(context, info1);
                }
            }
        });
        layout2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != info2) {
                    reportAndSave(context, info2);
                }
            }
        });
        layout3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != info3) {
                    reportAndSave(context, info3);
                }
            }
        });
        layout4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != info4) {
                    reportAndSave(context, info4);
                }
            }
        });
        mAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, MoreListActivity.class));
            }
        });
    }

    private void reportAndSave(Context context, StoreApkInfo info) {
        ReportLogic.report(context, appListInfo.rtp_method, info.rpt_cd, appListInfo.flag_replace, new ClickInfo(x, y));
        DownloadLogic.getInstance().startDownload(context, info.href_download, info.appname,
                info.appid, info.icon, info.apk, info.versioncode, info.rpt_dc, info.rpt_dl, appListInfo.rtp_method);
        PublicDao.insert(buildDmBean(info));
    }

    private DmBean buildDmBean(StoreApkInfo info) {
        DmBean dmBean = new DmBean();
        dmBean.packageName = info.apk;
        dmBean.appId = info.appid;
        dmBean.appName = info.appname;
        dmBean.iconUrl = info.icon;
        dmBean.downUrl = info.href_download;
        dmBean.size = info.size;
        dmBean.versionCode = info.versioncode;
        dmBean.versionName = info.versionname;
        dmBean.repDc = info.rpt_dc;
        dmBean.repInstall = info.rpt_ic;
        dmBean.repAc = info.rpt_ac;
        dmBean.method = appListInfo.rtp_method;
        return dmBean;
    }

    private void getAppList(Context context, String next, StringCallback stringCallback) {
        String url;
        if (null == next) {
            url = SConstant.MARKET + SConstant.TYPE + SConstant.TYPE_LIST + SConstant.CID
                    + SConstant.CID_HOT + SConstant.PAGE + 1;
        } else {
            url = next;
        }
        Map<String, String> deviceInfo = Device.getDeviceInfo(context);
        PostRequest<String> request = OkGo.<String>post(url)
                .tag("BannerViewLogic_getAppList");
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
