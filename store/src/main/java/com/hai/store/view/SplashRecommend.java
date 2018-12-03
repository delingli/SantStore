//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hai.store.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemAnimator;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.hai.store.Application;
import com.hai.store.R.drawable;
import com.hai.store.R.id;
import com.hai.store.R.layout;
import com.hai.store.adapter.OneAppHolder;
import com.hai.store.bean.ClickInfo;
import com.hai.store.bean.DmBean;
import com.hai.store.bean.StoreApkInfo;
import com.hai.store.bean.StoreListInfo;
import com.hai.store.data.DownloadLogic;
import com.hai.store.data.ReportLogic;
import com.hai.store.utils.Device;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SplashRecommend extends DialogFragment {
    private static final String TAG = "SplashRecommend";
    private TextView mInstall;
    private TextView mChange;
    private TextView mCancel;
    private RecyclerView mContent;
    private StoreListInfo mStoreListInfo;
    private SplashRecommend.Adapter mAdapter;
    private StoreLoadResourceListener mLoadListener;

    public SplashRecommend() {
    }

    public void setStoreLoadResourceListener(StoreLoadResourceListener listener) {
        this.mLoadListener = listener;
    }

    public void setData(StoreListInfo storeListInfo) {
        this.mStoreListInfo = storeListInfo;
    }

    public void reLoad() {
        if (this.mAdapter != null) {
            this.mAdapter.upDateStatus(this.mStoreListInfo.list);
        }

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(layout.dc_frame_splash_recommend, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Window dialogWindow = this.getDialog().getWindow();
        LayoutParams params = dialogWindow.getAttributes();
        params.width = Device.getScreenSize(Application.getContext())[0] - Device.dp2px(Application.getContext(), 20);
        params.height = -2;
        params.gravity = 17;
        params.y = Device.dp2px(Application.getContext(), 10);
        dialogWindow.setAttributes(params);
    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(1);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.findView(view);
        if (null != this.mStoreListInfo) {
            this.showData();
        }

    }

    private void showData() {
        if (null == this.mAdapter) {
            this.mContent.setLayoutManager(new GridLayoutManager(this.getActivity(), 3));
            this.mContent.setAdapter(this.mAdapter = new Adapter(getActivity(), mStoreListInfo));
            this.mContent.setItemAnimator((ItemAnimator) null);
            this.mInstall.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    List<StoreApkInfo> checkApp = SplashRecommend.this.mAdapter.getCheckApp();
                    if (checkApp.size() > 0) {
                        List<DmBean> list = SplashRecommend.this.buildDMBeanList(checkApp);
                        DownloadLogic.getInstance().addQueue(list);
                        SplashRecommend.this.dismiss();
                    } else {
                        Toast.makeText(SplashRecommend.this.getActivity(), "请选择应用", Toast.LENGTH_SHORT).show();
                    }

                }
            });
            this.mCancel.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (null != SplashRecommend.this.mAdapter) {
                        SplashRecommend.this.mAdapter.unCheckAll();
                    }

                }
            });
            this.mChange.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (null != SplashRecommend.this.mLoadListener) {
                        SplashRecommend.this.mLoadListener.onReload();
                    }

                }
            });
        } else {
            this.mAdapter.upDateStatus(this.mStoreListInfo.list);
        }

    }

    private List<DmBean> buildDMBeanList(List<StoreApkInfo> apkInfos) {
        List<DmBean> list = new ArrayList();
        Iterator var3 = apkInfos.iterator();

        while (var3.hasNext()) {
            StoreApkInfo apkInfo = (StoreApkInfo) var3.next();
            list.add(this.buildDMBean(apkInfo));
        }

        return list;
    }

    private DmBean buildDMBean(StoreApkInfo apkInfo) {
        DmBean dmBean = new DmBean(apkInfo.appid, apkInfo.appname, apkInfo.apk, apkInfo.versioncode, apkInfo.versionname, apkInfo.size, apkInfo.icon, apkInfo.href_download, apkInfo.rpt_dc, apkInfo.rpt_ic, apkInfo.rpt_ac, apkInfo.rpt_dl, this.mStoreListInfo.rtp_method);
        ReportLogic.report(Application.getContext(), this.mStoreListInfo.rtp_method, apkInfo.rpt_cd, this.mStoreListInfo.flag_replace, (ClickInfo) null);
        return dmBean;
    }

    private void findView(View view) {
        this.mInstall = (TextView) view.findViewById(id.splash_install);
        this.mCancel = (TextView) view.findViewById(id.splash_un_all_check);
        this.mChange = (TextView) view.findViewById(id.splash_change);
        ImageView mClose = (ImageView) view.findViewById(id.splash_close);
        mClose.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SplashRecommend.this.dismiss();
                if (null != SplashRecommend.this.mLoadListener) {
                    SplashRecommend.this.mLoadListener.onExit();
                }

            }
        });
        this.mContent = (RecyclerView) view.findViewById(id.splash_recycle);
    }

    private static class Adapter extends android.support.v7.widget.RecyclerView.Adapter<OneAppHolder> {
        private Context mContext;
        private StoreListInfo mListInfo;
        private List<StoreApkInfo> mInfoList;
        private SparseBooleanArray checkPosition;
        private LayoutInflater mFrom;
        private Set<String> showReport;

        private Adapter(Context context, StoreListInfo listInfo) {
            this.mContext = context;
            this.checkPosition = new SparseBooleanArray();
            this.showReport = new HashSet();
            this.mFrom = LayoutInflater.from(this.mContext);
            this.mListInfo = listInfo;
            this.mInfoList = listInfo.list;

            for (int i = 0; i < this.mInfoList.size(); ++i) {
                this.checkPosition.put(i, true);
            }

        }

        private List<StoreApkInfo> getCheckApp() {
            List<StoreApkInfo> checkList = new ArrayList();

            for (int i = 0; i < this.mInfoList.size() && i < 9; ++i) {
                if (this.checkPosition.get(i)) {
                    checkList.add(this.mInfoList.get(i));
                }
            }

            return checkList;
        }

        private void upDateStatus(List<StoreApkInfo> infoList) {
            this.checkPosition.clear();
            this.mInfoList = infoList;

            for (int i = 0; i < this.mInfoList.size(); ++i) {
                this.checkPosition.put(i, true);
            }

            this.notifyDataSetChanged();
        }

        private void unCheckAll() {
            for (int i = 0; i < this.checkPosition.size(); ++i) {
                this.checkPosition.put(i, false);
            }

            this.notifyDataSetChanged();
        }

        public OneAppHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new OneAppHolder(this.mFrom.inflate(layout.item_grid_recommend, parent, false));
        }

        public void onBindViewHolder(final OneAppHolder holder, int position) {
            StoreApkInfo apkInfo = (StoreApkInfo) this.mInfoList.get(position);
            holder.appCheck.setChecked(this.checkPosition.get(position));
            holder.appCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Adapter.this.checkPosition.put(holder.getAdapterPosition(), isChecked);
                }
            });
            holder.appName.setText(apkInfo.appname);
            Picasso.with(this.mContext).load(apkInfo.icon).placeholder(drawable.ic_loading).error(drawable.ic_loading).into(holder.appIcon);
            if (!this.showReport.contains(apkInfo.appid)) {
                this.showReport.add(apkInfo.appid);
                ReportLogic.report(Application.getContext(), this.mListInfo.rtp_method, apkInfo.rpt_ss, this.mListInfo.flag_replace, (ClickInfo) null);
            }

        }

        public int getItemCount() {
            return null == this.mInfoList ? 0 : (this.mInfoList.size() > 9 ? 9 : this.mInfoList.size());
        }
    }
}
