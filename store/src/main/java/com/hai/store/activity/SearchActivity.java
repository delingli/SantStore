//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hai.store.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemAnimator;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.SearchView.SearchAutoComplete;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hai.store.Application;
import com.hai.store.R.color;
import com.hai.store.R.drawable;
import com.hai.store.R.id;
import com.hai.store.R.layout;
import com.hai.store.R.string;
import com.hai.store.base.BaseListAdapter;
import com.hai.store.base.BaseViewHolder;
import com.hai.store.base.OnTipsItemClickListener;
import com.hai.store.bean.ClickInfo;
import com.hai.store.bean.DmBean;
import com.hai.store.bean.RptBean;
import com.hai.store.bean.StoreApkInfo;
import com.hai.store.bean.StoreListInfo;
import com.hai.store.data.DownloadCart;
import com.hai.store.data.DownloadCart.DownloadStatus;
import com.hai.store.data.DownloadLogic;
import com.hai.store.data.DownloadLogic.DownloadListener;
import com.hai.store.data.ReportLogic;
import com.hai.store.data.SearchLogic;
import com.hai.store.mildperate.MildOperatorConfig;
import com.hai.store.mildperate.MildperateConstant;
import com.hai.store.sqlite.PublicDao;
import com.hai.store.utils.ApkUtils;
import com.hai.store.utils.Utils;
import com.hai.store.view.FlowLayout;
import com.hai.store.view.FlowLayout.LayoutParams;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.sant.api.APIError;
import com.sant.api.Api;
import com.sant.api.Callback;
import com.sant.api.common.ADDAPPStore;
import com.sant.api.common.ADData;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class SearchActivity extends AppCompatActivity implements OnQueryTextListener, OnTipsItemClickListener, DownloadListener {
    private static final String TAG = "SearchActivity";
    private FlowLayout flowLayout;
    private TextView hot;
    private LinearLayout searchTop;
    private LinearLayout searchContent;
    private RelativeLayout searchError;
    private RecyclerView recyclerView;
    private SearchActivity.ContentAdapter adapter;
    private ProgressDialog mProgressDialog;
    private boolean finish;
    private Handler handler = new Handler(Looper.getMainLooper());
    private List<SearchActivity.SearchModel> pageList;
    private StoreListInfo hotListInfo;
    private List<StoreApkInfo> hotList;
    private Gson gson = new Gson();
    private StoreListInfo searchListInfo;
    private StoreListInfo recommendListInfo;
    private HashMap<String, List<String>> recommendId = new HashMap();
    private HashMap<String, List<String>> resultId = new HashMap();
    private static final int resultCount = 3;
    private String[] colors = new String[]{"#FF6A6A", "#EE2C2C", "#D15FEE", "#7EC0EE", "#1E90FF"};
    private Random random = new Random();
    private SearchAutoComplete autoText;
    public static String download_app = "downloadapp_MoreListFragment";
    public static String hot_app = "hot_MoreListActivity";
    private String currentFromTag;
    public static String EXTRA_TAG = "tag";
    private MildOperatorConfig mSearshRecommerconfig;
    private MildOperatorConfig mSearshResultConfig;

    public SearchActivity() {
        currentFromTag = download_app;
        mSearshRecommerconfig = new MildOperatorConfig();
        mSearshResultConfig = new MildOperatorConfig();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VERSION.SDK_INT >= 21) {
            this.getWindow().setStatusBarColor(this.getResources().getColor(color.colorPrimaryDark));
        }

        this.setContentView(layout.activity_search);
        this.handlerIntent();
        this.findView();
        this.hideHot();
        this.initSearchActivity();
        this.initSearchResultConfig();
        this.showKeyboard();
        DownloadLogic.getInstance().setDownloadListener(this);
    }

    private void initSearchResultConfig() {
        final String action = TextUtils.equals(this.currentFromTag, download_app) ? MildperateConstant.APP_SEARCH : MildperateConstant.APP_SEARCH3;
        Log.d("ldl", "action是:???" + action);
        Api.common(this).fetchADAPPStore(action, (String) null, new Callback<ADData>() {
            public void onFinish(boolean isSucess, ADData adData, APIError apiError, Object o) {
                String market = "";
                if (isSucess) {
                    Log.d("ldl", "竹蜻蜓配置了广告");
                    if (adData != null && adData instanceof ADDAPPStore) {
                        ADDAPPStore addappstore = (ADDAPPStore) adData;
                        market = addappstore.market;
                    }
                } else {
                    Log.d("ldl", "竹蜻蜓没有配置了广告");
                }


                mSearshResultConfig.market = market;
            }
        });
    }

    private void handlerIntent() {
        if (this.getIntent() != null) {
            Intent intent = this.getIntent();
            String tag = intent.getStringExtra(EXTRA_TAG);
            this.currentFromTag = tag;
        }

    }

    private void findView() {
        ImageView dm = (ImageView) this.findViewById(id.goto_dm);
        dm.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SearchActivity.this.startActivity(new Intent(SearchActivity.this, DMActivity.class));
            }
        });
        searchContent = (LinearLayout) this.findViewById(id.search_content);
        searchError = (RelativeLayout) this.findViewById(id.search_error);
        recyclerView = (RecyclerView) this.findViewById(id.search_recycler);
        recyclerView.setItemAnimator((ItemAnimator) null);
        searchTop = (LinearLayout) this.findViewById(id.search_top);
        hot = (TextView) this.findViewById(id.search_hot);
        flowLayout = (FlowLayout) this.findViewById(id.flow_layout);
        autoText = (SearchAutoComplete) this.findViewById(id.search_src_text);
        ImageView back = (ImageView) this.findViewById(id.search_back);
        back.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SearchActivity.this.finish();
            }
        });
        SearchView searchView = (SearchView) this.findViewById(id.search_view);
        searchView.onActionViewExpanded();
        searchView.setQueryHint("你想找什么?");
        searchView.setOnQueryTextListener(this);
    }

    private void setFlowParams() {
        LayoutParams params = new LayoutParams(-2, -2);
        params.setMargins(12, 12, 12, 12);
        Iterator var2 = this.hotList.iterator();

        while (var2.hasNext()) {
            final StoreApkInfo info = (StoreApkInfo) var2.next();
            TextView tv = new TextView(this);
            tv.setPadding(12, 6, 12, 6);
            tv.setText(info.appname);
            tv.setTextSize(14.0F);
            tv.setBackgroundResource(drawable.bg_app_down);
            tv.setTextColor(Color.parseColor(this.colors[this.random.nextInt(this.colors.length)]));
            tv.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    setText(info.appname);
                }
            });
            flowLayout.addView(tv, params);
        }

    }

    private void setText(String text) {
        this.autoText.setText(text);
        this.autoText.setSelection(text.length());
        this.showKeyboard();
    }

    private void showKeyboard() {
        this.handler.postDelayed(new Runnable() {
            public void run() {
                InputMethodManager im = (InputMethodManager) SearchActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (im != null) {
                    im.showSoftInput(SearchActivity.this.autoText, 0);
                }

            }
        }, 500L);
    }

    private void dismissKeyboard() {
        this.handler.post(new Runnable() {
            public void run() {
                InputMethodManager im = (InputMethodManager) SearchActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (im != null) {
                    im.toggleSoftInput(0, 2);
                }

            }
        });
    }

    public boolean onQueryTextSubmit(String query) {
        String trim = query.trim();
        if (TextUtils.isEmpty(trim)) {
            Toast.makeText(this, "请输入应用名称", Toast.LENGTH_SHORT).show();
            return true;
        } else if (trim.length() > 20) {
            Toast.makeText(this, "不能超过20个字符", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            this.textSubmit(trim);
            return true;
        }
    }

    public boolean onQueryTextChange(String newText) {
        if (null == this.pageList) {
            this.pageList = new ArrayList();
        }

        String trim = newText.trim();
        if (trim.length() > 20) {
            this.initSearchActivity();
        } else if (trim.length() >= 2) {
            this.textChange(trim);
        } else {
            this.initSearchActivity();
        }

        return true;
    }

    private void initSearchActivity() {
        SearchLogic.stopSearch();
        this.recyclerView.setVisibility(View.GONE);
        this.showContent();
        final String action = TextUtils.equals(this.currentFromTag, download_app) ? MildperateConstant.APP_SEARCH2 : MildperateConstant.APP_SEARCH4;
        Api.common(this).fetchADAPPStore(action, null, new Callback<ADData>() {
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
                mSearshRecommerconfig.market = market;
                if (mSearshRecommerconfig != null && null == hotList) {
                    toRequestHotSearch(mSearshRecommerconfig);
                    Log.d("ldl", "#SeqarchActivity#后台配置了请求热门搜索......");
                } else {
                    showHot();
                }

            }
        });
    }

    private void toRequestHotSearch(MildOperatorConfig config) {
        SearchLogic.getHotSearch(config.market, new StringCallback() {
            public void onSuccess(Response<String> response) {
                if (!SearchActivity.this.finish) {
                    if (handleHot(response.body())) {
                        hotList = SearchActivity.this.hotListInfo.list;
                        flowLayout.removeAllViews();
                        flowLayout.setGravity(48);
                        setFlowParams();
                        showHot();
                    } else {
                        Toast.makeText(Application.getContext(), "热门搜索获取失败", Toast.LENGTH_SHORT).show();
                        hideHot();
                        searchContent.setVisibility(View.GONE);
                    }

                }
            }

            public void onError(Response<String> response) {
                super.onError(response);
                if (!SearchActivity.this.finish) {
                    Toast.makeText(Application.getContext(), "热门搜索获取失败", Toast.LENGTH_SHORT).show();
                    hideHot();
                    searchContent.setVisibility(View.GONE);
                }
            }
        });
    }

    private boolean handleHot(String body) {
        this.hotListInfo = (StoreListInfo) this.gson.fromJson(body, StoreListInfo.class);
        return null != this.hotListInfo && null == this.hotListInfo.err && this.hotListInfo.list.size() > 0;
    }

    private void showError() {
        this.searchError.setVisibility(View.VISIBLE);
        this.searchContent.setVisibility(View.GONE);
    }

    private void showContent() {
        this.searchError.setVisibility(View.GONE);
        this.searchContent.setVisibility(View.VISIBLE);
    }

    private void showHot() {
        this.flowLayout.setVisibility(View.VISIBLE);
        this.hot.setText(string.hot_search);
        this.searchTop.setVisibility(View.VISIBLE);
        this.recyclerView.setVisibility(View.GONE);
        Log.d("ldl", "#SearchActivity#显示热门搜索...");
    }

    private void showResult() {
        this.flowLayout.setVisibility(View.GONE);
        this.hot.setText(string.search_result);
        this.searchTop.setVisibility(View.VISIBLE);
        this.showContent();
    }

    private void hideHot() {
        this.flowLayout.setVisibility(View.GONE);
        this.searchTop.setVisibility(View.GONE);
    }

    private void textSubmit(String text) {
        if (mSearshResultConfig != null) {
            submitHandler(text, this.mSearshResultConfig.market);
            Log.d("ldl", "#SearchActivity#配置搜索允许显示...");
        }

    }

    private void textChange(String text) {
        if (mSearshResultConfig != null) {
            this.hideHot();
            this.changeHandler(this.mSearshResultConfig.market, text);
        } else {
        }

    }

    private void submitHandler(String text, String market) {
        SearchLogic.stopSearch();
        this.showProgressDialog();
        SearchLogic.getSearchContent(market, text, new StringCallback() {
            public void onSuccess(Response<String> response) {
                SearchActivity.this.dismissProgressDialog();
                if (!SearchActivity.this.finish) {
                    SearchActivity.this.showResult();
                    SearchActivity.this.dismissKeyboard();
                    SearchActivity.this.pageList.clear();
                    SearchActivity.this.recyclerView.setVisibility(View.VISIBLE);
                    SearchActivity.this.recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
                    if (null == SearchActivity.this.adapter) {
                        SearchActivity.this.recyclerView.setAdapter(SearchActivity.this.adapter = new SearchActivity.ContentAdapter(SearchActivity.this));
                        SearchActivity.this.adapter.setOnTipsClickListener(SearchActivity.this);
                    }

                    if (SearchActivity.this.handleSearch((String) response.body())) {
                        for (int i = 0; i < SearchActivity.this.searchListInfo.list.size(); ++i) {
                            SearchActivity.this.pageList.add(new SearchModel(0));
                            if (SearchActivity.this.pageList.size() == 3) {
                                break;
                            }
                        }
                    } else {
                        pageList.add(new SearchModel(-1));
                    }

                    adapter.setData(SearchActivity.this.pageList);
                    loadRecommend();
                }
            }

            public void onError(Response<String> response) {
                super.onError(response);
                SearchActivity.this.dismissProgressDialog();
                if (!SearchActivity.this.finish) {
                    SearchActivity.this.showError();
                    SearchActivity.this.dismissKeyboard();
                }
            }
        });
    }

    private void loadRecommend() {
        SearchLogic.getRecommend(new StringCallback() {
            public void onSuccess(Response<String> response) {
                if (!SearchActivity.this.finish) {
                    if (SearchActivity.this.handleRecommend((String) response.body())) {
                        List<SearchActivity.SearchModel> list = new ArrayList();
                        list.add(new SearchModel(1));

                        for (int i = 0; i < SearchActivity.this.recommendListInfo.list.size(); ++i) {
                            list.add(new SearchModel(3));
                        }

                        SearchActivity.this.adapter.addData(list);
                    }

                }
            }

            public void onError(Response<String> response) {
                super.onError(response);
                Log.e("SearchActivity", "loadRecommend error " + response);
            }
        });
    }

    private void showProgressDialog() {
        String title = "提示";
        String content = "正在搜索...";
        this.mProgressDialog = ProgressDialog.show(this, title, content, true, true, new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                SearchLogic.stopSearch();
            }
        });
    }

    private void dismissProgressDialog() {
        if (null != this.mProgressDialog) {
            this.mProgressDialog.dismiss();
        }

    }

    private void changeHandler(String market, String text) {
        this.pageList.clear();
        SearchLogic.stopSearch();
        SearchLogic.getSearchContent(market, text, new StringCallback() {
            public void onSuccess(Response<String> response) {
                if (!SearchActivity.this.finish) {
                    SearchActivity.this.showContent();
                    if (SearchActivity.this.handleSearch((String) response.body())) {
                        SearchActivity.this.recyclerView.setVisibility(View.VISIBLE);

                        for (int i = 0; i < SearchActivity.this.searchListInfo.list.size(); ++i) {
                            if (i == 0) {
                                SearchActivity.this.pageList.add(new SearchActivity.SearchModel(0));
                            } else {
                                SearchActivity.this.pageList.add(new SearchActivity.SearchModel(2));
                            }
                        }

                        SearchActivity.this.recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
                        if (null == SearchActivity.this.adapter) {
                            SearchActivity.this.recyclerView.setAdapter(SearchActivity.this.adapter = new SearchActivity.ContentAdapter(SearchActivity.this));
                            SearchActivity.this.adapter.setOnTipsClickListener(SearchActivity.this);
                        }

                        SearchActivity.this.adapter.setData(SearchActivity.this.pageList);
                    } else {
                        SearchActivity.this.recyclerView.setVisibility(View.GONE);
                    }

                }
            }

            public void onError(Response<String> response) {
                super.onError(response);
                if (!finish) {
                    showContent();
                    recyclerView.setVisibility(View.GONE);
                }
            }
        });
    }

    private boolean handleSearch(String body) {
        this.searchListInfo = (StoreListInfo) this.gson.fromJson(body, StoreListInfo.class);
        return null != this.searchListInfo && null == this.searchListInfo.err && this.searchListInfo.list.size() > 0;
    }

    private boolean handleRecommend(String body) {
        this.recommendListInfo = (StoreListInfo) this.gson.fromJson(body, StoreListInfo.class);
        return null != this.recommendListInfo && null == this.recommendListInfo.err && this.recommendListInfo.list.size() > 0;
    }

    public void onProgressListener(String appId) {
    }

    public void onError(String appId) {
        if (null != this.adapter) {
            this.adapter.notifyDataSetChanged();
        }

    }

    public void onStart(String appId) {
    }

    public void onSuccess(String appId) {
        if (null != this.adapter) {
            this.adapter.notifyDataSetChanged();
        }

    }

    public void onItemClick(String tips) {
        this.setText(tips);
    }

    public void showRecommendItem(StoreApkInfo info) {
        if (!this.recommendId.containsKey(info.appid)) {
            ReportLogic.report(this, this.recommendListInfo.rtp_method, info.rpt_ss, this.recommendListInfo.flag_replace, (ClickInfo) null);
            this.recommendId.put(info.appid, info.rpt_ss);
        }

    }

    public void showResultItem(StoreApkInfo info) {
        if (!this.resultId.containsKey(info.appid)) {
            ReportLogic.report(this, this.searchListInfo.rtp_method, info.rpt_ss, this.searchListInfo.flag_replace, (ClickInfo) null);
            this.resultId.put(info.appid, info.rpt_ss);
        }

    }

    public StoreListInfo getResultInfo() {
        return this.searchListInfo;
    }

    public StoreListInfo getRecommend() {
        return this.recommendListInfo;
    }

    protected void onDestroy() {
        super.onDestroy();
        this.finish = true;
    }

    private static class SearchModel {
        private static final int TYPE_ERROR = -1;
        private static final int TYPE_NORMAL = 0;
        private static final int TYPE_TITLE = 1;
        private static final int TYPE_TIPS = 2;
        private static final int TYPE_RECOMMEND = 3;
        public int type;

        private SearchModel(int i) {
            this.type = i;
        }

        public String toString() {
            return "type = " + this.type;
        }
    }

    private static class ErrorHolder extends BaseViewHolder<SearchActivity.SearchModel> {
        private ErrorHolder(View view) {
            super(view);
        }

        public void onBind(SearchActivity.SearchModel searchModel, int position) {
        }
    }

    private static class TitleHolder extends BaseViewHolder<SearchActivity.SearchModel> {
        private Random random;
        private TextView title;

        private TitleHolder(View view) {
            super(view);
            this.random = new Random();
            this.title = (TextView) view.findViewById(id.title);
        }

        public void onBind(SearchActivity.SearchModel searchModel, int position) {
            String percent = this.mContext.getResources().getString(string.other_down);
            this.title.setText(this.random.nextInt(38) + 51 + "%" + percent);
        }
    }

    private static class TipsHolder extends BaseViewHolder<SearchActivity.SearchModel> {
        private TextView tips;
        private OnTipsItemClickListener listener;

        private TipsHolder(View view, OnTipsItemClickListener l) {
            super(view);
            this.listener = l;
            this.tips = (TextView) view.findViewById(id.tips);
        }

        public void onBind(SearchActivity.SearchModel searchModel, int position) {
            if (null != this.listener) {
                StoreListInfo listInfo = this.listener.getResultInfo();
                StoreApkInfo info = (StoreApkInfo) listInfo.list.get(position);
                this.tips.setText(info.appname);
                this.itemView.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        TipsHolder.this.listener.onItemClick(TipsHolder.this.tips.getText().toString());
                    }
                });
            }

        }
    }

    private static class SearchHolder extends BaseViewHolder<SearchActivity.SearchModel> {
        TextView name;
        TextView down;
        TextView count;
        TextView size;
        TextView vn;
        ImageView icon;
        Gson gson;
        OnTipsItemClickListener listener;
        private StoreListInfo resultListInfo;

        private SearchHolder(View view, OnTipsItemClickListener l) {
            super(view);
            this.gson = new Gson();
            this.listener = l;
            this.name = (TextView) view.findViewById(id.app_name);
            this.count = (TextView) view.findViewById(id.app_count);
            this.size = (TextView) view.findViewById(id.app_size);
            this.vn = (TextView) view.findViewById(id.app_version);
            this.down = (TextView) view.findViewById(id.app_down);
            this.icon = (ImageView) view.findViewById(id.app_icon);
        }

        public void onBind(SearchActivity.SearchModel o, int position) {
            this.resultListInfo = this.listener.getResultInfo();
            StoreApkInfo info = (StoreApkInfo) this.resultListInfo.list.get(position);
            Picasso.with(this.mContext).load(info.icon).placeholder(drawable.ic_loading).error(drawable.ic_loading).into(this.icon);
            this.name.setText(info.appname);
            if (null != info.downcount) {
                this.count.setVisibility(View.VISIBLE);

                try {
                    double c = Double.valueOf(info.downcount);
                    this.count.setText(Utils.downloadNum(c));
                } catch (NumberFormatException var7) {
                    this.count.setText(info.downcount);
                }
            } else {
                this.count.setVisibility(View.GONE);
            }

            try {
                this.size.setText(Utils.readableFileSize(info.size));
            } catch (NumberFormatException var6) {
                this.size.setText(info.size);
            }

            this.vn.setText(Utils.versionName(info.versionname));
            this.itemView.setTag(info);
            this.itemView.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    StoreApkInfo tag = (StoreApkInfo) view.getTag();
                    Intent intent = new Intent(SearchHolder.this.mContext, DetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("packageName", tag.href_detail);
                    bundle.putString("appName", tag.appname);
                    bundle.putString("detail_else", "icon");
                    intent.putExtra("app_detail", bundle);
                    SearchHolder.this.mContext.startActivity(intent);
                    ReportLogic.report(SearchHolder.this.mContext, SearchHolder.this.resultListInfo.rtp_method, tag.rpt_ct, SearchHolder.this.resultListInfo.flag_replace, (ClickInfo) null);
                }
            });
            this.setDownButton(this.resultListInfo, info);
            this.listener.showResultItem(info);
        }

        void setDownButton(final StoreListInfo listInfo, StoreApkInfo info) {
            int apkStatus = ApkUtils.getStatus(this.mContext, info.appid, info.apk, Integer.valueOf(info.versioncode));
            this.down.setTag(info);
            switch (apkStatus) {
                case -1:
                    this.down.setText(string.downloading);
                    this.down.setClickable(false);
                    break;
                case 0:
                    this.down.setText(string.download);
                    this.down.setClickable(true);
                    this.down.setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            SearchHolder.this.down.setClickable(false);
                            StoreApkInfo tag = (StoreApkInfo) view.getTag();
                            SearchHolder.this.down.setText(string.downloading);
                            SearchHolder.this.downAndSave(listInfo, tag, true);
                        }
                    });
                    break;
                case 1:
                    this.down.setText(string.update);
                    this.down.setClickable(true);
                    this.down.setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            SearchHolder.this.down.setClickable(false);
                            StoreApkInfo tag = (StoreApkInfo) view.getTag();
                            SearchHolder.this.down.setText(string.downloading);
                            SearchHolder.this.downAndSave(listInfo, tag, false);
                        }
                    });
                    break;
                case 2:
                    this.down.setText(string.install);
                    this.down.setClickable(true);
                    this.down.setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            StoreApkInfo tag = (StoreApkInfo) view.getTag();
                            ApkUtils.tryInstall(SearchHolder.this.mContext, new File(DownloadLogic.buildUrl(SearchHolder.this.mContext, tag.appname)));
                        }
                    });
                    break;
                case 3:
                    this.down.setText(string.open);
                    this.down.setClickable(true);
                    this.down.setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            StoreApkInfo tag = (StoreApkInfo) view.getTag();
                            ApkUtils.startApp(SearchHolder.this.mContext, tag.apk);
                        }
                    });
            }

        }

        void downAndSave(final StoreListInfo listInfo, final StoreApkInfo info, boolean report) {
            if (1 == listInfo.flag_download) {
                for (int i = 0; i < info.rpt_cd.size(); ++i) {
                    if (i == 0) {
                        DownloadCart.getInstance().setApkStatus(info.appid, -1);
                        DownloadStatus status = new DownloadStatus(0L, 0L, 0.0F, info.icon, info.appname, info.href_download, info.appid, info.apk, info.versioncode, info.rpt_dc, info.rpt_dl, listInfo.rtp_method);
                        DownloadCart.getInstance().setApkCarDownloadStatus(info.appid, status);
                        ReportLogic.report(this.mContext, listInfo.rtp_method, (String) info.rpt_cd.get(i), false, 0L, new StringCallback() {
                            public void onSuccess(Response<String> response) {
                                String body = (String) response.body();
                                if (null != body) {
                                    RptBean rptBean = (RptBean) SearchHolder.this.gson.fromJson(body, RptBean.class);
                                    if (null != rptBean && null != rptBean.href_download) {
                                        DownloadLogic.getInstance().startDownload(SearchHolder.this.mContext, rptBean.href_download, info.appname, info.appid, info.icon, info.apk, info.versioncode, info.rpt_dc, info.rpt_dl, listInfo.rtp_method);
                                        PublicDao.insert(SearchHolder.this.buildDmBean(listInfo, info, rptBean.href_download));
                                        return;
                                    }
                                }

                                DownloadCart.getInstance().remove(info.appid);
                                DownloadCart.getInstance().removeDownloadStatus(info.appid);
                                Toast.makeText(Application.getContext(), string.down_failed, View.VISIBLE).show();
                            }

                            public void onError(Response<String> response) {
                                super.onError(response);
                                DownloadCart.getInstance().remove(info.appid);
                                DownloadCart.getInstance().removeDownloadStatus(info.appid);
                                Toast.makeText(Application.getContext(), string.down_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        ReportLogic.report(this.mContext, listInfo.rtp_method, (String) info.rpt_cd.get(i), false, 0L, (StringCallback) null);
                    }
                }
            } else {
                if (report) {
                    ReportLogic.report(this.mContext, listInfo.rtp_method, info.rpt_cd, listInfo.flag_replace, (ClickInfo) null);
                }

                DownloadLogic.getInstance().startDownload(this.mContext, info.href_download, info.appname, info.appid, info.icon, info.apk, info.versioncode, info.rpt_dc, info.rpt_dl, listInfo.rtp_method);
                PublicDao.insert(this.buildDmBean(listInfo, info, (String) null));
            }

        }

        DmBean buildDmBean(StoreListInfo listInfo, StoreApkInfo info, String downloadUrl) {
            DmBean dmBean = new DmBean();
            dmBean.packageName = info.apk;
            dmBean.appId = info.appid;
            dmBean.appName = info.appname;
            dmBean.iconUrl = info.icon;
            if (null != downloadUrl) {
                dmBean.downUrl = downloadUrl;
            } else {
                dmBean.downUrl = info.href_download;
            }

            dmBean.size = info.size;
            dmBean.versionCode = info.versioncode;
            dmBean.versionName = info.versionname;
            dmBean.repDc = info.rpt_dc;
            dmBean.repInstall = info.rpt_ic;
            dmBean.repAc = info.rpt_ac;
            dmBean.repDel = info.rpt_dl;
            dmBean.method = listInfo.rtp_method;
            return dmBean;
        }
    }

    private static class RecommendHolder extends SearchActivity.SearchHolder {
        private StoreListInfo listInfo;

        private RecommendHolder(View view, OnTipsItemClickListener l) {
            super(view, l);
        }

        public void onBind(SearchActivity.SearchModel searchModel, int position) {
            this.listInfo = this.listener.getRecommend();
            StoreListInfo result = this.listener.getResultInfo();
            int i;
            if (null != result && null != result.list) {
                i = (result.list.size() > 3 ? 3 : result.list.size()) + 1;
            } else {
                i = 2;
            }

            StoreApkInfo info = (StoreApkInfo) this.listInfo.list.get(position - i);
            Picasso.with(this.mContext).load(info.icon).placeholder(drawable.ic_loading).error(drawable.ic_loading).into(this.icon);
            this.name.setText(info.appname);
            if (null != info.downcount) {
                this.count.setVisibility(View.VISIBLE);

                try {
                    double c = Double.valueOf(info.downcount);
                    this.count.setText(Utils.downloadNum(c));
                } catch (NumberFormatException var9) {
                    this.count.setText(info.downcount);
                }
            } else {
                this.count.setVisibility(View.GONE);
            }

            try {
                this.size.setText(Utils.readableFileSize(info.size));
            } catch (NumberFormatException var8) {
                this.size.setText(info.size);
            }

            this.vn.setText(Utils.versionName(info.versionname));
            this.itemView.setTag(info);
            this.itemView.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    StoreApkInfo tag = (StoreApkInfo) view.getTag();
                    Intent intent = new Intent(RecommendHolder.this.mContext, DetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("packageName", tag.href_detail);
                    bundle.putString("appName", tag.appname);
                    bundle.putString("detail_else", "icon");
                    intent.putExtra("app_detail", bundle);
                    RecommendHolder.this.mContext.startActivity(intent);
                    ReportLogic.report(RecommendHolder.this.mContext, RecommendHolder.this.listInfo.rtp_method, tag.rpt_ct, RecommendHolder.this.listInfo.flag_replace, (ClickInfo) null);
                }
            });
            this.setDownButton(this.listInfo, info);
            this.listener.showRecommendItem(info);
        }
    }

    private static class ContentAdapter extends BaseListAdapter<SearchActivity.SearchModel> {
        private OnTipsItemClickListener listener;

        private void setOnTipsClickListener(OnTipsItemClickListener l) {
            this.listener = l;
        }

        private ContentAdapter(Context context) {
            super(context);
        }

        public int getItemViewType(int position) {
            return ((SearchActivity.SearchModel) this.mData.get(position)).type;
        }

        public BaseViewHolder<SearchActivity.SearchModel> onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.e("SearchActivity", "onCreateViewHolder");
            if (viewType == 0) {
                return new SearchActivity.SearchHolder(mInflater.inflate(layout.item_more_list, parent, false), this.listener);
            } else if (viewType == 2) {
                return new SearchActivity.TipsHolder(mInflater.inflate(layout.item_search_tips, parent, false), this.listener);
            } else if (viewType == 1) {
                return new SearchActivity.TitleHolder(mInflater.inflate(layout.item_search_title, parent, false));
            } else if (viewType == -1) {
                return new SearchActivity.ErrorHolder(mInflater.inflate(layout.item_search_null, parent, false));
            } else {
                return viewType == 3 ? new SearchActivity.RecommendHolder(this.mInflater.inflate(layout.item_more_list, parent, false), listener) : null;
            }
        }
    }
}
