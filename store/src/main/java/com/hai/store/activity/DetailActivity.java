package com.hai.store.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hai.store.R;
import com.hai.store.adapter.SrcUrlListAdapter;
import com.hai.store.base.BaseActivity;
import com.hai.store.base.SConstant;
import com.hai.store.bean.ClickInfo;
import com.hai.store.bean.DmBean;
import com.hai.store.bean.StoreADInfo;
import com.hai.store.bean.StoreDetailInfo;
import com.hai.store.data.DetailLogic;
import com.hai.store.data.DownloadLogic;
import com.hai.store.data.ReportLogic;
import com.hai.store.sqlite.PublicDao;
import com.hai.store.utils.ApkUtils;
import com.hai.store.utils.Utils;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.sant.api.Api;
import com.squareup.picasso.Picasso;

import static com.hai.store.base.SConstant.APP_NAME;
import static com.hai.store.base.SConstant.DETAIL_ELSE;
import static com.hai.store.base.SConstant.DETAIL_NOTIFY;
import static com.hai.store.base.SConstant.PKG_NAME;
import static com.hai.store.base.SConstant.TMODE;
import static com.hai.store.base.SConstant.TMODE_NOTIFY;

public class DetailActivity extends BaseActivity implements DownloadLogic.DownloadListener {

    public static final String DETAIL = "app_detail";
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Toolbar toolbar;
    private ImageView icon;
    private RecyclerView srcList;
    private LinearLayout contentView, moreDetail, romLayout, authorLayout, updateTimeLayout;
    private ProgressBar progressBar;
    private RelativeLayout errorView;
    private Button reload;
    private TextView size, starCount, versionName, description, newFeature, authorName, rom, updateTime, pkgName;
    private TextView count, intoApp, newFeatureTitle;
    private RatingBar rating;
    private StoreDetailInfo detailInfo;
    private String detailUrl;
    private String appName;
    private boolean canUpdateButton;
    private int x;
    private int y;
    private long startTime;
    public static String BKI = "55CF6E0716248405";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        findView();
        setLogic();
        loadData();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        startTime = System.currentTimeMillis();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        long time = System.currentTimeMillis() - startTime;
//        if (null != detailInfo && time > 1000) {
//            ReportLogic.report(this, detailInfo.rtp_method, detailInfo.rpt_st, true, System.currentTimeMillis() - startTime);
//        }
    }

    @Override
    public void findView() {
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.detail_collapsing);
        toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        icon = (ImageView) findViewById(R.id.app_icon);
        srcList = (RecyclerView) findViewById(R.id.detail_src_list);
        contentView = (LinearLayout) findViewById(R.id.content_view);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        reload = (Button) findViewById(R.id.btn_reload);
        errorView = (RelativeLayout) findViewById(R.id.error_view);
        size = (TextView) findViewById(R.id.size);
        count = (TextView) findViewById(R.id.play_count);
        rating = (RatingBar) findViewById(R.id.rb_star);
        starCount = (TextView) findViewById(R.id.star_count);
        versionName = (TextView) findViewById(R.id.version_name);
        description = (TextView) findViewById(R.id.description);
        newFeature = (TextView) findViewById(R.id.newFeature);
        authorName = (TextView) findViewById(R.id.author_name);
        rom = (TextView) findViewById(R.id.rom);
        romLayout = (LinearLayout) findViewById(R.id.rom_title);
        authorLayout = (LinearLayout) findViewById(R.id.author_title);
        updateTimeLayout = (LinearLayout) findViewById(R.id.update_time_title);
        updateTime = (TextView) findViewById(R.id.update_time);
        pkgName = (TextView) findViewById(R.id.pkg_name);
        intoApp = (TextView) findViewById(R.id.into_app);
        newFeatureTitle = (TextView) findViewById(R.id.newFeature_title);
        moreDetail = (LinearLayout) findViewById(R.id.more_detail);
    }

    @Override
    public void setLogic() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.app_back_selector);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData();
            }
        });
        Intent intent = getIntent();
        StoreADInfo info = (StoreADInfo) intent.getSerializableExtra(DETAIL_NOTIFY);
        if (info != null) {
            detailUrl = info.href + TMODE + TMODE_NOTIFY + SConstant.CID + "-28";
            appName = info.name;
            setTitle();
            ReportLogic.report(this, "POST", info.c_rpt, 0, null);
//            ReportLogic.report(this, "POST", info.d_rpt, 0, null);
//            startDown(info);
        } else {
            Bundle bundle = intent.getBundleExtra(DETAIL);
            if (null != bundle) {
                detailUrl = bundle.getString(PKG_NAME) + TMODE + bundle.getString(DETAIL_ELSE);
                appName = bundle.getString(APP_NAME);
                setTitle();
            }
        }
        if (intent != null && intent.getStringArrayExtra("rpClick") != null) {
//            com.stkj.lib:api-1.3.6
            final String[] rpClicks = intent.getStringArrayExtra("rpClick");
            Api.common(getApplicationContext()).report(rpClicks, null, null);
            Log.d("HandleBusinessService", "应用圈类型点击上报成功");
        }
    }

    @Override
    public void onBackPressed() {
        if (null != getIntent()) {
            Intent backIntent = getIntent().getParcelableExtra(BKI);
            if (null != backIntent) startActivity(backIntent);
        }
        super.onBackPressed();
    }

    private void setTitle() {
        collapsingToolbarLayout.setTitle(appName);
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);
    }

    @Override
    public void loadData() {
        showLoading();
        DetailLogic.getAppDetail(this, detailUrl, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                if (handleData(response.body())) {
                    showView();
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                showError();
            }
        });
    }

    private boolean handleData(String body) {
        detailInfo = new Gson().fromJson(body, StoreDetailInfo.class);
        if (null != detailInfo && null == detailInfo.err) {
            return true;
        } else {
            Toast.makeText(this, R.string.not_detail, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void showView() {
        ReportLogic.report(this, detailInfo.rtp_method, detailInfo.rpt_ss, 0, null);
        errorView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);

        if (null != detailInfo.updateinfo) {
            newFeatureTitle.setText(R.string.new_features);
            newFeature.setText(detailInfo.updateinfo);
        } else {
            newFeatureTitle.setVisibility(View.GONE);
            newFeature.setVisibility(View.GONE);
        }

        moreDetail.setVisibility(View.VISIBLE);
        Picasso.with(this).load(detailInfo.icon).placeholder(R.drawable.ic_loading).error(R.drawable.ic_loading).into(icon);

        try {
            size.setText(Utils.readableFileSize(detailInfo.size));
        } catch (NumberFormatException e) {
            size.setText(detailInfo.size);
        }

        if (null != detailInfo.downcount) {
            try {
                double c = Double.valueOf(detailInfo.downcount);
                count.setText(Utils.downloadNum(c));
            } catch (NumberFormatException e) {
                count.setText(detailInfo.downcount);
            }
        }

        if (null != detailInfo.rating) {
            try {
                rating.setRating(Long.valueOf(detailInfo.rating));
            } catch (Exception e) {
                rating.setRating(4); //默认4分
            }
            rating.setVisibility(View.VISIBLE);
        }

        if (null != detailInfo.ratingperson) {
            starCount.setText(getString(R.string.rating_count, detailInfo.ratingperson));
        }

        versionName.setText(Utils.versionName(detailInfo.versionname));

        description.setText(detailInfo.description);


        pkgName.setText(detailInfo.apk);

        if (null != detailInfo.os) {
            rom.setText(Utils.getAndroidRom(Integer.valueOf(detailInfo.os)));
        } else {
            romLayout.setVisibility(View.GONE);
        }

        if (null != detailInfo.updatetime) {
            try {
                updateTime.setText(Utils.dateString((Long.valueOf(detailInfo.updatetime)) * 1000));
            } catch (NumberFormatException e) {
                updateTime.setText(detailInfo.updatetime);
            }
        } else {
            updateTimeLayout.setVisibility(View.GONE);
        }

        if (null != detailInfo.developer) {
            authorName.setText(detailInfo.developer);
        } else {
            authorLayout.setVisibility(View.GONE);
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        srcList.setLayoutManager(linearLayoutManager);
        srcList.setNestedScrollingEnabled(false); //滑动冲突
        srcList.setAdapter(new SrcUrlListAdapter(detailInfo.screenshots, this));

        buttonStatus();
        DownloadLogic.getInstance().setDownloadListener(this);
    }

    private void buttonStatus() {
        intoApp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                x = (int) motionEvent.getX();
                y = (int) motionEvent.getY();
                return false;
            }
        });
        int status = ApkUtils.getStatus(this, detailInfo.appid, detailInfo.apk, Integer.valueOf(detailInfo.versioncode));
        switch (status) {
            case ApkUtils.DOWNLOAD:
                intoApp.setClickable(true);
                intoApp.setText(R.string.download);
                intoApp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        insert(true);
                    }
                });
                break;
            case ApkUtils.DOWNLOADING:
                intoApp.setText(R.string.downloading);
                intoApp.setClickable(false);
                break;
            case ApkUtils.INSTALL:
                intoApp.setText(R.string.install);
                intoApp.setClickable(true);
                intoApp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ApkUtils.install(DetailActivity.this, DownloadLogic.buildUrl(DetailActivity.this, appName));
                    }
                });
                break;
            case ApkUtils.OPEN:
                intoApp.setText(R.string.open);
                intoApp.setClickable(true);
                intoApp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ApkUtils.startApp(DetailActivity.this, detailInfo.apk);
                    }
                });
                break;
            case ApkUtils.UPDATE:
                intoApp.setText(R.string.update);
                intoApp.setClickable(true);
                intoApp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        insert(false);
                    }
                });
                break;
            default:
                break;
        }
        canUpdateButton = true;
    }

    private void insert(boolean report) {
        intoApp.setText(R.string.downloading);
        if (report) {
            ReportLogic.report(this, detailInfo.rtp_method, detailInfo.rpt_cd, detailInfo.flag_replace, new ClickInfo(x, y));
        }
        DownloadLogic.getInstance().startDownload(this, detailInfo.href_download, appName, detailInfo.appid, detailInfo.icon,
                detailInfo.apk, detailInfo.versioncode, detailInfo.rpt_dc, detailInfo.rpt_dl, detailInfo.rtp_method);
        intoApp.setClickable(false);
        PublicDao.insert(buildDmBean(detailInfo));
    }

    private DmBean buildDmBean(StoreDetailInfo info) {
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
        dmBean.method = info.rtp_method;
        return dmBean;
    }

    private void startDown(StoreADInfo info) {
        intoApp.setText(R.string.downloading);
        DownloadLogic.getInstance().startDownload(this, info.down_url, info.name, info.appid, info.icon_img,
                info.apk, info.versioncode, info.dc_rpt, null, "POST");
        intoApp.setClickable(false);
        PublicDao.insert(buildDmBean(detailInfo));
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        contentView.setVisibility(View.GONE);
    }

    @Override
    public void showError() {
        contentView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onProgressListener(String appId) {

    }

    @Override
    public void onError(String appId) {
        if (canUpdateButton)
            buttonStatus();
    }

    @Override
    public void onStart(String appId) {
    }

    @Override
    public void onSuccess(String appId) {
        if (canUpdateButton)
            buttonStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadLogic.getInstance().revokedDownloadListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTime = System.currentTimeMillis();
        if (canUpdateButton)
            buttonStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        long time = System.currentTimeMillis() - startTime;
        if (startTime != 0 && null != detailInfo && null == detailInfo.err && time > 1000) {
            ReportLogic.report(this, detailInfo.rtp_method, detailInfo.rpt_st, true, System.currentTimeMillis() - startTime, null);
        }
    }
}
