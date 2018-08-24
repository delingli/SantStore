package com.hai.store.view;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hai.store.R;
import com.hai.store.base.SConstant;
import com.hai.store.bean.ClickInfo;
import com.hai.store.bean.DmBean;
import com.hai.store.bean.StoreApkInfo;
import com.hai.store.bean.StoreListInfo;
import com.hai.store.data.DownloadCart;
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendADListView extends FrameLayout implements DownloadLogic.DownloadListener {

    private RecyclerView recycler;
    private TextView oneKeyInstall;
    public static boolean check;
    private RecommendListAdapter adapter;
    private StoreListInfo updateAppInfo;
    private Context mContext;
    private static int x;
    private static int y;
    public Map<String, List<String>> exposureId = new HashMap<>();
    private int mFirst;
    private int mLast;
    private Handler mHandler;
    private List<PackageInfo> mLocalApkInfo = new ArrayList<>();
    private List<String> mLocalApkPath = new ArrayList<>();

    public RecommendADListView(Context context) {
        super(context);
        this.mContext = context;
        initView(context);
    }

    public RecommendADListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initView(context);
    }

    public RecommendADListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.dc_frame_recommend, this, true);
        recycler = (RecyclerView) findViewById(R.id.app_list);
        AppCompatCheckBox update = (AppCompatCheckBox) findViewById(R.id.update_new);
        update.setChecked(true);
        check = true;
        update.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                check = b;
            }
        });
        oneKeyInstall = (TextView) findViewById(R.id.one_key_install);
    }

    /**
     * 接收传输的应用名称，根据名称获取路径
     */
    public void loadDate(Context context, List<String> appNameList) {
        List<Drawable> iconList = new ArrayList<>();
        for (String apkName : appNameList) {
            String path = Environment.getExternalStorageDirectory() + "/DCDownload" + apkName;
            PackageInfo apkInfo = ApkUtils.getApkInfo(path, context);
            Drawable icon = ApkUtils.getApkIcon(path, context);
            if (null != apkInfo && null != icon) {
                mLocalApkPath.add(path);
                mLocalApkInfo.add(apkInfo);
                iconList.add(icon);
            }
            Log.e("RECOMMEND_APP", "apk path : " + path);
        }
        showView(context, iconList);
        DownloadLogic.getInstance().setDownloadListener(this);
        getRecommendADList(context, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                Log.e("RECOMMEND_APP", "onSuccess");
                handlerDate(response.body());
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                Log.e("RecommendADList", "error : " + response.body());
            }
        });
    }

    private void reportExposure(int first, int last) {
        this.mFirst = first;
        this.mLast = last;
        report();
    }

    private void report() {
        if (null != updateAppInfo) {
            for (int i = mFirst; i <= mLast; i++) {
                PackageInfo apkInfo = mLocalApkInfo.get(i);
                if (null != apkInfo) {
                    for (StoreApkInfo info : updateAppInfo.list) {
                        if (apkInfo.packageName.equals(info.apk) && !exposureId.containsKey(info.appid)) {
                            Log.e("RECOMMEND_APP", "reportExposure");
                            ReportLogic.report(mContext, updateAppInfo.rtp_method, info.rpt_ss, 0, null);
                            exposureId.put(info.appid, info.rpt_ss);
                        }
                    }
                }
            }
        }
    }

    private void showView(final Context context, List<Drawable> localIconList) {
        recycler.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        recycler.setAdapter(adapter = new RecommendListAdapter(mContext, mLocalApkPath, mLocalApkInfo, localIconList));
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView view, int dx, int dy) {
                int lastVisibleItem = ((LinearLayoutManager) view.getLayoutManager()).findLastVisibleItemPosition();
                int firstVisibleItem = ((LinearLayoutManager) view.getLayoutManager()).findFirstVisibleItemPosition();
                int itemNumber = lastVisibleItem - firstVisibleItem;
//                View topView = view.getChildAt(0);
                View bottomView = view.getChildAt(itemNumber);
                int total = view.getHeight();
                int bottomViewTop = bottomView.getTop();
                int half = total - bottomViewTop;
                if (half >= bottomView.getHeight() / 3) {
                    //上报最后一个item
                    reportExposure(firstVisibleItem, lastVisibleItem);
                } else {
                    reportExposure(firstVisibleItem, lastVisibleItem - 1);
                    //不上报最后一个item
                }
            }
        });

        oneKeyInstall.setText(R.string.store_one_key_install);
        oneKeyInstall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                oneKeyInstall.setClickable(false);
                Toast.makeText(context, R.string.installing, Toast.LENGTH_SHORT).show();
                if (null == mHandler) {
                    mHandler = new Handler();
                }
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        oneKeyInstall.setClickable(true);
                        if (null != updateAppInfo) {
                            Log.e("RECOMMEND_APP", "updateAppInfo == " + updateAppInfo);
                            Map<String, String> willDown = new HashMap<>();
                            for (int i = 0; i < mLocalApkInfo.size(); i++) {
                                if (ApkUtils.isAvailable(context, mLocalApkInfo.get(i).packageName)) { //已安装，则忽略
                                    Toast.makeText(mContext, ApkUtils.getApkName(mLocalApkPath.get(i), context) + context.getString(R.string.was_install), Toast.LENGTH_SHORT).show();
                                    continue;
                                }
                                willDown.put(mLocalApkInfo.get(i).packageName, mLocalApkPath.get(i));
                            }
                            for (StoreApkInfo info : updateAppInfo.list) {
                                if (willDown.containsKey(info.apk)) {     //是否包含在请求回来到数据中
                                    int status = DownloadCart.getInstance().getApkStatus(info.appid);
                                    switch (status) {
                                        case ApkUtils.INSTALL:
                                            Log.e("RECOMMEND_APP", "安装下载apk");
                                            ApkUtils.install(context, DownloadLogic.buildUrl(context, info.appname));
                                            willDown.remove(info.apk);
                                            break;
                                        case ApkUtils.DOWNLOADING:
                                            if (check) {
                                                Log.e("RECOMMEND_APP", "正在下载apk");
                                                willDown.remove(info.apk);
                                            } else {
                                                Log.e("RECOMMEND_APP", "安装本地apk");
                                            }
                                            break;
                                        case ApkUtils.DOWNLOAD:
                                            if (check) {
                                                Log.e("RECOMMEND_APP", "download");
                                                downAndSave(context, updateAppInfo, info, true);
                                                willDown.remove(info.apk);
                                            } else {
                                                Log.e("RECOMMEND_APP", "安装本地apk");
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }
                            for (String pkgName : willDown.keySet()) {
                                String path = willDown.get(pkgName);
                                ApkUtils.install(mContext, path);
                            }
                        } else { // 请求洗包数据失败，安装本地apk
                            Log.e("RECOMMEND_APP", "安装本地apk");
                            Map<String, String> temp = new HashMap<>();
                            for (int i = 0; i < mLocalApkInfo.size(); i++) {
                                PackageInfo info = mLocalApkInfo.get(i);
                                int j = ApkUtils.checkNeedDownload(mContext, info.packageName, info.versionCode);
                                if (j == ApkUtils.DOWNLOAD || j == ApkUtils.UPDATE) {
                                    temp.put(info.packageName, mLocalApkPath.get(i));
                                }
                            }
                            if (temp.size() > 0) {
                                for (String pkgName : temp.keySet()) {
                                    ApkUtils.install(mContext, temp.get(pkgName));
                                }
                            } else {
                                Toast.makeText(mContext, R.string.already_install, Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (null != adapter) {
                            adapter.notifyDataSetChanged();
                        }
                    }
                }, 2000);
            }
        });
    }

    private void handlerDate(String body) {
        try {
            updateAppInfo = new Gson().fromJson(body, StoreListInfo.class);
            if (null != updateAppInfo && updateAppInfo.list.size() > 0) {
                if (null != updateAppInfo.err) {
                    Log.e("RECOMMEND_APP", "服务器返回错误 : " + updateAppInfo.err);
                    return;
                }
                adapter.update(updateAppInfo);
                report();
            }
        } catch (Exception e) {
            updateAppInfo = null;
            Log.e("RECOMMEND_APP", "类型解析错误");
        }
    }

    /**
     * 请求数据
     * 本地已安装的应用将不再请求
     */
    private void getRecommendADList(Context context, StringCallback stringCallback) {
        String url = SConstant.MARKET + SConstant.TYPE + SConstant.TYPE_RECOMMEND_AD;
        Map<String, String> deviceInfo = Device.getDeviceInfo(context);
        PostRequest<String> request = OkGo.<String>post(url)
                .tag("RecommendADList");
        for (String key : deviceInfo.keySet()) {
            if ("mac".equals(key)) {
                if ("".equals(deviceInfo.get(key))) {
                    request.params(key, Utils.getMacAddress(context));
                    continue;
                }
            }
            request.params(key, deviceInfo.get(key));
        }
        StringBuilder builder = new StringBuilder();
        if (null != mLocalApkInfo) {
            for (int i = 0; i < mLocalApkInfo.size(); i++) {
                if (ApkUtils.isAvailable(context, mLocalApkInfo.get(i).packageName)) { //已安装，则忽略
                    continue;
                }
                if (i == mLocalApkInfo.size() - 1) {
                    builder.append(mLocalApkInfo.get(i).packageName);
                } else {
                    builder.append(mLocalApkInfo.get(i).packageName).append(",");
                }
            }
        }
        if (builder.length() == 0) {
            return;
        }
        request.params(SConstant.APP_LIST, builder.toString());
        request.execute(stringCallback);
    }

    public void onResume() {
        if (null != adapter) {
            adapter.notifyDataSetChanged();
        }
    }

    public void onClose() {
        DownloadLogic.getInstance().revokedDownloadListener(this);
    }

    @Override
    public void onProgressListener(String appId) {

    }

    @Override
    public void onError(String appId) {
        if (null != adapter) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart(String appId) {
        if (null != adapter) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSuccess(String appId) {
        if (null != adapter) {
            adapter.notifyDataSetChanged();
        }
    }

    private static class RecommendListAdapter extends RecyclerView.Adapter<ViewHolder> {
        private Context context;
        private StoreListInfo info;
        private List<Drawable> localDrawableList;
        private List<String> localPathList;
        private List<PackageInfo> localInfoList;

        private RecommendListAdapter(Context context, List<String> apkPathList, List<PackageInfo> apkInfoList,
                                     List<Drawable> apkDrawableList) {
            this.context = context;
            this.localDrawableList = apkDrawableList;
            this.localInfoList = apkInfoList;
            this.localPathList = apkPathList;
        }

        private void update(StoreListInfo info) {
            this.info = info;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recommend_list, parent, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            String filePath = localPathList.get(position);
            PackageInfo apkInfo = localInfoList.get(position);
            holder.icon.setImageDrawable(localDrawableList.get(position));
            holder.title.setText(ApkUtils.getApkName(filePath, context));
            holder.size.setText(Utils.readableFileSize(String.valueOf(new File(filePath).length())));
            holder.vc.setText(Utils.versionName(apkInfo.versionName));
            int apkStatus = ApkUtils.checkNeedDownload(context, apkInfo.packageName, apkInfo.versionCode);
            holder.install.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    x = (int) motionEvent.getX();
                    y = (int) motionEvent.getY();
                    return false;
                }
            });
            Log.e("RECOMMEND_APP", "holder.install apkStatus == " + apkStatus);
            switch (apkStatus) {
                case ApkUtils.DOWNLOAD: //手机上不存在该应用
                    holder.install.setText(R.string.install);
                    holder.install.setClickable(true);
                    if (null != info) { //获取到换包数据
                        for (StoreApkInfo appInfo : info.list) {
                            if (apkInfo.packageName.equals(appInfo.apk)) {
                                holder.install.setTag(appInfo);
                                holder.title.setText(appInfo.appname);
                                Integer status = DownloadCart.getInstance().getApkStatus(appInfo.appid);
                                if (ApkUtils.DOWNLOAD == status) { //手机上未下载该应用
                                    holder.install.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            holder.install.setClickable(false);
                                            holder.install.setText(R.string.ready_install);
                                            if (check) {
                                                StoreApkInfo tag = (StoreApkInfo) view.getTag();
                                                downAndSave(context, info, tag, true);
                                            } else {
                                                ApkUtils.install(context, localPathList.get(holder.getAdapterPosition()));
                                            }
                                        }
                                    });
                                    Log.e("RECOMMEND_APP", "check will down");
                                    return;
                                }
                                if (ApkUtils.INSTALL == status) { //手机上已经下载好该应用
                                    holder.install.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            StoreApkInfo tag = (StoreApkInfo) view.getTag();
                                            holder.install.setClickable(false);
                                            holder.install.setText(R.string.ready_install);
                                            ApkUtils.install(context, DownloadLogic.buildUrl(context, tag.appname));
                                        }
                                    });
                                    Log.e("RECOMMEND_APP", "will install");
                                    return;
                                }
                                if (ApkUtils.DOWNLOADING == status) { //手机正在下载该应用
                                    holder.install.setText(R.string.ready_install);
                                    holder.install.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Toast.makeText(context, R.string.downloading_new_apps, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    Log.e("RECOMMEND_APP", "downloading");
                                    return;
                                }
                            }
                        }
                    }
                    //未获取到换包数据
                    Log.e("RECOMMEND_APP", "未获取到换包数据");
                    holder.install.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            holder.install.setClickable(false);
                            holder.install.setText(R.string.ready_install);
                            ApkUtils.install(context, localPathList.get(holder.getAdapterPosition()));
                        }
                    });
                    break;
                case ApkUtils.OPEN: //手机已经安装有该应用，最新版本
                    holder.install.setText(R.string.open);
                    holder.install.setClickable(true);
                    holder.install.setTag(apkInfo);
                    holder.install.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            PackageInfo tag = (PackageInfo) view.getTag();
                            ApkUtils.startApp(context, tag.packageName);
                        }
                    });
                    break;
                case ApkUtils.UPDATE: //手机已经安装有该应用，版本较低
                    if (null != info) {
                        for (StoreApkInfo appInfo : info.list) {
                            if (apkInfo.packageName.equals(appInfo.apk)) {
                                holder.title.setText(appInfo.appname);
                                Integer status = DownloadCart.getInstance().getApkStatus(appInfo.appid);
                                if (ApkUtils.OPEN == status) {
                                    holder.install.setText(R.string.open);
                                    holder.install.setClickable(true);
                                    holder.install.setTag(appInfo);
                                    holder.install.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            StoreApkInfo tag = (StoreApkInfo) view.getTag();
                                            ApkUtils.startApp(context, tag.apk);
                                        }
                                    });
                                    return;
                                }
                            }
                        }
                    }
                    holder.install.setText(R.string.install);
                    holder.install.setClickable(true);
                    holder.install.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            holder.install.setClickable(false);
                            holder.install.setText(R.string.ready_install);
                            ApkUtils.install(context, localPathList.get(holder.getAdapterPosition()));
                        }
                    });
                    break;
                default:
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return localInfoList.size();
        }
    }

    private static void downAndSave(Context context, StoreListInfo updateAppInfo, StoreApkInfo tag,
                                    boolean report) {
        if (report) {
            ReportLogic.report(context, updateAppInfo.rtp_method, tag.rpt_cd, 0, new ClickInfo(x, y));
        }
        DownloadLogic.getInstance().startDownload(context, tag.href_download, tag.appname,
                tag.appid, tag.icon, tag.apk, tag.versioncode, tag.rpt_dc, null, updateAppInfo.rtp_method);
        PublicDao.insert(buildDmBean(tag, updateAppInfo.rtp_method));
    }

    private static DmBean buildDmBean(StoreApkInfo info, String rtp_method) {
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
        dmBean.method = rtp_method;
        return dmBean;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, size, vc, install;

        private ViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.app_icon);
            title = (TextView) itemView.findViewById(R.id.title);
            size = (TextView) itemView.findViewById(R.id.size);
            vc = (TextView) itemView.findViewById(R.id.vc);
            install = (TextView) itemView.findViewById(R.id.install);
        }
    }
}
