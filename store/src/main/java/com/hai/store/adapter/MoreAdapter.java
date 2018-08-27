package com.hai.store.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hai.store.Application;
import com.hai.store.R;
import com.hai.store.activity.DetailActivity;
import com.hai.store.bean.ClickInfo;
import com.hai.store.bean.DmBean;
import com.hai.store.bean.RptBean;
import com.hai.store.bean.StoreApkInfo;
import com.hai.store.bean.StoreListInfo;
import com.hai.store.data.DownloadCart;
import com.hai.store.data.DownloadLogic;
import com.hai.store.data.ReportLogic;
import com.hai.store.fragment.MoreListFragment;
import com.hai.store.sqlite.PublicDao;
import com.hai.store.utils.ApkUtils;
import com.hai.store.utils.Utils;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import static com.hai.store.base.SConstant.APP_NAME;
import static com.hai.store.base.SConstant.DETAIL_ELSE;
import static com.hai.store.base.SConstant.PKG_NAME;

public class MoreAdapter extends RecyclerView.Adapter<MoreAdapter.MoreHolder> {

    private Context context;
    public static List<StoreApkInfo> appList;
    private StoreListInfo appListInfo;
    private String mModeReq;
    private int x;
    private int y;
    private Gson gson;

    public MoreAdapter(StoreListInfo info, Context context, String mode) {
        mModeReq = mode;
        appListInfo = info;
        appList = info.list;
        this.context = context;
        gson = new Gson();
    }

    public void setData(StoreListInfo info) {
        appListInfo = info;
        appList = info.list;
        notifyDataSetChanged();
    }

    public void addData(List<StoreApkInfo> info) {
        int size = appList.size();
        appList.addAll(info);
        notifyItemInserted(size);
    }

    @Override
    public MoreAdapter.MoreHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MoreHolder(LayoutInflater.from(context).inflate(R.layout.item_more_list, parent, false));
    }

    @Override
    public void onBindViewHolder(final MoreAdapter.MoreHolder holder, int position) {
        final StoreApkInfo info = appList.get(position);
        holder.appName.setText(info.appname);
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                x = (int) motionEvent.getX();
                y = (int) motionEvent.getY();
                return false;
            }
        });
        holder.itemView.setTag(info);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(PKG_NAME, info.href_detail);
                bundle.putString(APP_NAME, info.appname);
                bundle.putString(DETAIL_ELSE, mModeReq);
                intent.putExtra(DetailActivity.DETAIL, bundle);
                context.startActivity(intent);
                ReportLogic.report(context, appListInfo.rtp_method, info.rpt_ct, appListInfo.flag_replace, new ClickInfo(x, y));
            }
        });

        Picasso.with(context).load(info.icon).placeholder(R.drawable.ic_loading).error(R.drawable.ic_loading).into(holder.icon);

        if (null != info.downcount) {
            holder.count.setVisibility(View.VISIBLE);
            try {
                double count = Double.valueOf(info.downcount);
                holder.count.setText(Utils.downloadNum(count));
            } catch (NumberFormatException e) {
                holder.count.setText(info.downcount);
            }
        } else {
            holder.count.setVisibility(View.GONE);
        }

        try {
            holder.size.setText(Utils.readableFileSize(info.size));
        } catch (NumberFormatException e) {
            holder.size.setText(info.size);
        }

        holder.version.setText(Utils.versionName(info.versionname));
        int apkStatus = ApkUtils.getStatus(context, info.appid, info.apk, Integer.valueOf(info.versioncode));
        holder.down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                x = (int) motionEvent.getX();
                y = (int) motionEvent.getY();
                return false;
            }
        });
        holder.down.setTag(info);
        switch (apkStatus) {
            case ApkUtils.INSTALL:
                holder.down.setText(R.string.install);
                holder.down.setClickable(true);
                holder.down.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StoreApkInfo tag = (StoreApkInfo) view.getTag();
                        ApkUtils.tryInstall(context, new File(DownloadLogic.buildUrl(context, tag.appname)));
//                        ApkUtils.install(context, DownloadLogic.buildUrl(context, tag.appname));
                    }
                });
                break;
            case ApkUtils.DOWNLOAD:
                holder.down.setText(R.string.download);
                holder.down.setClickable(true);
                holder.down.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.down.setClickable(false);
                        StoreApkInfo tag = (StoreApkInfo) view.getTag();
                        holder.down.setText(R.string.downloading);
                        downAndSave(tag, true);
                    }
                });
                break;
            case ApkUtils.DOWNLOADING:
                holder.down.setText(R.string.downloading);
                holder.down.setClickable(false);
                break;
            case ApkUtils.OPEN:
                holder.down.setText(R.string.open);
                holder.down.setClickable(true);
                holder.down.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StoreApkInfo tag = (StoreApkInfo) view.getTag();
                        ApkUtils.startApp(context, tag.apk);
                    }
                });
                break;
            case ApkUtils.UPDATE:
                holder.down.setText(R.string.update);
                holder.down.setClickable(true);
                holder.down.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.down.setClickable(false);
                        StoreApkInfo tag = (StoreApkInfo) view.getTag();
                        holder.down.setText(R.string.downloading);
                        downAndSave(tag, false);
                    }
                });
                break;
            default:
                break;
        }
    }

    private void downAndSave(final StoreApkInfo tag, boolean report) {
        if (1 == appListInfo.flag_download) {
            for (int i = 0; i < tag.rpt_cd.size(); i++) {
                if (i == 0) {
                    DownloadCart.getInstance().setApkStatus(tag.appid, ApkUtils.DOWNLOADING);
                    DownloadCart.DownloadStatus status = new DownloadCart.DownloadStatus(0, 0, 0,
                            tag.icon,
                            tag.appname,
                            tag.href_download,
                            tag.appid,
                            tag.apk,
                            tag.versioncode,
                            tag.rpt_dc,
                            tag.rpt_dl,
                            appListInfo.rtp_method);
                    DownloadCart.getInstance().setApkCarDownloadStatus(tag.appid, status);
                    ReportLogic.report(context, appListInfo.rtp_method, tag.rpt_cd.get(i), false, 0, new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            String body = response.body();
                            if (null != body) {
                                RptBean rptBean = gson.fromJson(body, RptBean.class);
                                if (null != rptBean && null != rptBean.href_download) {
                                    DownloadLogic.getInstance().startDownload(context, rptBean.href_download, tag.appname,
                                            tag.appid, tag.icon, tag.apk, tag.versioncode, tag.rpt_dc, tag.rpt_dl, appListInfo.rtp_method);
                                    PublicDao.insert(buildDmBean(tag, rptBean.href_download));
                                    return;
                                }
                            }
                            DownloadCart.getInstance().remove(tag.appid);
                            DownloadCart.getInstance().removeDownloadStatus(tag.appid);
                            Toast.makeText(Application.getContext(), R.string.down_failed, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            DownloadCart.getInstance().remove(tag.appid);
                            DownloadCart.getInstance().removeDownloadStatus(tag.appid);
                            Toast.makeText(Application.getContext(), R.string.down_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    ReportLogic.report(context, appListInfo.rtp_method, tag.rpt_cd.get(i), false, 0, null);
                }
            }
        } else {
            if (report) {
                ReportLogic.report(context, appListInfo.rtp_method, tag.rpt_cd, appListInfo.flag_replace, new ClickInfo(x, y));
            }
            DownloadLogic.getInstance().startDownload(context, tag.href_download, tag.appname,
                    tag.appid, tag.icon, tag.apk, tag.versioncode, tag.rpt_dc, tag.rpt_dl, appListInfo.rtp_method);
            PublicDao.insert(buildDmBean(tag, null));
        }
    }

    private DmBean buildDmBean(StoreApkInfo info, String downloadUrl) {
        DmBean dmBean = new DmBean();
        dmBean.packageName = info.apk;
        dmBean.appId = info.appid;
        dmBean.appName = info.appname;
        dmBean.iconUrl = info.icon;
        if (null != downloadUrl) {
            dmBean.downUrl = downloadUrl;
        } else {
            dmBean.downUrl = info.href_download;
        }
        dmBean.size = info.size;
        dmBean.versionCode = info.versioncode;
        dmBean.versionName = info.versionname;
        dmBean.repDc = info.rpt_dc;
        dmBean.repInstall = info.rpt_ic;
        dmBean.repAc = info.rpt_ac;
        dmBean.repDel = info.rpt_dl;
        dmBean.method = appListInfo.rtp_method;
        return dmBean;
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    class MoreHolder extends RecyclerView.ViewHolder {

        TextView appName, down, count, size, version;
        ImageView icon;

        MoreHolder(View itemView) {
            super(itemView);
            appName = (TextView) itemView.findViewById(R.id.app_name);
            down = (TextView) itemView.findViewById(R.id.app_down);
            count = (TextView) itemView.findViewById(R.id.app_count);
            size = (TextView) itemView.findViewById(R.id.app_size);
            version = (TextView) itemView.findViewById(R.id.app_version);
            icon = (ImageView) itemView.findViewById(R.id.app_icon);
        }

    }
}
