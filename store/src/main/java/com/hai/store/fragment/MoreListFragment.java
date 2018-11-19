//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hai.store.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hai.store.Application;
import com.hai.store.R.id;
import com.hai.store.R.layout;
import com.hai.store.R.string;
import com.hai.store.activity.DMActivity;
import com.hai.store.activity.SearchActivity;
import com.hai.store.adapter.MoreAdapter;
import com.hai.store.base.SConstant;
import com.hai.store.bean.ClickInfo;
import com.hai.store.bean.DmBean;
import com.hai.store.bean.StoreApkInfo;
import com.hai.store.bean.StoreListInfo;
import com.hai.store.data.DownloadLogic;
import com.hai.store.data.MoreListLogic;
import com.hai.store.data.ReportLogic;
import com.hai.store.data.DownloadLogic.DownloadListener;
import com.hai.store.mildperate.MildOperatorConfig;
import com.hai.store.mildperate.MildperateConstant;
import com.hai.store.sqlite.PublicDao;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.sant.api.APIError;
import com.sant.api.Api;
import com.sant.api.Callback;
import com.sant.api.common.ADDAPPStore;
import com.sant.api.common.ADData;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MoreListFragment extends BaseFragment implements DownloadListener {
    public static final String MORE = "more";
    public static final String TITLE = "title";
    public static final String ISSHOW_SEARCH_RESULT = "isshow_search_result";
    public static final String ISSHOW_SEARCH_RECOMMER = "isshow_search_recommer";
    public static final String DOWN_LIST = "DOWN_LIST";
    private RecyclerView mRecyclerView;
    private TextView mTitle;
    private Button reload;
    private RelativeLayout errorView;
    private ProgressBar progressBar;
    private ImageView gotoDM;
    private SwipeRefreshLayout refreshLayout;
    private String mModeReq = "icon";
    private StoreListInfo listInfo;
    private MoreAdapter moreAdapter;
    private boolean mLoading = false;
    private RelativeLayout searchHome;
    public Map<String, List<String>> exposureId = new HashMap();
    public static String IA_VALUE = "is_blue";
    private MildOperatorConfig config = new MildOperatorConfig();
    private MoreListFragment.OnMovieListScrollListener mListener;
    private int mDistanceY = 0;

    public MoreListFragment() {
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(layout.fragment_more_list, container, false);
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setLogic();
        this.initSearch();
    }

    public void findView(View view) {
        this.mRecyclerView = (RecyclerView) view.findViewById(id.more_list);
        Toolbar toolbar = (Toolbar) view.findViewById(id.more_toolbar);
        this.searchHome = (RelativeLayout) view.findViewById(id.search_home);
        this.gotoDM = (ImageView) view.findViewById(id.goto_dm);
        this.refreshLayout = (SwipeRefreshLayout) view.findViewById(id.more_swipe);
        this.progressBar = (ProgressBar) view.findViewById(id.progress);
        this.errorView = (RelativeLayout) view.findViewById(id.error_view);
        this.reload = (Button) view.findViewById(id.btn_reload);
        this.mTitle = (TextView) view.findViewById(id.more_title);
    }

    public void setLogic() {
        this.gotoDM.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                MoreListFragment.this.startActivity(new Intent(MoreListFragment.this.getActivity(), DMActivity.class));
            }
        });
        Bundle more = this.getArguments();
        if (null != more) {
            String title = more.getString("title");
            if (null != title) {
                this.mTitle.setText(title);
            }

            String tMode = more.getString("list_mode");
            if (null != tMode) {
                this.mModeReq = tMode;
            }

            this.handleDownList(more.getString("DOWN_LIST", (String) null));
        }

        Api.common(this.getContext()).fetchADAPPStore(MildperateConstant.APP_DISCOVER,  null, new Callback<ADData>() {
            public void onFinish(boolean isSucess, ADData adData, APIError apiError, Object o) {
                String market = "";
                if (isSucess) {
                    if (adData != null && adData instanceof ADDAPPStore) {
                        ADDAPPStore addappstore = (ADDAPPStore) adData;
                        market = addappstore.market;
                    }
                } else {
                    Log.d("ldl", "竹蜻蜓没有配置了广告");
                }


                config.market = market;
                loadData();
            }
        });
    }

    public void loadData() {
        Log.d("ldl", "加载数据...");
        if (this.config != null) {
            this.toLoadData(this.config.market);
        } else {
            this.showError();
        }

    }

    private void toLoadData(String market) {
        this.showLoading();
        MoreListLogic.getAppList(this.getActivity(), null, SConstant.CID_APP_LIST, market, new StringCallback() {
            public void onSuccess(Response<String> response) {
                if (MoreListFragment.this.handleData(response.body())) {
                    MoreListFragment.this.showView();
                } else {
                    MoreListFragment.this.showError();
                }

            }

            public void onError(Response<String> response) {
                super.onError(response);
                MoreListFragment.this.showError();
            }
        }, this.mModeReq);
    }

    public void addOnMovieListScrollListener(MoreListFragment.OnMovieListScrollListener mlistener) {
        this.mListener = mlistener;
    }

    public void showView() {
        this.progressBar.setVisibility(View.GONE);
        this.refreshLayout.setVisibility(View.VISIBLE);
        this.errorView.setVisibility(View.GONE);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        this.mRecyclerView.setAdapter(this.moreAdapter = new MoreAdapter(this.listInfo, this.getActivity(), this.mModeReq));
        this.mRecyclerView.addOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(RecyclerView view, int state) {
                LayoutManager layoutManager = view.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItem = ((LinearLayoutManager) view.getLayoutManager()).findLastVisibleItemPosition();
                if (!MoreListFragment.this.mLoading && visibleItemCount > 0 && state == 0 && lastVisibleItem >= totalItemCount - 1) {
                    MoreListFragment.this.mLoading = true;
                    if (MoreListFragment.this.config != null) {
                        MoreListLogic.getAppList(MoreListFragment.this.getActivity(), MoreListFragment.this.listInfo.href_next, SConstant.CID_APP_LIST, config.market, new StringCallback() {
                            public void onSuccess(Response<String> response) {
                                if (MoreListFragment.this.handleData((String) response.body())) {
                                    MoreListFragment.this.moreAdapter.addData(MoreListFragment.this.listInfo.list);
                                } else {
                                    Toast.makeText(MoreListFragment.this.getActivity(), string.not_more_data, View.VISIBLE).show();
                                }

                                MoreListFragment.this.mLoading = false;
                            }

                            public void onError(Response<String> response) {
                                super.onError(response);
                                if (null != MoreListFragment.this.getActivity() && MoreListFragment.this.getActivity().getResources() != null) {
                                    Toast.makeText(MoreListFragment.this.getActivity(), string.get_data_failed, View.VISIBLE).show();
                                }

                                MoreListFragment.this.mLoading = false;
                            }
                        }, MoreListFragment.this.mModeReq);
                    }
                }

                view.getChildAt(0);
            }

            public void onScrolled(RecyclerView view, int dx, int dy) {
                int lastVisibleItem = ((LinearLayoutManager) view.getLayoutManager()).findLastVisibleItemPosition();
                int firstVisibleItem = ((LinearLayoutManager) view.getLayoutManager()).findFirstVisibleItemPosition();
                int itemNumber = lastVisibleItem - firstVisibleItem;
                View bottomView = view.getChildAt(itemNumber);
                int total = view.getHeight();
                int bottomViewTop = bottomView.getTop();
                int half = total - bottomViewTop;
                MoreListFragment.this.mDistanceY = MoreListFragment.this.mDistanceY + dy;
                if (MoreListFragment.this.mListener != null) {
                    MoreListFragment.this.mListener.onMovieListScrolled(MoreListFragment.this.mDistanceY, dy);
                }

                if (half >= bottomView.getHeight() / 3) {
                    MoreListFragment.this.reportExposure(firstVisibleItem, lastVisibleItem);
                } else {
                    MoreListFragment.this.reportExposure(firstVisibleItem, lastVisibleItem - 1);
                }

            }
        });
        this.refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            public void onRefresh() {
                MoreListFragment.this.refresh();
            }
        });
        DownloadLogic.getInstance().setDownloadListener(this);
    }

    private void initSearch() {
        this.searchHome.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MoreListFragment.this.getActivity(), SearchActivity.class);
                intent.putExtra(SearchActivity.EXTRA_TAG, SearchActivity.download_app);
                MoreListFragment.this.startActivity(intent);
            }
        });
    }

    private void refresh() {
        if (this.config != null) {
            MoreListLogic.getAppList(this.getActivity(), this.listInfo.href_next, SConstant.CID_APP_LIST, this.config.market, new StringCallback() {
                public void onSuccess(Response<String> response) {
                    if (handleData(response.body())) {
                        moreAdapter.setData(MoreListFragment.this.listInfo);
                    } else {
                        Toast.makeText(MoreListFragment.this.getActivity(), string.refresh_failed, Toast.LENGTH_SHORT).show();
                    }

                   refreshLayout.setRefreshing(false);
                }

                public void onError(Response<String> response) {
                    super.onError(response);
                    MoreListFragment.this.showError();
                    MoreListFragment.this.refreshLayout.setRefreshing(false);
                }
            }, this.mModeReq);
        }

    }

    private void handleDownList(String downListJson) {
        if (null != downListJson) {
            Log.e("DOWN_LIST", downListJson);

            try {
                final StoreListInfo downAction = (StoreListInfo) (new Gson()).fromJson(downListJson, StoreListInfo.class);
                if (null != downAction && downAction.list != null) {
                    EXECUTOR.execute(new Runnable() {
                        public void run() {
                            Iterator var1 = downAction.list.iterator();

                            while (var1.hasNext()) {
                                StoreApkInfo apkInfo = (StoreApkInfo) var1.next();
                                DownloadLogic.getInstance().startDownload(Application.getContext(), apkInfo.href_download, apkInfo.appname, apkInfo.appid, apkInfo.icon, apkInfo.apk, apkInfo.versioncode, apkInfo.rpt_dc, apkInfo.rpt_dl, downAction.rtp_method);
                                PublicDao.insert(MoreListFragment.this.buildDmBean(apkInfo, downAction.rtp_method));
                            }

                            MoreListFragment.this.startActivity(new Intent(MoreListFragment.this.getActivity(), DMActivity.class));
                        }
                    });
                } else {
                    Toast.makeText(this.getActivity(), "数据解析错误", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception var3) {
                Toast.makeText(this.getActivity(), "数据解析错误", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void reportExposure(int first, int last) {
        for (int i = first; i <= last; ++i) {
            StoreApkInfo info = (StoreApkInfo) MoreAdapter.appList.get(i);
            if (!this.exposureId.containsKey(info.appid)) {
                ReportLogic.report(this.getActivity(), this.listInfo.rtp_method, info.rpt_ss, this.listInfo.flag_replace, (ClickInfo) null);
                this.exposureId.put(info.appid, info.rpt_ss);
            }
        }

    }

    public void showLoading() {
        this.refreshLayout.setVisibility(View.GONE);
        this.errorView.setVisibility(View.GONE);
        this.progressBar.setVisibility(View.VISIBLE);
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

    public void showError() {
        this.progressBar.setVisibility(View.GONE);
        this.refreshLayout.setVisibility(View.GONE);
        this.errorView.setVisibility(View.VISIBLE);
        this.reload.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                MoreListFragment.this.loadData();
            }
        });
    }

    private boolean handleData(String body) {
        this.listInfo = (StoreListInfo) (new Gson()).fromJson(body, StoreListInfo.class);
        return null != this.listInfo && null == this.listInfo.err && this.listInfo.list.size() > 0;
    }

    public void onProgressListener(String appId) {
    }

    public void onError(String appId) {
        if (null != this.moreAdapter) {
            this.moreAdapter.notifyDataSetChanged();
        }

    }

    public void onStart(String appId) {
    }

    public void onSuccess(String appId) {
        if (null != this.moreAdapter) {
            this.moreAdapter.notifyDataSetChanged();
        }

    }

    public interface OnMovieListScrollListener {
        void onMovieListScrolled(int var1, int var2);
    }
}
