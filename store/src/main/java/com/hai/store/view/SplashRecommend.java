package com.hai.store.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hai.store.Application;
import com.hai.store.R;
import com.hai.store.adapter.OneAppHolder;
import com.hai.store.bean.DmBean;
import com.hai.store.bean.StoreApkInfo;
import com.hai.store.bean.StoreListInfo;
import com.hai.store.data.DownloadLogic;
import com.hai.store.data.ReportLogic;
import com.hai.store.utils.Device;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SplashRecommend extends DialogFragment {

    private static final String TAG = "SplashRecommend";
    private TextView mInstall, mChange, mCancel;
    private RecyclerView mContent;
    private StoreListInfo mStoreListInfo;
    private Adapter mAdapter;
    private StoreLoadResourceListener mLoadListener;

    public void setStoreLoadResourceListener(StoreLoadResourceListener listener) {
        mLoadListener = listener;
    }

    public void setData(StoreListInfo storeListInfo) {
        mStoreListInfo = storeListInfo;
    }

    public void reLoad() {
        if (mAdapter != null) mAdapter.upDateStatus(mStoreListInfo.list);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dc_frame_splash_recommend, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Window dialogWindow = getDialog().getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();
        params.width = Device.getScreenSize(Application.getContext())[0] - Device.dp2px(Application.getContext(), 20);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        params.y = Device.dp2px(Application.getContext(), 10);
        dialogWindow.setAttributes(params);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findView(view);
        if (null != mStoreListInfo) {
            showData();
        }
    }

    private void showData() {
        if (null == mAdapter) {
            mContent.setLayoutManager(new GridLayoutManager(getActivity(), 3));
            mContent.setAdapter(mAdapter = new Adapter(getActivity(), mStoreListInfo));
            mContent.setItemAnimator(null);
            mInstall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<StoreApkInfo> checkApp = mAdapter.getCheckApp();
                    if (checkApp.size() > 0) {
                        List<DmBean> list = buildDMBeanList(checkApp);
                        DownloadLogic.getInstance().addQueue(list);
                        dismiss();
                    } else {
                        Toast.makeText(getActivity(), "请选择应用", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            mCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mAdapter) mAdapter.unCheckAll();
                }
            });
            mChange.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mLoadListener) {
                        mLoadListener.onReload();
                    }
                }
            });
        } else {
            mAdapter.upDateStatus(mStoreListInfo.list);
        }
    }

    private List<DmBean> buildDMBeanList(List<StoreApkInfo> apkInfos) {
        List<DmBean> list = new ArrayList<>();
        for (StoreApkInfo apkInfo : apkInfos) {
            list.add(buildDMBean(apkInfo));
        }
        return list;
    }

    private DmBean buildDMBean(StoreApkInfo apkInfo) {
        DmBean dmBean = new DmBean(apkInfo.appid, apkInfo.appname, apkInfo.apk, apkInfo.versioncode,
                apkInfo.versionname, apkInfo.size, apkInfo.icon, apkInfo.href_download, apkInfo.rpt_dc,
                apkInfo.rpt_ic, apkInfo.rpt_ac, apkInfo.rpt_dl, mStoreListInfo.rtp_method);
        ReportLogic.report(Application.getContext(), mStoreListInfo.rtp_method, apkInfo.rpt_cd,
                mStoreListInfo.flag_replace, null);
        return dmBean;
    }

    private void findView(View view) {
        mInstall = (TextView) view.findViewById(R.id.splash_install);
        mCancel = (TextView) view.findViewById(R.id.splash_un_all_check);
        mChange = (TextView) view.findViewById(R.id.splash_change);
        ImageView mClose = (ImageView) view.findViewById(R.id.splash_close);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (null != mLoadListener) mLoadListener.onExit();
            }
        });
        mContent = (RecyclerView) view.findViewById(R.id.splash_recycle);
    }

    private static class Adapter extends RecyclerView.Adapter<OneAppHolder> {

        private Context mContext;
        private StoreListInfo mListInfo;
        private List<StoreApkInfo> mInfoList;
        private SparseBooleanArray checkPosition;
        private LayoutInflater mFrom;
        private Set<String> showReport;

        private Adapter(Context context, StoreListInfo listInfo) {
            mContext = context;
            checkPosition = new SparseBooleanArray();
            showReport = new HashSet<>();
            mFrom = LayoutInflater.from(mContext);
            mListInfo = listInfo;
            mInfoList = listInfo.list;
            for (int i = 0; i < mInfoList.size(); i++) {
                checkPosition.put(i, true);
            }
        }

        //一键安装
        private List<StoreApkInfo> getCheckApp() {
            List<StoreApkInfo> checkList = new ArrayList<>();
            for (int i = 0; i < mInfoList.size(); i++) {
                if (i >= 9) break; //返回不能超过9个
                if (checkPosition.get(i)) {
                    checkList.add(mInfoList.get(i));
                }
            }
            return checkList;
        }

        //换一批
        private void upDateStatus(List<StoreApkInfo> infoList) {
            checkPosition.clear();
            mInfoList = infoList;
            for (int i = 0; i < mInfoList.size(); i++) {
                checkPosition.put(i, true);
            }
            notifyDataSetChanged();
        }

        //取消选择
        private void unCheckAll() {
            for (int i = 0; i < checkPosition.size(); i++) {
                checkPosition.put(i, false);
            }
            notifyDataSetChanged();
        }

        @Override
        public OneAppHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new OneAppHolder(mFrom.inflate(R.layout.item_grid_recommend, parent, false));
        }

        @Override
        public void onBindViewHolder(final OneAppHolder holder, int position) {
            StoreApkInfo apkInfo = mInfoList.get(position);
            holder.appCheck.setChecked(checkPosition.get(position));
            holder.appCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checkPosition.put(holder.getAdapterPosition(), isChecked);
                }
            });
            holder.appName.setText(apkInfo.appname);
            Picasso.with(mContext).load(apkInfo.icon).placeholder(R.drawable.ic_loading).error(R.drawable.ic_loading).into(holder.appIcon);
            if (!showReport.contains(apkInfo.appid)) {
                showReport.add(apkInfo.appid);
                ReportLogic.report(Application.getContext(), mListInfo.rtp_method, apkInfo.rpt_ss, mListInfo.flag_replace, null);
            }
        }

        @Override
        public int getItemCount() {
            return null == mInfoList ? 0 : (mInfoList.size() > 9 ? 9 : mInfoList.size());
        }
    }
}
