//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hai.store.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hai.store.Application;
import com.hai.store.R.drawable;
import com.hai.store.R.id;
import com.hai.store.R.layout;
import com.hai.store.R.string;
import com.hai.store.adapter.MoreAdapter;
import com.hai.store.base.BaseActivity;
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

public class MoreListActivity extends BaseActivity implements DownloadListener {
    public static final String MORE = "more";
    public static final String TITLE = "title";
    public static final String DOWN_LIST = "DOWN_LIST";
    private RecyclerView mRecyclerView;
    private ImageView gotoDM;
    private SwipeRefreshLayout refreshLayout;
    private MoreAdapter moreAdapter;
    private ProgressBar progressBar;
    private RelativeLayout errorView;
    private Button reload;
    private TextView mTitle;
    private boolean mLoading = false;
    private StoreListInfo listInfo;
    private long startTime;
    private String mModeReq = "icon";
    public Map<String, List<String>> exposureId = new HashMap();
    private MildOperatorConfig configList = new MildOperatorConfig();

    public MoreListActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(layout.activity_more_list);
        this.findView();
        this.initSearch();
        this.setLogic();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.handleDownList(intent);
    }

    protected void onStart() {
        super.onStart();
    }

    protected void onStop() {
        super.onStop();
    }

    public void findView() {
        this.mRecyclerView = (RecyclerView) this.findViewById(id.more_list);
        Toolbar toolbar = (Toolbar) this.findViewById(id.more_toolbar);
        toolbar.setNavigationIcon(drawable.ic_home);
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Toast.makeText(MoreListActivity.this, "^_^", Toast.LENGTH_SHORT).show();
            }
        });
        this.gotoDM = (ImageView) this.findViewById(id.goto_dm);
        this.refreshLayout = (SwipeRefreshLayout) this.findViewById(id.more_swipe);
        this.progressBar = (ProgressBar) this.findViewById(id.progress);
        this.errorView = (RelativeLayout) this.findViewById(id.error_view);
        this.reload = (Button) this.findViewById(id.btn_reload);
        this.mTitle = (TextView) this.findViewById(id.more_title);
    }

    public void setLogic() {
        this.gotoDM.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                MoreListActivity.this.startActivity(new Intent(MoreListActivity.this, DMActivity.class));
            }
        });
        Intent intent = this.getIntent();
        String tMode = intent.getStringExtra("list_mode");
        if (null != tMode) {
            this.mModeReq = tMode;
        }

        Bundle more = intent.getBundleExtra("more");
        if (null != more) {
            String title = more.getString("title");
            if (null != title) {
                this.mTitle.setText(title);
            }
        }

        Api.common(this).fetchADAPPStore(MildperateConstant.APP_HOT, null, new Callback<ADData>() {
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

                configList.market = market;
                loadData();
            }
        });
        this.handleDownList(intent);
    }

    private void handleDownList(Intent intent) {
        String downListJson = intent.getStringExtra("DOWN_LIST");
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
                                PublicDao.insert(MoreListActivity.this.buildDmBean(apkInfo, downAction.rtp_method));
                            }

                            MoreListActivity.this.startActivity(new Intent(MoreListActivity.this, DMActivity.class));
                        }
                    });
                } else {
                    Toast.makeText(this, "数据解析错误", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception var4) {
                Toast.makeText(this, "数据解析错误", Toast.LENGTH_SHORT).show();
            }
        }

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

    public void loadData() {
        if (configList != null) {
            toLoadData(configList.market);
            Log.d("ldl", "loadData去请求...");
        }
    }

    private void toLoadData(String market) {
        this.showLoading();
        MoreListLogic.getAppList(this, null, SConstant.CID_APP_LIST, market, new StringCallback() {
            public void onSuccess(Response<String> response) {
                if (handleData(response.body())) {
                    showView();
                } else {
                    showError();
                }

            }

            public void onError(Response<String> response) {
                super.onError(response);
                MoreListActivity.this.showError();
            }
        }, this.mModeReq);
    }

    public void showView() {
        Log.d("ldl", "loadData请求成功显示...");
        this.progressBar.setVisibility(View.GONE);
        this.refreshLayout.setVisibility(View.VISIBLE);
        this.errorView.setVisibility(View.GONE);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.mRecyclerView.setAdapter(this.moreAdapter = new MoreAdapter(this.listInfo, this, this.mModeReq));
        this.mRecyclerView.addOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(RecyclerView view, int state) {
                LayoutManager layoutManager = view.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItem = ((LinearLayoutManager) view.getLayoutManager()).findLastVisibleItemPosition();
                if (!MoreListActivity.this.mLoading && visibleItemCount > 0 && state == 0 && lastVisibleItem >= totalItemCount - 1) {
                    MoreListActivity.this.mLoading = true;
                    if (configList != null) {
                        MoreListLogic.getAppList(MoreListActivity.this, listInfo.href_next, SConstant.CID_APP_LIST, configList.market, new StringCallback() {
                            public void onSuccess(Response<String> response) {
                                if (handleData(response.body())) {
                                    moreAdapter.addData(listInfo.list);
                                } else {
                                    Toast.makeText(MoreListActivity.this, string.not_more_data, Toast.LENGTH_SHORT).show();
                                }
                                MoreListActivity.this.mLoading = false;
                            }

                            public void onError(Response<String> response) {
                                super.onError(response);
                                Toast.makeText(MoreListActivity.this, string.get_data_failed, Toast.LENGTH_SHORT).show();
                                MoreListActivity.this.mLoading = false;
                            }
                        }, MoreListActivity.this.mModeReq);
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
                if (half >= bottomView.getHeight() / 3) {
                    MoreListActivity.this.reportExposure(firstVisibleItem, lastVisibleItem);
                } else {
                    MoreListActivity.this.reportExposure(firstVisibleItem, lastVisibleItem - 1);
                }

            }
        });
        this.refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            public void onRefresh() {
                MoreListActivity.this.refresh();
            }
        });
        DownloadLogic.getInstance().setDownloadListener(this);
    }

    private void initSearch() {
        RelativeLayout searchHome = (RelativeLayout) this.findViewById(id.search_home);
        searchHome.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MoreListActivity.this, SearchActivity.class);
                intent.putExtra(SearchActivity.EXTRA_TAG, SearchActivity.hot_app);
                MoreListActivity.this.startActivity(intent);
            }
        });
    }

    private void reportExposure(int first, int last) {
        for (int i = first; i <= last; ++i) {
            StoreApkInfo info = (StoreApkInfo) MoreAdapter.appList.get(i);
            if (!this.exposureId.containsKey(info.appid)) {
                ReportLogic.report(this, this.listInfo.rtp_method, info.rpt_ss, this.listInfo.flag_replace, (ClickInfo) null);
                this.exposureId.put(info.appid, info.rpt_ss);
            }
        }

    }

    private void refresh() {
        if (this.configList != null) {
            MoreListLogic.getAppList(this, this.listInfo.href_next, SConstant.CID_APP_LIST, configList.market, new StringCallback() {
                public void onSuccess(Response<String> response) {
                    if (handleData(response.body())) {
                        moreAdapter.setData(MoreListActivity.this.listInfo);
                    } else {
                        Toast.makeText(MoreListActivity.this, string.refresh_failed, Toast.LENGTH_SHORT).show();
                    }

                    refreshLayout.setRefreshing(false);
                }

                public void onError(Response<String> response) {
                    super.onError(response);
                    showError();
                    refreshLayout.setRefreshing(false);
                }
            }, this.mModeReq);
        }

    }

    private boolean handleData(String body) {
        this.listInfo = (StoreListInfo) (new Gson()).fromJson(body, StoreListInfo.class);
        return null != this.listInfo && null == this.listInfo.err && this.listInfo.list.size() > 0;
    }

    protected void onResume() {
        super.onResume();
        this.startTime = System.currentTimeMillis();
        if (null != this.moreAdapter) {
            this.moreAdapter.notifyDataSetChanged();
        }

    }

    protected void onPause() {
        super.onPause();
        long time = System.currentTimeMillis() - this.startTime;
        if (this.startTime != 0L && null != this.listInfo && null == this.listInfo.err && time > 1000L) {
            ReportLogic.report(this, this.listInfo.rtp_method, this.listInfo.rpt_st, true, System.currentTimeMillis() - this.startTime, (StringCallback) null);
        }

    }

    public void showLoading() {
        this.refreshLayout.setVisibility(View.GONE);
        this.errorView.setVisibility(View.GONE);
        this.progressBar.setVisibility(View.VISIBLE);
    }

    public void showError() {
        Log.d("ldl", "loadData去请求返回失败...");
        this.progressBar.setVisibility(View.GONE);
        this.refreshLayout.setVisibility(View.GONE);
        this.errorView.setVisibility(View.VISIBLE);
        this.reload.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                MoreListActivity.this.loadData();
            }
        });
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
}
