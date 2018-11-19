//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hai.store.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.gson.Gson;
import com.hai.store.R.drawable;
import com.hai.store.R.id;
import com.hai.store.R.layout;
import com.hai.store.activity.MoreListActivity;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BannerAppListView extends FrameLayout {
    private ImageView icon1;
    private ImageView icon2;
    private ImageView icon3;
    private ImageView icon4;
    private TextView name1;
    private TextView name2;
    private TextView name3;
    private TextView name4;
    private LinearLayout layout1;
    private LinearLayout layout2;
    private LinearLayout layout3;
    private LinearLayout layout4;
    private LinearLayout content;
    private LinearLayout mAll;
    private LinearLayout contentLayout;
    private StoreListInfo appListInfo;
    private int x;
    private int y;
    private List<StoreApkInfo> tempList = new ArrayList();
    private Gson gson = new Gson();

    public BannerAppListView(Context context) {
        super(context);
    }

    public BannerAppListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(layout.dc_frame_applist, this);
        this.icon1 = (ImageView)this.findViewById(id.icon1);
        this.icon2 = (ImageView)this.findViewById(id.icon2);
        this.icon3 = (ImageView)this.findViewById(id.icon3);
        this.icon4 = (ImageView)this.findViewById(id.icon4);
        this.name1 = (TextView)this.findViewById(id.appName1);
        this.name2 = (TextView)this.findViewById(id.appName2);
        this.name3 = (TextView)this.findViewById(id.appName3);
        this.name4 = (TextView)this.findViewById(id.appName4);
        this.layout1 = (LinearLayout)this.findViewById(id.layout1);
        this.layout2 = (LinearLayout)this.findViewById(id.layout2);
        this.layout3 = (LinearLayout)this.findViewById(id.layout3);
        this.layout4 = (LinearLayout)this.findViewById(id.layout4);
        this.content = (LinearLayout)this.findViewById(id.content);
        this.contentLayout = (LinearLayout)this.findViewById(id.content_layout);
        this.mAll = (LinearLayout)this.findViewById(id.all);
    }

    public void show(final Context context, final String market) {
        this.getAppList(context, (String)null, market, new StringCallback() {
            public void onSuccess(Response<String> response) {
                BannerAppListView.this.handleData(response, context, market);
            }

            public void onError(Response<String> response) {
                super.onError(response);
            }
        });
    }

    private void handleData(Response<String> response, Context context, String market) {
        this.appListInfo = (StoreListInfo)this.gson.fromJson((String)response.body(), StoreListInfo.class);
        if (null != this.appListInfo && null == this.appListInfo.err && this.appListInfo.list.size() > 0) {
            Iterator var4 = this.appListInfo.list.iterator();

            while(var4.hasNext()) {
                StoreApkInfo info = (StoreApkInfo)var4.next();
                if (0 == ApkUtils.getStatus(context, info.appid, info.apk, Integer.valueOf(info.versioncode))) {
                    this.tempList.add(info);
                }
            }

            if (this.tempList.size() >= 4) {
                this.showView(context);
            } else {
                this.reload(context, market);
            }
        }

    }

    private void reload(final Context context, final String market) {
        if (null != this.appListInfo.href_next) {
            this.getAppList(context, this.appListInfo.href_next, market, new StringCallback() {
                public void onSuccess(Response<String> response) {
                    BannerAppListView.this.handleData(response, context, market);
                }

                public void onError(Response<String> response) {
                    super.onError(response);
                }
            });
        }

    }

    private void showView(final Context context) {
        final StoreApkInfo info1 = (StoreApkInfo)this.tempList.get(0);
        final StoreApkInfo info2 = (StoreApkInfo)this.tempList.get(1);
        final StoreApkInfo info3 = (StoreApkInfo)this.tempList.get(2);
        final StoreApkInfo info4 = (StoreApkInfo)this.tempList.get(3);
        this.contentLayout.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                BannerAppListView.this.x = (int)motionEvent.getX();
                BannerAppListView.this.y = (int)motionEvent.getY();
                return false;
            }
        });
        if (null != info1) {
            Picasso.with(context).load(info1.icon).placeholder(drawable.ic_loading).error(drawable.ic_loading).into(this.icon1);
            this.name1.setText(info1.appname);
            ReportLogic.report(context, this.appListInfo.rtp_method, info1.rpt_ss, 0, (ClickInfo)null);
        }

        if (null != info2) {
            Picasso.with(context).load(info2.icon).placeholder(drawable.ic_loading).error(drawable.ic_loading).into(this.icon2);
            this.name2.setText(info2.appname);
            ReportLogic.report(context, this.appListInfo.rtp_method, info2.rpt_ss, 0, (ClickInfo)null);
        }

        if (null != info3) {
            Picasso.with(context).load(info3.icon).placeholder(drawable.ic_loading).error(drawable.ic_loading).into(this.icon3);
            this.name3.setText(info3.appname);
            ReportLogic.report(context, this.appListInfo.rtp_method, info3.rpt_ss, 0, (ClickInfo)null);
        }

        if (null != info4) {
            Picasso.with(context).load(info4.icon).placeholder(drawable.ic_loading).error(drawable.ic_loading).into(this.icon4);
            this.name4.setText(info4.appname);
            ReportLogic.report(context, this.appListInfo.rtp_method, info4.rpt_ss, 0, (ClickInfo)null);
        }

        this.content.setVisibility(View.VISIBLE);
        this.layout1.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (null != info1) {
                    BannerAppListView.this.reportAndSave(context, info1);
                }

            }
        });
        this.layout2.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (null != info2) {
                    BannerAppListView.this.reportAndSave(context, info2);
                }

            }
        });
        this.layout3.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (null != info3) {
                    BannerAppListView.this.reportAndSave(context, info3);
                }

            }
        });
        this.layout4.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (null != info4) {
                    BannerAppListView.this.reportAndSave(context, info4);
                }

            }
        });
        this.mAll.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                context.startActivity(new Intent(context, MoreListActivity.class));
            }
        });
        Intent intent = new Intent("APP_TRANS.ACTION");
        context.sendBroadcast(intent);
    }

    private void reportAndSave(Context context, StoreApkInfo info) {
        ReportLogic.report(context, this.appListInfo.rtp_method, info.rpt_cd, this.appListInfo.flag_replace, new ClickInfo(this.x, this.y));
        DownloadLogic.getInstance().startDownload(context, info.href_download, info.appname, info.appid, info.icon, info.apk, info.versioncode, info.rpt_dc, info.rpt_dl, this.appListInfo.rtp_method);
        PublicDao.insert(this.buildDmBean(info));
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
        dmBean.method = this.appListInfo.rtp_method;
        return dmBean;
    }

    private void getAppList(Context context, String next, String market, StringCallback stringCallback) {
        String url;
        if (null == next) {
            url = "http://adapi.yiticm.com:7701/market.php?type=list&cid=-2&page=1&market=" + market;
        } else {
            url = next;
        }

        Map<String, String> deviceInfo = Device.getDeviceInfo(context);
        PostRequest<String> request = (PostRequest)OkGo.post(url).tag("BannerViewLogic_getAppList");
        Iterator var8 = deviceInfo.keySet().iterator();

        while(true) {
            while(var8.hasNext()) {
                String key = (String)var8.next();
                if ("mac".equals(key) && "".equals(deviceInfo.get(key))) {
                    request.params(key, Utils.getMacAddress(context), new boolean[0]);
                } else {
                    request.params(key, (String)deviceInfo.get(key), new boolean[0]);
                }
            }

            request.execute(stringCallback);
            return;
        }
    }
}
