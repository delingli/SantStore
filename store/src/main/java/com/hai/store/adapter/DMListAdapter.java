package com.hai.store.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hai.store.R;
import com.hai.store.data.DownloadCart;
import com.hai.store.data.DownloadLogic;
import com.hai.store.data.ReportLogic;
import com.hai.store.fragment.MoreListFragment;
import com.hai.store.sqlite.PublicDao;
import com.hai.store.utils.ApkUtils;
import com.hai.store.utils.Utils;
import com.hai.store.view.NumberProgressBar;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DMListAdapter extends RecyclerView.Adapter<DMListAdapter.DMListHolder> {

    private Context context;
    private List<String> statusList = new ArrayList<>();

    public DMListAdapter(Context context) {
        this.context = context;
        Map<String, DownloadCart.DownloadStatus> statusMap = DownloadCart.getInstance().getApkCarDownloadStatus();
        for (String appId : statusMap.keySet()) {
            statusList.add(appId);
        }
    }

    public void upDateStatus(List<String> status) {
        statusList = status;
        notifyDataSetChanged();
    }

    public void upDateStatus(String appId) {
        int i = statusList.indexOf(appId);
        notifyItemChanged(i);
    }

    public void upDateStatus() {
        notifyDataSetChanged();
    }

    @Override
    public DMListAdapter.DMListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DMListHolder(LayoutInflater.from(context).inflate(R.layout.item_dm_list, parent, false));
    }

    @Override
    public void onBindViewHolder(final DMListAdapter.DMListHolder holder, int position) {
        final String appId = statusList.get(position);
        if (null == appId) return;
        DownloadCart.DownloadStatus status = DownloadCart.getInstance().getApkCarDownloadStatus(appId);
        if (null == status) return;
        holder.itemView.setTag(status);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final DownloadCart.DownloadStatus tag = (DownloadCart.DownloadStatus) view.getTag();
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setItems(new String[]{context.getString(R.string.delete)}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DownloadLogic.getInstance().stopDownload(tag.downUrl);
                        DownloadCart.getInstance().remove(tag.appId);
                        DownloadCart.getInstance().removeDownloadStatus(tag.appId);
                        statusList.remove(tag.appId);
                        deleteFile(DownloadLogic.buildUrl(context, tag.appName));
                        PublicDao.delete(tag.packageName);
                        ReportLogic.report(context, tag.rtp_method, tag.rpt_dl, false, 0, null);
                        notifyDataSetChanged();
                        if (null != deleteListener)
                            deleteListener.onDeleteListener();
                    }
                });
                builder.create().show();
                return false;
            }
        });
        holder.appName.setText(status.appName);
        holder.progressBar.setProgress(Math.round(status.fraction * 100));
        int apkStatus = DownloadCart.getInstance().getApkStatus(status.appId);
        if (ApkUtils.INSTALL == apkStatus) {
            holder.icon.setImageDrawable(ApkUtils.getApkIcon(DownloadLogic.buildUrl(context, status.appName), context));
        } else if (ApkUtils.OPEN == apkStatus || ApkUtils.UPDATE == apkStatus) {
            holder.icon.setImageDrawable(ApkUtils.getAppDrawable(context, status.packageName));
        } else {
            Picasso.with(context).load(status.iconUrl).placeholder(R.drawable.ic_loading).error(R.drawable.ic_loading).into(holder.icon);
        }
        holder.install.setTag(status);
        switch (apkStatus) {
            case ApkUtils.INSTALL:
                holder.install.setText(R.string.install);
                holder.install.setClickable(true);
                holder.install.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DownloadCart.DownloadStatus tag = (DownloadCart.DownloadStatus) view.getTag();
                        ApkUtils.blueInstall(context,new File( DownloadLogic.buildUrl(context, tag.appName)), MoreListFragment.IA);
//                        ApkUtils.install(context, DownloadLogic.buildUrl(context, tag.appName));
                    }
                });
                break;
            case ApkUtils.DOWNLOAD:
                holder.df.setVisibility(View.VISIBLE);
                holder.install.setText(R.string.re_down);
                holder.install.setClickable(true);
                holder.install.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.install.setClickable(false);
                        holder.df.setVisibility(View.GONE);
                        DownloadCart.DownloadStatus tag = (DownloadCart.DownloadStatus) view.getTag();
                        holder.install.setText(R.string.downloading);
                        DownloadLogic.getInstance().startDownload(context, tag.downUrl, tag.appName,
                                tag.appId, tag.iconUrl, tag.packageName, tag.versionCode, tag.rpt_dc,
                                tag.rpt_dl, tag.rtp_method);
                    }
                });
                break;
            case ApkUtils.DOWNLOADING:
                holder.install.setText(R.string.downloading);
                holder.install.setClickable(false);
                break;
            case ApkUtils.OPEN:
            case ApkUtils.UPDATE:
                holder.install.setText(R.string.open);
                holder.install.setClickable(true);
                holder.install.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DownloadCart.DownloadStatus tag = (DownloadCart.DownloadStatus) view.getTag();
                        ApkUtils.startApp(context, tag.packageName);
                    }
                });
                break;
            default:
                break;
        }

        holder.size.setText(Utils.readableFileSize(String.valueOf(status.currentSize)) + "/" + Utils.readableFileSize(String.valueOf(status.totalSize)));
    }

    @Override
    public int getItemCount() {
        return statusList.size();
    }

    static class DMListHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        NumberProgressBar progressBar;
        TextView appName, size, df, install;

        DMListHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.app_icon);
            appName = (TextView) itemView.findViewById(R.id.app_name);
            progressBar = (NumberProgressBar) itemView.findViewById(R.id.dm_Progress);
            size = (TextView) itemView.findViewById(R.id.size);
            df = (TextView) itemView.findViewById(R.id.df);
            install = (TextView) itemView.findViewById(R.id.install);
        }
    }

    private void deleteFile(String filePath) {
        ApkUtils.deleteFile(filePath);
    }

    private DeleteListener deleteListener;

    public interface DeleteListener {
        void onDeleteListener();
    }

    public void setOnDeleteListener(DeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }
}
