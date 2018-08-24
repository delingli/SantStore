package com.hai.store.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;

import com.hai.store.R;
import com.hai.store.adapter.DMListAdapter;
import com.hai.store.base.BaseActivity;
import com.hai.store.data.DownloadCart;
import com.hai.store.data.DownloadLogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DMActivity extends BaseActivity implements DownloadLogic.DownloadListener {

    private RecyclerView mRecyclerView;
    private Toolbar toolbar;
    private DMListAdapter dmListAdapter;
    private RelativeLayout noOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dm);
        findView();
        setLogic();
        showView();
    }

    @Override
    public void findView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.dm_list);
        noOrder = (RelativeLayout) findViewById(R.id.rl_no_order);
    }

    @Override
    public void setLogic() {
        toolbar = (Toolbar) findViewById(R.id.dm_toolbar);
        toolbar.setNavigationIcon(R.drawable.app_back_selector);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        if (DownloadCart.getInstance().getApkStatus().size() <= 0) {
            noOrder.setVisibility(View.VISIBLE);
        }
        showError();
    }

    @Override
    public void loadData() {

    }

    @Override
    public void showView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(dmListAdapter = new DMListAdapter(this));
        mRecyclerView.setItemAnimator(null);
        dmListAdapter.setOnDeleteListener(new DMListAdapter.DeleteListener() {
            @Override
            public void onDeleteListener() {
                showError();
            }
        });
        DownloadLogic.getInstance().setDownloadListener(this);
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void showError() {
        if (null != noOrder && DownloadCart.getInstance().getApkStatus().size() <= 0) {
            noOrder.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onProgressListener(String appId) {
        if (null != dmListAdapter)
            dmListAdapter.upDateStatus(appId);
    }

    @Override
    public void onError(String appId) {
        if (null != dmListAdapter)
            dmListAdapter.upDateStatus(appId);
    }

    @Override
    public void onStart(String appId) {
        if (null != dmListAdapter) {
            List<String> statusList = new ArrayList<>();
            Map<String, DownloadCart.DownloadStatus> statusMap = DownloadCart.getInstance().getApkCarDownloadStatus();
            for (String apkId : statusMap.keySet()) {
                statusList.add(apkId);
            }
            dmListAdapter.upDateStatus(statusList);
        }
    }

    @Override
    public void onSuccess(String appId) {
        if (null != dmListAdapter)
            dmListAdapter.upDateStatus(appId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadLogic.getInstance().revokedDownloadListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != dmListAdapter)
            dmListAdapter.upDateStatus();
    }
}
