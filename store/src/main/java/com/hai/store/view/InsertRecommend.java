package com.hai.store.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.hai.store.Application;
import com.hai.store.R;
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
import java.util.List;

public class InsertRecommend extends DialogFragment {

    private static final String TAG = "InsertRecommend";
    private RecyclerView mContent;
    private Button mInstall;
    private Adapter mAdapter;
    private StoreListInfo mStoreListInfo;
    private StoreLoadResourceListener mLoadListener;
    private int x;
    private int y;

    public void setStoreLoadResourceListener(StoreLoadResourceListener listener) {
        mLoadListener = listener;
    }

    public void setData(StoreListInfo storeListInfo) {
        mStoreListInfo = storeListInfo;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dc_frame_insert_recommend, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        findView(view);
        showData();
    }

    private void showData() {
        mContent.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mContent.setAdapter(mAdapter = new Adapter(getActivity(), mStoreListInfo));
        mContent.setItemAnimator(null);
        mInstall.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                x = (int) motionEvent.getX();
                y = (int) motionEvent.getY();
                return false;
            }
        });
        mInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<StoreApkInfo> checkApp = mAdapter.getCheckApp();
                if (checkApp.size() > 0) {
                    List<DmBean> list = buildDMBeanList(checkApp,new ClickInfo(x,y));
                    DownloadLogic.getInstance().addQueue(list);
                    dismiss();
                } else {
                    Toast.makeText(getActivity(), "请选择应用", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private List<DmBean> buildDMBeanList(List<StoreApkInfo> apkInfos,ClickInfo clickInfo) {
        List<DmBean> list = new ArrayList<>();
        for (StoreApkInfo apkInfo : apkInfos) {
            list.add(buildDMBean(apkInfo,clickInfo));
        }
        return list;
    }

    private DmBean buildDMBean(StoreApkInfo apkInfo,ClickInfo clickInfo) {
        DmBean dmBean = new DmBean(apkInfo.appid, apkInfo.appname, apkInfo.apk, apkInfo.versioncode,
                apkInfo.versionname, apkInfo.size, apkInfo.icon, apkInfo.href_download, apkInfo.rpt_dc,
                apkInfo.rpt_ic, apkInfo.rpt_ac, apkInfo.rpt_dl, mStoreListInfo.rtp_method);
        ReportLogic.report(Application.getContext(), mStoreListInfo.rtp_method, apkInfo.rpt_cd,
                mStoreListInfo.flag_replace, clickInfo);
        return dmBean;
    }

    private void findView(View view) {
        Button mExit = (Button) view.findViewById(R.id.exit);
        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                mLoadListener.onExit();
            }
        });
        mInstall = (Button) view.findViewById(R.id.install);
        mContent = (RecyclerView) view.findViewById(R.id.content);
    }

//    @Override
//    public void requestData() {
//        StoreApi.requestAppList(Application.getContext(), "-15", TAG, new StringCallback() {
//            @Override
//            public void onSuccess(Response<String> response) {
//                handlerData(response.body());
//            }
//
//            @Override
//            public void onError(Response<String> response) {
//                super.onError(response);
//                dismiss();
//            }
//        });
//    }

//    @Override
//    public void handlerData(String body) {
//        try {
//            StoreListInfo sl = new Gson().fromJson(body, StoreListInfo.class);
//            if (null != sl && sl.list.size() > 0) {
//                if (null != sl.err) {
//                    Log.e(TAG, "服务器返回错误 : " + sl.err);
//                    dismiss();
//                    return;
//                }
//                List<StoreApkInfo> exist = new ArrayList<>();
//                for (StoreApkInfo apkInfo : sl.list) {
//                    if (ApkUtils.isAvailable(Application.getContext(), apkInfo.apk)) {
//                        exist.add(apkInfo);
//                    }
//                }
//                sl.list.removeAll(exist);
//                if (sl.list.size() > 0) {
//                    mStoreListInfo = sl;
//                    showData();
//                    return;
//                }
//            }
//            dismiss();
//        } catch (Exception e) {
//            dismiss();
//            Log.e(TAG, "类型解析错误");
//        }
//    }

    private class Adapter extends RecyclerView.Adapter<OneAppHolder> {

        private Context mContext;
        private StoreListInfo mListInfo;
        private List<StoreApkInfo> mInfoList;
        private SparseBooleanArray checkPosition;
        private LayoutInflater mFrom;

        private Adapter(Context context, StoreListInfo listInfo) {
            mContext = context;
            checkPosition = new SparseBooleanArray();
            mFrom = LayoutInflater.from(context);
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
                if (i >= 4) break; //返回不能超过4个
                if (checkPosition.get(i)) {
                    checkList.add(mInfoList.get(i));
                }
            }
            return checkList;
        }

        @Override
        public OneAppHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new OneAppHolder(mFrom.inflate(R.layout.item_linear_recommend, parent, false));
        }

        @Override
        public void onBindViewHolder(final OneAppHolder holder, int position) {
            StoreApkInfo apkInfo = mInfoList.get(position);
            holder.appName.setText(apkInfo.appname);
            Picasso.with(mContext).load(apkInfo.icon).placeholder(R.drawable.ic_loading).error(R.drawable.ic_loading).into(holder.appIcon);
            holder.appCheck.setChecked(checkPosition.get(position));
            holder.appCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checkPosition.put(holder.getAdapterPosition(), isChecked);
                }
            });
            ReportLogic.report(Application.getContext(), mListInfo.rtp_method, apkInfo.rpt_ss, mListInfo.flag_replace, null);
        }

        @Override
        public int getItemCount() {
            return null == mInfoList ? 0 : (mInfoList.size() > 4 ? 4 : mInfoList.size());
        }
    }
}
