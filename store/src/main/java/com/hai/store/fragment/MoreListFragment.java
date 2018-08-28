package com.hai.store.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hai.store.Application;
import com.hai.store.R;
import com.hai.store.activity.DMActivity;
import com.hai.store.activity.SearchActivity;
import com.hai.store.adapter.MoreAdapter;
import com.hai.store.base.SConstant;
import com.hai.store.bean.DmBean;
import com.hai.store.bean.StoreApkInfo;
import com.hai.store.bean.StoreListInfo;
import com.hai.store.data.DownloadLogic;
import com.hai.store.data.MoreListLogic;
import com.hai.store.data.ReportLogic;
import com.hai.store.sqlite.PublicDao;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * create by ldl2018/8/22 0022
 */

public class MoreListFragment extends BaseFragment implements DownloadLogic.DownloadListener {

    public static final String MORE = "more";
    public static final String TITLE = "title";
    public static final String DOWN_LIST = "DOWN_LIST";
    private RecyclerView mRecyclerView;
    private TextView mTitle;
    private Button reload;
    private RelativeLayout errorView;
    private ProgressBar progressBar;
    private ImageView gotoDM;
    private SwipeRefreshLayout refreshLayout;
    private String mModeReq = SConstant.TMODE_ICON;
    private StoreListInfo listInfo;
    private MoreAdapter moreAdapter;
    private boolean mLoading = false;
    private RelativeLayout searchHome;
    public Map<String, List<String>> exposureId = new HashMap<>();
    public static String IA_VALUE = "is_blue";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_more_list, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setLogic();
        loadData();
    }


    @Override
    public void findView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.more_list);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.more_toolbar);
//        toolbar.setNavigationIcon(R.drawable.ic_home);
        searchHome = (RelativeLayout) view.findViewById(R.id.search_home);
//        toolbar.setNavigationIcon(-1);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(getActivity(), "^_^", Toast.LENGTH_SHORT).show();
//            }
//        });
        gotoDM = (ImageView) view.findViewById(R.id.goto_dm);
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.more_swipe);
        progressBar = (ProgressBar) view.findViewById(R.id.progress);
        errorView = (RelativeLayout) view.findViewById(R.id.error_view);
        reload = (Button) view.findViewById(R.id.btn_reload);
        mTitle = (TextView) view.findViewById(R.id.more_title);
    }

    @Override
    public void setLogic() {

        gotoDM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), DMActivity.class));
            }
        });
//        Intent intent = getIntent();
        Bundle more = getArguments();
        if (null != more) {
            String title = more.getString(TITLE);
            if (null != title) {
                mTitle.setText(title);
            }
            String tMode = more.getString(SConstant.LIST_MODE);
            if (null != tMode) {
                mModeReq = tMode;
            }
            handleDownList(more.getString(DOWN_LIST, null));
        }

    }

    @Override
    public void loadData() {
        showLoading();
        MoreListLogic.getAppList(getActivity(), null, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                if (handleData(response.body())) {
                    showView();
                } else {
                    showError();
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                showError();
            }
        }, mModeReq);
    }

    private OnMovieListScrollListener mListener;

    public interface OnMovieListScrollListener {
        void onMovieListScrolled(int distance, int offset);
    }

    public void addOnMovieListScrollListener(OnMovieListScrollListener mlistener) {
        this.mListener = mlistener;
    }

    private int mDistanceY = 0;

    @Override
    public void showView() {
        progressBar.setVisibility(View.GONE);
        refreshLayout.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(moreAdapter = new MoreAdapter(listInfo, getActivity(), mModeReq));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView view, int state) {
                RecyclerView.LayoutManager layoutManager = view.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();


                int lastVisibleItem = ((LinearLayoutManager) view.getLayoutManager()).findLastVisibleItemPosition();
                if (!mLoading && visibleItemCount > 0 && state == SCROLL_STATE_IDLE && lastVisibleItem >= totalItemCount - 1) {
                    mLoading = true;
                    MoreListLogic.getAppList(getActivity(), listInfo.href_next, new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            if (handleData(response.body())) {
                                moreAdapter.addData(listInfo.list);
                            } else {
                                Toast.makeText(getActivity(), R.string.not_more_data, Toast.LENGTH_SHORT).show();
                            }
                            mLoading = false;
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            if(null!=getActivity()&&getActivity().getResources()!=null){
                                Toast.makeText(getActivity(), R.string.get_data_failed, Toast.LENGTH_SHORT).show();
                            }

                            mLoading = false;
                        }
                    }, mModeReq);
                }
                view.getChildAt(0);
            }

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
                mDistanceY += dy;
                if (mListener != null) {
                    mListener.onMovieListScrolled(mDistanceY, dy);
                }
                if (half >= bottomView.getHeight() / 3) {
                    //上报最后一个item
                    reportExposure(firstVisibleItem, lastVisibleItem);
                } else {
                    reportExposure(firstVisibleItem, lastVisibleItem - 1);
                    //不上报最后一个item
                }
            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        DownloadLogic.getInstance().setDownloadListener(this);
        initSearch();
    }

    private void initSearch() {

        searchHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SearchActivity.class));
            }
        });
    }

    private void refresh() {
        MoreListLogic.getAppList(getActivity(), listInfo.href_next, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                if (handleData(response.body())) {
                    moreAdapter.setData(listInfo);
                } else {
                    Toast.makeText(getActivity(), R.string.refresh_failed, Toast.LENGTH_SHORT).show();
                }
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                showError();
                refreshLayout.setRefreshing(false);
            }
        }, mModeReq);
    }

    private void handleDownList(String down_list) {
        if (null != down_list) {
            Log.e("DOWN_LIST", down_list);
            try {
                final StoreListInfo downAction = new Gson().fromJson(down_list, StoreListInfo.class);
                if (null != downAction && downAction.list != null) {
                    EXECUTOR.execute(new Runnable() {
                        @Override
                        public void run() {
                            for (StoreApkInfo apkInfo : downAction.list) {
                                DownloadLogic.getInstance().startDownload(Application.getContext(), apkInfo.href_download, apkInfo.appname,
                                        apkInfo.appid, apkInfo.icon, apkInfo.apk, apkInfo.versioncode, apkInfo.rpt_dc, apkInfo.rpt_dl, downAction.rtp_method);
                                PublicDao.insert(buildDmBean(apkInfo, downAction.rtp_method));
                            }
                            startActivity(new Intent(getActivity(), DMActivity.class));
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), "数据解析错误", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), "数据解析错误", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void reportExposure(int first, int last) {
        for (int i = first; i <= last; i++) {
            StoreApkInfo info = MoreAdapter.appList.get(i); //此处不需要-1
            if (!exposureId.containsKey(info.appid)) {
                ReportLogic.report(getActivity(), listInfo.rtp_method, info.rpt_ss, listInfo.flag_replace, null);
                exposureId.put(info.appid, info.rpt_ss);
            }
        }
    }

    public void showLoading() {
        refreshLayout.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private DmBean buildDmBean(StoreApkInfo info, String rtp_method) {
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
        dmBean.repDel = info.rpt_dl;
        dmBean.method = rtp_method;
        return dmBean;
    }

    @Override
    public void showError() {
        progressBar.setVisibility(View.GONE);
        refreshLayout.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData();
            }
        });
    }

    private boolean handleData(String body) {
        listInfo = new Gson().fromJson(body, StoreListInfo.class);
        return null != listInfo && null == listInfo.err && listInfo.list.size() > 0;
    }

    @Override
    public void onProgressListener(String appId) {

    }

    @Override
    public void onError(String appId) {
        if (null != moreAdapter)
            moreAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart(String appId) {

    }

    @Override
    public void onSuccess(String appId) {
        if (null != moreAdapter)
            moreAdapter.notifyDataSetChanged();
    }
}
