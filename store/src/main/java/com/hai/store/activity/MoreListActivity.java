package com.hai.store.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hai.store.Application;
import com.hai.store.R;
import com.hai.store.adapter.MoreAdapter;
import com.hai.store.base.BaseActivity;
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

public class MoreListActivity extends BaseActivity implements DownloadLogic.DownloadListener {

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
    private String mModeReq = SConstant.TMODE_ICON;
    public Map<String, List<String>> exposureId = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_list);
        findView();
        setLogic();
        loadData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleDownList(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void findView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.more_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.more_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_home);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MoreListActivity.this, "^_^", Toast.LENGTH_SHORT).show();
            }
        });
        gotoDM = (ImageView) findViewById(R.id.goto_dm);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.more_swipe);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        errorView = (RelativeLayout) findViewById(R.id.error_view);
        reload = (Button) findViewById(R.id.btn_reload);
        mTitle = (TextView) findViewById(R.id.more_title);
    }

    @Override
    public void setLogic() {

        gotoDM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MoreListActivity.this, DMActivity.class));
            }
        });
        Intent intent = getIntent();

        String tMode = intent.getStringExtra(SConstant.LIST_MODE);
        if (null != tMode) {
            mModeReq = tMode;
        }

        Bundle more = intent.getBundleExtra(MORE);
        if (null != more) {
            String title = more.getString(TITLE);
            if (null != title) {
                mTitle.setText(title);
            }
        }
        handleDownList(intent);
    }

    private void handleDownList(Intent intent) {
        String downListJson = intent.getStringExtra(DOWN_LIST);
        if (null != downListJson) {
            Log.e("DOWN_LIST", downListJson);
            try {
                final StoreListInfo downAction = new Gson().fromJson(downListJson, StoreListInfo.class);
                if (null != downAction && downAction.list != null) {
                    EXECUTOR.execute(new Runnable() {
                        @Override
                        public void run() {
                            for (StoreApkInfo apkInfo : downAction.list) {
                                DownloadLogic.getInstance().startDownload(Application.getContext(), apkInfo.href_download, apkInfo.appname,
                                        apkInfo.appid, apkInfo.icon, apkInfo.apk, apkInfo.versioncode, apkInfo.rpt_dc, apkInfo.rpt_dl, downAction.rtp_method);
                                PublicDao.insert(buildDmBean(apkInfo, downAction.rtp_method));
                            }
                            startActivity(new Intent(MoreListActivity.this, DMActivity.class));
                        }
                    });
                } else {
                    Toast.makeText(this, "数据解析错误", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
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

    @Override
    public void loadData() {
        showLoading();
        MoreListLogic.getAppList(MoreListActivity.this, null, new StringCallback() {
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

    @Override
    public void showView() {
        progressBar.setVisibility(View.GONE);
        refreshLayout.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(moreAdapter = new MoreAdapter(listInfo, this, mModeReq));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView view, int state) {
                RecyclerView.LayoutManager layoutManager = view.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItem = ((LinearLayoutManager) view.getLayoutManager()).findLastVisibleItemPosition();
                if (!mLoading && visibleItemCount > 0 && state == SCROLL_STATE_IDLE && lastVisibleItem >= totalItemCount - 1) {
                    mLoading = true;
                    MoreListLogic.getAppList(MoreListActivity.this, listInfo.href_next, new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            if (handleData(response.body())) {
                                moreAdapter.addData(listInfo.list);
                            } else {
                                Toast.makeText(MoreListActivity.this, R.string.not_more_data, Toast.LENGTH_SHORT).show();
                            }
                            mLoading = false;
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            Toast.makeText(MoreListActivity.this, R.string.get_data_failed, Toast.LENGTH_SHORT).show();
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
        RelativeLayout searchHome = (RelativeLayout) findViewById(R.id.search_home);
        searchHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));
            }
        });
    }

    private void reportExposure(int first, int last) {
        for (int i = first; i <= last; i++) {
            StoreApkInfo info = MoreAdapter.appList.get(i); //此处不需要-1
            if (!exposureId.containsKey(info.appid)) {
                ReportLogic.report(this, listInfo.rtp_method, info.rpt_ss, listInfo.flag_replace, null);
                exposureId.put(info.appid, info.rpt_ss);
            }
        }
    }

    private void refresh() {
        MoreListLogic.getAppList(MoreListActivity.this, listInfo.href_next, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                if (handleData(response.body())) {
                    moreAdapter.setData(listInfo);
                } else {
                    Toast.makeText(MoreListActivity.this, R.string.refresh_failed, Toast.LENGTH_SHORT).show();
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

    private boolean handleData(String body) {
        listInfo = new Gson().fromJson(body, StoreListInfo.class);
        return null != listInfo && null == listInfo.err && listInfo.list.size() > 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTime = System.currentTimeMillis();
        if (null != moreAdapter) {
            moreAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        long time = System.currentTimeMillis() - startTime;
        if (startTime != 0 && null != listInfo && null == listInfo.err && time > 1000) {
            ReportLogic.report(this, listInfo.rtp_method, listInfo.rpt_st, true, System.currentTimeMillis() - startTime, null);
        }
    }

    @Override
    public void showLoading() {
        refreshLayout.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
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
