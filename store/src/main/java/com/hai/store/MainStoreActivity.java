package com.hai.store;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.hai.store.activity.DMActivity;
import com.hai.store.adapter.MainAdapter;
import com.hai.store.base.BaseActivity;
import com.hai.store.data.DownloadLogic;
import com.hai.store.view.InsertRecommend;
import com.hai.store.view.RecommendADListView;
import com.hai.store.view.SplashRecommend;

import java.util.ArrayList;
import java.util.List;

public class MainStoreActivity extends BaseActivity implements DownloadLogic.DownloadListener {

    private RecyclerView mRecyclerView;
    private List<List<String>> main;
    private List<String> card;
    private Toolbar toolbar;
    private ImageView gotoDM;
    private ProgressBar progressBar;
    private RelativeLayout errorView;
    private Button reload;
    private RecommendADListView bannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_store);
        findView();
        setLogic();
        bannerView.setVisibility(View.GONE);
        showDialog();
    }

    private void showDialog() {
//        InsertRecommend ir = new InsertRecommend();
//        ir.show(getSupportFragmentManager(), "insert");
        SplashRecommend sr = new SplashRecommend();
        sr.show(getSupportFragmentManager(), "splash");
    }

    @Override
    public void setLogic() {
        toolbar.setNavigationIcon(R.drawable.app_back_selector);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        gotoDM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainStoreActivity.this, DMActivity.class));
            }
        });
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData();
            }
        });
        card = new ArrayList<>();
        card.add("aaa");
        card.add("bbb");
        card.add("ccc");
        card.add("ddd");
        card.add("eee");
        card.add("fff");
        card.add("ggg");
        main = new ArrayList<>();
        main.add(card);
        main.add(card);
        main.add(card);
        main.add(card);
        main.add(card);
        main.add(card);
        main.add(card);
        main.add(card);
        main.add(card);
        main.add(card);
        main.add(card);
        main.add(card);
        main.add(card);
        loadData();
    }

    @Override
    public void loadData() {
//        showLoading();
        List<String> list = new ArrayList<>();
        list.add("/微信.apk");
        bannerView.loadDate(this, list);
    }

    @Override
    public void showView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new MainAdapter(this, main));
        mRecyclerView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showLoading() {
        errorView.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void showError() {
        errorView.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void findView() {
        reload = (Button) findViewById(R.id.btn_reload);
        mRecyclerView = (RecyclerView) findViewById(R.id.main_list);
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        gotoDM = (ImageView) findViewById(R.id.goto_dm);
        errorView = (RelativeLayout) findViewById(R.id.error_view);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        bannerView = (RecommendADListView) findViewById(R.id.banner_app);
    }

    @Override
    public void onProgressListener(String appId) {

    }

    @Override
    public void onError(String appId) {

    }

    @Override
    public void onStart(String appId) {
        Log.d("Main_Store", "onStart: ");
    }

    @Override
    public void onSuccess(String appId) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        bannerView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadLogic.getInstance().revokedDownloadListener(this);
        bannerView.onClose();
    }
}
