//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hai.store.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.hai.store.Application;
import com.hai.store.R.drawable;
import com.hai.store.R.id;
import com.hai.store.R.layout;
import com.hai.store.R.string;
import com.hai.store.activity.DetailActivity;
import com.hai.store.bean.ClickInfo;
import com.hai.store.bean.DmBean;
import com.hai.store.bean.RptBean;
import com.hai.store.bean.StoreApkInfo;
import com.hai.store.bean.StoreListInfo;
import com.hai.store.data.DownloadCart;
import com.hai.store.data.DownloadLogic;
import com.hai.store.data.ReportLogic;
import com.hai.store.data.DownloadCart.DownloadStatus;
import com.hai.store.sqlite.PublicDao;
import com.hai.store.utils.ApkUtils;
import com.hai.store.utils.Utils;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.List;

public class MoreAdapter extends Adapter<MoreAdapter.MoreHolder> {
    private Context context;
    public static List<StoreApkInfo> appList;
    private StoreListInfo appListInfo;
    private String mModeReq;
    private int x;
    private int y;
    private Gson gson;

    public MoreAdapter(StoreListInfo info, Context context, String mode) {
        this.mModeReq = mode;
        this.appListInfo = info;
        appList = info.list;
        this.context = context;
        this.gson = new Gson();
    }

    public void setData(StoreListInfo info) {
        this.appListInfo = info;
        appList = info.list;
        this.notifyDataSetChanged();
    }

    public void addData(List<StoreApkInfo> info) {
        int size = appList.size();
        appList.addAll(info);
        this.notifyItemInserted(size);
    }

    public MoreAdapter.MoreHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MoreAdapter.MoreHolder(LayoutInflater.from(this.context).inflate(layout.item_more_list, parent, false));
    }

    public void onBindViewHolder(final MoreAdapter.MoreHolder holder, int position) {
        final StoreApkInfo info = (StoreApkInfo)appList.get(position);
        holder.appName.setText(info.appname);
        holder.itemView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                MoreAdapter.this.x = (int)motionEvent.getX();
                MoreAdapter.this.y = (int)motionEvent.getY();
                return false;
            }
        });
        holder.itemView.setTag(info);
        holder.itemView.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(MoreAdapter.this.context, DetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("packageName", info.href_detail);
                bundle.putString("appName", info.appname);
                bundle.putString("detail_else", MoreAdapter.this.mModeReq);
                intent.putExtra("app_detail", bundle);
                MoreAdapter.this.context.startActivity(intent);
                ReportLogic.report(MoreAdapter.this.context, MoreAdapter.this.appListInfo.rtp_method, info.rpt_ct, MoreAdapter.this.appListInfo.flag_replace, new ClickInfo(MoreAdapter.this.x, MoreAdapter.this.y));
            }
        });
        Picasso.with(this.context).load(info.icon).placeholder(drawable.ic_loading).error(drawable.ic_loading).into(holder.icon);
        if (null != info.downcount) {
            holder.count.setVisibility(View.VISIBLE);

            try {
                double count = Double.valueOf(info.downcount);
                holder.count.setText(Utils.downloadNum(count));
            } catch (NumberFormatException var7) {
                holder.count.setText(info.downcount);
            }
        } else {
            holder.count.setVisibility(View.GONE);
        }

        try {
            holder.size.setText(Utils.readableFileSize(info.size));
        } catch (NumberFormatException var6) {
            holder.size.setText(info.size);
        }

        holder.version.setText(Utils.versionName(info.versionname));
        int apkStatus = ApkUtils.getStatus(this.context, info.appid, info.apk, Integer.valueOf(info.versioncode));
        holder.down.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                MoreAdapter.this.x = (int)motionEvent.getX();
                MoreAdapter.this.y = (int)motionEvent.getY();
                return false;
            }
        });
        holder.down.setTag(info);
        switch(apkStatus) {
            case -1:
                holder.down.setText(string.downloading);
                holder.down.setClickable(false);
                break;
            case 0:
                holder.down.setText(string.download);
                holder.down.setClickable(true);
                holder.down.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        holder.down.setClickable(false);
                        StoreApkInfo tag = (StoreApkInfo)view.getTag();
                        holder.down.setText(string.downloading);
                        MoreAdapter.this.downAndSave(tag, true);
                    }
                });
                break;
            case 1:
                holder.down.setText(string.update);
                holder.down.setClickable(true);
                holder.down.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        holder.down.setClickable(false);
                        StoreApkInfo tag = (StoreApkInfo)view.getTag();
                        holder.down.setText(string.downloading);
                        MoreAdapter.this.downAndSave(tag, false);
                    }
                });
                break;
            case 2:
                holder.down.setText(string.install);
                holder.down.setClickable(true);
                holder.down.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        StoreApkInfo tag = (StoreApkInfo)view.getTag();
                        ApkUtils.tryInstall(MoreAdapter.this.context, new File(DownloadLogic.buildUrl(MoreAdapter.this.context, tag.appname)));
                    }
                });
                break;
            case 3:
                holder.down.setText(string.open);
                holder.down.setClickable(true);
                holder.down.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        StoreApkInfo tag = (StoreApkInfo)view.getTag();
                        ApkUtils.startApp(MoreAdapter.this.context, tag.apk);
                    }
                });
        }

    }

    private void downAndSave(final StoreApkInfo tag, boolean report) {
        if (1 == this.appListInfo.flag_download) {
            for(int i = 0; i < tag.rpt_cd.size(); ++i) {
                if (i == 0) {
                    DownloadCart.getInstance().setApkStatus(tag.appid, -1);
                    DownloadStatus status = new DownloadStatus(0L, 0L, 0.0F, tag.icon, tag.appname, tag.href_download, tag.appid, tag.apk, tag.versioncode, tag.rpt_dc, tag.rpt_dl, this.appListInfo.rtp_method);
                    DownloadCart.getInstance().setApkCarDownloadStatus(tag.appid, status);
                    ReportLogic.report(this.context, this.appListInfo.rtp_method, (String)tag.rpt_cd.get(i), false, 0L, new StringCallback() {
                        public void onSuccess(Response<String> response) {
                            String body = (String)response.body();
                            if (null != body) {
                                RptBean rptBean = (RptBean)MoreAdapter.this.gson.fromJson(body, RptBean.class);
                                if (null != rptBean && null != rptBean.href_download) {
                                    DownloadLogic.getInstance().startDownload(MoreAdapter.this.context, rptBean.href_download, tag.appname, tag.appid, tag.icon, tag.apk, tag.versioncode, tag.rpt_dc, tag.rpt_dl, MoreAdapter.this.appListInfo.rtp_method);
                                    PublicDao.insert(MoreAdapter.this.buildDmBean(tag, rptBean.href_download));
                                    return;
                                }
                            }

                            DownloadCart.getInstance().remove(tag.appid);
                            DownloadCart.getInstance().removeDownloadStatus(tag.appid);
                            Toast.makeText(Application.getContext(), string.down_failed, Toast.LENGTH_SHORT).show();
                        }

                        public void onError(Response<String> response) {
                            super.onError(response);
                            DownloadCart.getInstance().remove(tag.appid);
                            DownloadCart.getInstance().removeDownloadStatus(tag.appid);
                            Toast.makeText(Application.getContext(), string.down_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    ReportLogic.report(this.context, this.appListInfo.rtp_method, (String)tag.rpt_cd.get(i), false, 0L, (StringCallback)null);
                }
            }
        } else {
            if (report) {
                ReportLogic.report(this.context, this.appListInfo.rtp_method, tag.rpt_cd, this.appListInfo.flag_replace, new ClickInfo(this.x, this.y));
            }

            DownloadLogic.getInstance().startDownload(this.context, tag.href_download, tag.appname, tag.appid, tag.icon, tag.apk, tag.versioncode, tag.rpt_dc, tag.rpt_dl, this.appListInfo.rtp_method);
            PublicDao.insert(this.buildDmBean(tag, (String)null));
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
        dmBean.method = this.appListInfo.rtp_method;
        return dmBean;
    }

    public int getItemCount() {
        return appList.size();
    }

    class MoreHolder extends ViewHolder {
        TextView appName;
        TextView down;
        TextView count;
        TextView size;
        TextView version;
        ImageView icon;

        MoreHolder(View itemView) {
            super(itemView);
            this.appName = (TextView)itemView.findViewById(id.app_name);
            this.down = (TextView)itemView.findViewById(id.app_down);
            this.count = (TextView)itemView.findViewById(id.app_count);
            this.size = (TextView)itemView.findViewById(id.app_size);
            this.version = (TextView)itemView.findViewById(id.app_version);
            this.icon = (ImageView)itemView.findViewById(id.app_icon);
        }
    }
}
