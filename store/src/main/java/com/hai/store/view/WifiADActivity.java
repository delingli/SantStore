package com.hai.store.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hai.store.R;
import com.hai.store.activity.MoreListActivity;
import com.hai.store.base.SConstant;
import com.hai.store.bean.DmBean;
import com.hai.store.bean.StoreApkInfo;
import com.hai.store.bean.StoreListInfo;
import com.hai.store.data.DownloadLogic;
import com.hai.store.data.ReportLogic;
import com.hai.store.keepalive.GrayService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.hai.store.utils.ApkUtils.DOWNLOAD;

public class WifiADActivity extends Activity {

    private TextView mOneKey;
    private RecyclerView mRecycler;
    private StoreListInfo appListInfo;
    private List<StoreApkInfo> tempList = new ArrayList<>();
    private Gson gson = new Gson();
    private ADAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_wifi_ad_dialog);
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
        appListInfo = gson.fromJson(response.body(), StoreListInfo.class);
        if (null != appListInfo && null == appListInfo.err && appListInfo.list.size() > 0) {
            GrayService.nextPage = appListInfo.href_next;
            for (StoreApkInfo info : appListInfo.list) {
                if (DOWNLOAD == ApkUtils.getStatus(this, info.appid, info.apk, Integer.valueOf(info.versioncode))) {
                    tempList.add(info);
                }
            }
            if (tempList.size() >= 4) {
                show();
            } else {
                reload();
            }
        }
    }

    private void show() {
        mRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRecycler.setAdapter(mAdapter = new ADAdapter(this, tempList, appListInfo));
        mOneKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapter.onKeyDownload();
                finish();
            }
        });
    }

    private void reload() {
        if (null != GrayService.nextPage) {
            Log.e("WifiADActivity", GrayService.nextPage);
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
                    + SConstant.CID_WIFI + SConstant.PAGE + 1;
        } else {
            url = next;
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
        ImageView mClose = (ImageView) findViewById(R.id.wifi_ad_close);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mOneKey = (TextView) findViewById(R.id.wifi_ad_one_key);
        TextView mMore = (TextView) findViewById(R.id.wifi_goto_more);
        mMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WifiADActivity.this, MoreListActivity.class));
                finish();
            }
        });
        mRecycler = (RecyclerView) findViewById(R.id.wifi_ad_list);
    }

    private static class ADAdapter extends RecyclerView.Adapter<ViewHolder> {

        private String TAG = "ADAdapter";
        private List<StoreApkInfo> appInfoList;
        private Context context;
        private Set<StoreApkInfo> checkInfo;
        private StoreListInfo listInfo;

        ADAdapter(Context context, List<StoreApkInfo> appInfos, StoreListInfo info) {
            this.listInfo = info;
            this.context = context;
            appInfoList = appInfos;
            checkInfo = new HashSet<>();
            for (int i = 0; i < 4; i++) {
                checkInfo.add(appInfos.get(i));
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_wifi_ad_list, parent, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final StoreApkInfo appInfo = appInfoList.get(position);
            ReportLogic.report(context, listInfo.rtp_method, appInfo.rpt_ss, 0, null);
            holder.title.setText(appInfo.appname);
            Picasso.with(context).load(appInfo.icon).placeholder(R.drawable.ic_loading).error(R.drawable.ic_loading).into(holder.icon);
            holder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.gou.getVisibility() == View.VISIBLE) {
                        holder.gou.setVisibility(View.GONE);
                        checkInfo.remove(appInfo);
                    } else {
                        holder.gou.setVisibility(View.VISIBLE);
                        checkInfo.add(appInfo);
                    }
                    for (StoreApkInfo info : checkInfo) {
                        Log.e(TAG, "AppInfo = " + info);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return 4;
        }

        void onKeyDownload() {
            for (StoreApkInfo info : checkInfo) {
                reportAndSave(context, info);
            }
        }

        private void reportAndSave(Context context, StoreApkInfo info) {
            ReportLogic.report(context, listInfo.rtp_method, info.rpt_cd, listInfo.flag_replace, null); // TODO: 17-9-29 替换坐标
            DownloadLogic.getInstance().startDownload(context, info.href_download, info.appname,
                    info.appid, info.icon, info.apk, info.versioncode, info.rpt_dc, info.rpt_dl, listInfo.rtp_method);
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
            dmBean.method = listInfo.rtp_method;
            return dmBean;
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout root;
        private ImageView icon, gou;
        private TextView title;

        ViewHolder(View itemView) {
            super(itemView);
            root = (LinearLayout) itemView.findViewById(R.id.wifi_ad_app_ll);
            icon = (ImageView) itemView.findViewById(R.id.wifi_ad_icon);
            gou = (ImageView) itemView.findViewById(R.id.wifi_ad_gou);
            title = (TextView) itemView.findViewById(R.id.wifi_ad_title);
        }
    }
}
