package com.hai.store.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hai.store.Application;
import com.hai.store.R;
import com.hai.store.base.BaseListAdapter;
import com.hai.store.base.BaseViewHolder;
import com.hai.store.base.OnTipsItemClickListener;
import com.hai.store.base.SConstant;
import com.hai.store.bean.DmBean;
import com.hai.store.bean.RptBean;
import com.hai.store.bean.StoreApkInfo;
import com.hai.store.bean.StoreListInfo;
import com.hai.store.data.DownloadCart;
import com.hai.store.data.DownloadLogic;
import com.hai.store.data.ReportLogic;
import com.hai.store.data.SearchLogic;
import com.hai.store.fragment.MoreListFragment;
import com.hai.store.sqlite.PublicDao;
import com.hai.store.utils.ApkUtils;
import com.hai.store.utils.Utils;
import com.hai.store.view.FlowLayout;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static com.hai.store.base.SConstant.APP_NAME;
import static com.hai.store.base.SConstant.DETAIL_ELSE;
import static com.hai.store.base.SConstant.PKG_NAME;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, OnTipsItemClickListener, DownloadLogic.DownloadListener {

    private static final String TAG = "SearchActivity";
    private FlowLayout flowLayout;
    private TextView hot;
    private LinearLayout searchTop, searchContent;
    private RelativeLayout searchError;
    private RecyclerView recyclerView;
    private ContentAdapter adapter;
    private ProgressDialog mProgressDialog;
    private boolean finish;

    private Handler handler = new Handler(Looper.getMainLooper());

    private List<SearchModel> pageList; //搜索页的数据映射

    private StoreListInfo hotListInfo; //热搜

    private List<StoreApkInfo> hotList; //热搜应用名

    private Gson gson = new Gson();

    private StoreListInfo searchListInfo; //搜索结果

    private StoreListInfo recommendListInfo; //推荐结果

    private HashMap<String, List<String>> recommendId = new HashMap<>();

    private HashMap<String, List<String>> resultId = new HashMap<>();

    private static final int resultCount = 3;

    private String[] colors = new String[]{
            "#FF6A6A", "#EE2C2C", "#D15FEE", "#7EC0EE", "#1E90FF"
    };

    private Random random = new Random();
    private SearchView.SearchAutoComplete autoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//            }
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        setContentView(R.layout.activity_search);
        findView();
        hideHot();
        initSearchActivity();
        showKeyboard();
        DownloadLogic.getInstance().setDownloadListener(this);
    }

    private void findView() {
        ImageView dm = (ImageView) findViewById(R.id.goto_dm);
        dm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchActivity.this, DMActivity.class));
            }
        });
        searchContent = (LinearLayout) findViewById(R.id.search_content);
        searchError = (RelativeLayout) findViewById(R.id.search_error);
        recyclerView = (RecyclerView) findViewById(R.id.search_recycler);
        recyclerView.setItemAnimator(null);
        searchTop = (LinearLayout) findViewById(R.id.search_top);
        hot = (TextView) findViewById(R.id.search_hot);
        flowLayout = (FlowLayout) findViewById(R.id.flow_layout);
        autoText = (SearchView.SearchAutoComplete) findViewById(R.id.search_src_text);
        ImageView back = (ImageView) findViewById(R.id.search_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        SearchView searchView = (SearchView) findViewById(R.id.search_view);
        searchView.onActionViewExpanded();
        searchView.setQueryHint("你想找什么?");
        searchView.setOnQueryTextListener(this);
    }

    private void setFlowParams() {
        FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(12, 12, 12, 12);
        for (final StoreApkInfo info : hotList) {
            TextView tv = new TextView(this);
            tv.setPadding(12, 6, 12, 6);
            tv.setText(info.appname);
            tv.setTextSize(14);
            tv.setBackgroundResource(R.drawable.bg_app_down);
            tv.setTextColor(Color.parseColor(colors[random.nextInt(colors.length)]));
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setText(info.appname);
                }
            });
            flowLayout.addView(tv, params);
        }
    }

    private void setText(String text) {
        autoText.setText(text);
        autoText.setSelection(text.length());
        showKeyboard();
    }

    private void showKeyboard() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (im != null)
                    im.showSoftInput(autoText, 0);
            }
        }, 500);
    }

    private void dismissKeyboard() {
        handler.post(new Runnable() { //post
            @Override
            public void run() {
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (im != null)
                    im.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        String trim = query.trim();
        if (TextUtils.isEmpty(trim)) {
            Toast.makeText(this, "请输入应用名称", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (trim.length() > 20) {
            Toast.makeText(this, "不能超过20个字符", Toast.LENGTH_SHORT).show();
            return true;
        }
        textSubmit(trim);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (null == pageList) pageList = new ArrayList<>();
        String trim = newText.trim();
        if (trim.length() > 20) {
            initSearchActivity();
        } else {
            if (trim.length() >= 2) {
                textChange(trim);
            } else {
                initSearchActivity();
            }
        }
        return true;
    }

    private void initSearchActivity() {
        SearchLogic.stopSearch();
        recyclerView.setVisibility(View.GONE);
        showContent();
        if (null == hotList) {
            SearchLogic.getHotSearch(new StringCallback() {
                @Override
                public void onSuccess(Response<String> response) {
                    if (finish) return;
                    if (handleHot(response.body())) {
                        hotList = hotListInfo.list;
                        flowLayout.removeAllViews();
                        flowLayout.setGravity(Gravity.TOP);
                        setFlowParams();
                        showHot();
                    } else {
                        Toast.makeText(Application.getContext(), "热门搜索获取失败", Toast.LENGTH_SHORT).show();
                        hideHot();
                        searchContent.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onError(Response<String> response) {
                    super.onError(response);
                    if (finish) return;
                    Toast.makeText(Application.getContext(), "热门搜索获取失败", Toast.LENGTH_SHORT).show();
                    hideHot();
                    searchContent.setVisibility(View.GONE);
                }
            });
        } else {
            showHot();
        }
    }

    private boolean handleHot(String body) {
        hotListInfo = gson.fromJson(body, StoreListInfo.class);
        return null != hotListInfo && null == hotListInfo.err && hotListInfo.list.size() > 0;
    }

    /*显示网络错误*/
    private void showError() {
        searchError.setVisibility(View.VISIBLE);
        searchContent.setVisibility(View.GONE);
    }

    /*显示所有内容*/
    private void showContent() {
        searchError.setVisibility(View.GONE);
        searchContent.setVisibility(View.VISIBLE);
    }

    /*显示热门搜索*/
    private void showHot() {
        flowLayout.setVisibility(View.VISIBLE); //显示热搜
        hot.setText(R.string.hot_search);
        searchTop.setVisibility(View.VISIBLE); //显示top
        recyclerView.setVisibility(View.GONE); //隐藏结果
    }

    /*显示搜索结果*/
    private void showResult() {
        flowLayout.setVisibility(View.GONE);
        hot.setText(R.string.search_result);
        searchTop.setVisibility(View.VISIBLE); //显示top
        showContent();
    }

    private void hideHot() {
        flowLayout.setVisibility(View.GONE);
        searchTop.setVisibility(View.GONE);
    }

    private void textSubmit(String text) {
        submitHandler(text); //处理提交
    }

    private void textChange(String text) {
        hideHot();
        changeHandler(text);
    }

    private void submitHandler(String text) {
        SearchLogic.stopSearch();
        showProgressDialog();
        SearchLogic.getSearchContent(text, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                dismissProgressDialog();
                if (finish) return;
                showResult();
                dismissKeyboard();
                pageList.clear();
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
                if (null == adapter) {
                    recyclerView.setAdapter(adapter = new ContentAdapter(SearchActivity.this));
                    adapter.setOnTipsClickListener(SearchActivity.this);
                }
                if (handleSearch(response.body())) {
                    for (int i = 0; i < searchListInfo.list.size(); i++) {
                        pageList.add(new SearchModel(SearchModel.TYPE_NORMAL)); //添加其他结果
                        if (pageList.size() == resultCount) break;
                    }
                } else {
                    pageList.add(new SearchModel(SearchModel.TYPE_ERROR));
                }
                adapter.setData(pageList);
                loadRecommend();
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                dismissProgressDialog();
                if (finish) return;
                showError();
                dismissKeyboard();
            }
        });
    }

    private void loadRecommend() {
        SearchLogic.getRecommend(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                if (finish) return;
                if (handleRecommend(response.body())) {
                    List<SearchModel> list = new ArrayList<>();
                    list.add(new SearchModel(SearchModel.TYPE_TITLE));
                    for (int i = 0; i < recommendListInfo.list.size(); i++) {
                        list.add(new SearchModel(SearchModel.TYPE_RECOMMEND));
                    }
                    adapter.addData(list);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                Log.e(TAG, "loadRecommend error " + response);
            }
        });
    }

    private void showProgressDialog() {
        String title = "提示";
        String content = "正在搜索...";
        mProgressDialog = ProgressDialog.show(this, title, content, true, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                SearchLogic.stopSearch();
            }
        });
    }

    private void dismissProgressDialog() {
        if (null != mProgressDialog)
            mProgressDialog.dismiss();
    }

    private void changeHandler(String text) {
        pageList.clear();
        SearchLogic.stopSearch();
        SearchLogic.getSearchContent(text, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                if (finish) return;
                showContent();
                if (handleSearch(response.body())) {
                    recyclerView.setVisibility(View.VISIBLE);
                    for (int i = 0; i < searchListInfo.list.size(); i++) {
                        if (i == 0) {
                            pageList.add(new SearchModel(SearchModel.TYPE_NORMAL)); //添加最佳匹配
                            continue;
                        }
                        pageList.add(new SearchModel(SearchModel.TYPE_TIPS));
                    }
                    recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
                    if (null == adapter) {
                        recyclerView.setAdapter(adapter = new ContentAdapter(SearchActivity.this));
                        adapter.setOnTipsClickListener(SearchActivity.this);
                    }
                    adapter.setData(pageList);
                } else {
                    recyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                if (finish) return;
                showContent();
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    private boolean handleSearch(String body) {
        searchListInfo = gson.fromJson(body, StoreListInfo.class);
        return null != searchListInfo && null == searchListInfo.err && searchListInfo.list.size() > 0;
    }

    private boolean handleRecommend(String body) {
        recommendListInfo = gson.fromJson(body, StoreListInfo.class);
        return null != recommendListInfo && null == recommendListInfo.err && recommendListInfo.list.size() > 0;
    }

    @Override
    public void onProgressListener(String appId) {

    }

    @Override
    public void onError(String appId) {
        if (null != adapter)
            adapter.notifyDataSetChanged();
    }

    @Override
    public void onStart(String appId) {

    }

    @Override
    public void onSuccess(String appId) {
        if (null != adapter)
            adapter.notifyDataSetChanged();
    }

    private static class ContentAdapter extends BaseListAdapter<SearchModel> {

        private OnTipsItemClickListener listener;

        private void setOnTipsClickListener(OnTipsItemClickListener l) {
            listener = l;
        }

        private ContentAdapter(Context context) {
            super(context);
        }

        @Override
        public int getItemViewType(int position) {
            return mData.get(position).type;
        }

        @Override
        public BaseViewHolder<SearchModel> onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.e(TAG, "onCreateViewHolder");
            if (viewType == SearchModel.TYPE_NORMAL) {
                return new SearchHolder(mInflater.inflate(R.layout.item_more_list, parent, false), listener);
            }
            if (viewType == SearchModel.TYPE_TIPS) {
                return new TipsHolder(mInflater.inflate(R.layout.item_search_tips, parent, false), listener);
            }
            if (viewType == SearchModel.TYPE_TITLE) {
                return new TitleHolder(mInflater.inflate(R.layout.item_search_title, parent, false));
            }
            if (viewType == SearchModel.TYPE_ERROR) {
                return new ErrorHolder(mInflater.inflate(R.layout.item_search_null, parent, false));
            }
            if (viewType == SearchModel.TYPE_RECOMMEND) {
                return new RecommendHolder(mInflater.inflate(R.layout.item_more_list, parent, false), listener);
            }
            return null;
        }
    }

    private static class RecommendHolder extends SearchHolder {

        private StoreListInfo listInfo;

        private RecommendHolder(View view, OnTipsItemClickListener l) {
            super(view, l);
        }

        @Override
        public void onBind(SearchModel searchModel, int position) {
            listInfo = listener.getRecommend();
            int i;
            StoreListInfo result = listener.getResultInfo();
            if (null != result && null != result.list) {
                i = (result.list.size() > resultCount ? resultCount : result.list.size()) + 1;
            } else {
                i = 2;
            }
            StoreApkInfo info = listInfo.list.get(position - i);
            Picasso.with(mContext).load(info.icon).placeholder(R.drawable.ic_loading).error(R.drawable.ic_loading).into(icon);
            name.setText(info.appname);
            if (null != info.downcount) {
                count.setVisibility(View.VISIBLE);
                try {
                    double c = Double.valueOf(info.downcount);
                    count.setText(Utils.downloadNum(c));
                } catch (NumberFormatException e) {
                    count.setText(info.downcount);
                }
            } else {
                count.setVisibility(View.GONE);
            }

            try {
                size.setText(Utils.readableFileSize(info.size));
            } catch (NumberFormatException e) {
                size.setText(info.size);
            }

            vn.setText(Utils.versionName(info.versionname));

            itemView.setTag(info);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    StoreApkInfo tag = (StoreApkInfo) view.getTag();
                    Intent intent = new Intent(mContext, DetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(PKG_NAME, tag.href_detail);
                    bundle.putString(APP_NAME, tag.appname);
                    bundle.putString(DETAIL_ELSE, SConstant.TMODE_ICON);
                    intent.putExtra(DetailActivity.DETAIL, bundle);
                    mContext.startActivity(intent);
                    ReportLogic.report(mContext, listInfo.rtp_method, tag.rpt_ct, listInfo.flag_replace, null);
                }
            });

            setDownButton(listInfo, info);

            listener.showRecommendItem(info);
        }
    }

    private static class SearchHolder extends BaseViewHolder<SearchModel> {

        TextView name, down, count, size, vn;
        ImageView icon;
        Gson gson = new Gson();
        OnTipsItemClickListener listener;
        private StoreListInfo resultListInfo;

        private SearchHolder(View view, OnTipsItemClickListener l) {
            super(view);
            listener = l;
            name = (TextView) view.findViewById(R.id.app_name);
            count = (TextView) view.findViewById(R.id.app_count);
            size = (TextView) view.findViewById(R.id.app_size);
            vn = (TextView) view.findViewById(R.id.app_version);
            down = (TextView) view.findViewById(R.id.app_down);
            icon = (ImageView) view.findViewById(R.id.app_icon);
        }

        @Override
        public void onBind(SearchModel o, int position) {
            resultListInfo = listener.getResultInfo();
            StoreApkInfo info = resultListInfo.list.get(position);
            Picasso.with(mContext).load(info.icon).placeholder(R.drawable.ic_loading).error(R.drawable.ic_loading).into(icon);
            name.setText(info.appname);

            if (null != info.downcount) {
                count.setVisibility(View.VISIBLE);
                try {
                    double c = Double.valueOf(info.downcount);
                    count.setText(Utils.downloadNum(c));
                } catch (NumberFormatException e) {
                    count.setText(info.downcount);
                }
            } else {
                count.setVisibility(View.GONE);
            }

            try {
                size.setText(Utils.readableFileSize(info.size));
            } catch (NumberFormatException e) {
                size.setText(info.size);
            }

            vn.setText(Utils.versionName(info.versionname));

            itemView.setTag(info);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    StoreApkInfo tag = (StoreApkInfo) view.getTag();
                    Intent intent = new Intent(mContext, DetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(PKG_NAME, tag.href_detail);
                    bundle.putString(APP_NAME, tag.appname);
                    bundle.putString(DETAIL_ELSE, SConstant.TMODE_ICON);
                    intent.putExtra(DetailActivity.DETAIL, bundle);
                    mContext.startActivity(intent);
                    ReportLogic.report(mContext, resultListInfo.rtp_method, tag.rpt_ct, resultListInfo.flag_replace, null);
                }
            });

            setDownButton(resultListInfo, info);

            listener.showResultItem(info);
        }

        void setDownButton(final StoreListInfo listInfo, StoreApkInfo info) {
            int apkStatus = ApkUtils.getStatus(mContext, info.appid, info.apk, Integer.valueOf(info.versioncode));
            down.setTag(info);
            switch (apkStatus) {
                case ApkUtils.INSTALL:
                    down.setText(R.string.install);
                    down.setClickable(true);
                    down.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            StoreApkInfo tag = (StoreApkInfo) view.getTag();
                            ApkUtils.tryInstall(mContext, new File(DownloadLogic.buildUrl(mContext, tag.appname)));
//                            ApkUtils.install(mContext, DownloadLogic.buildUrl(mContext, tag.appname));
                        }
                    });
                    break;
                case ApkUtils.DOWNLOAD:
                    down.setText(R.string.download);
                    down.setClickable(true);
                    down.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            down.setClickable(false);
                            StoreApkInfo tag = (StoreApkInfo) view.getTag();
                            down.setText(R.string.downloading);
                            downAndSave(listInfo, tag, true);
                        }
                    });
                    break;
                case ApkUtils.DOWNLOADING:
                    down.setText(R.string.downloading);
                    down.setClickable(false);
                    break;
                case ApkUtils.OPEN:
                    down.setText(R.string.open);
                    down.setClickable(true);
                    down.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            StoreApkInfo tag = (StoreApkInfo) view.getTag();
                            ApkUtils.startApp(mContext, tag.apk);
                        }
                    });
                    break;
                case ApkUtils.UPDATE:
                    down.setText(R.string.update);
                    down.setClickable(true);
                    down.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            down.setClickable(false);
                            StoreApkInfo tag = (StoreApkInfo) view.getTag();
                            down.setText(R.string.downloading);
                            downAndSave(listInfo, tag, false);
                        }
                    });
                    break;
                default:
                    break;
            }
        }

        void downAndSave(final StoreListInfo listInfo, final StoreApkInfo info, boolean report) {
            if (1 == listInfo.flag_download) {
                for (int i = 0; i < info.rpt_cd.size(); i++) {
                    if (i == 0) {
                        DownloadCart.getInstance().setApkStatus(info.appid, ApkUtils.DOWNLOADING);
                        DownloadCart.DownloadStatus status = new DownloadCart.DownloadStatus(0, 0, 0,
                                info.icon,
                                info.appname,
                                info.href_download,
                                info.appid,
                                info.apk,
                                info.versioncode,
                                info.rpt_dc,
                                info.rpt_dl,
                                listInfo.rtp_method);
                        DownloadCart.getInstance().setApkCarDownloadStatus(info.appid, status);
                        ReportLogic.report(mContext, listInfo.rtp_method, info.rpt_cd.get(i), false, 0, new StringCallback() {
                            @Override
                            public void onSuccess(Response<String> response) {
                                String body = response.body();
                                if (null != body) {
                                    RptBean rptBean = gson.fromJson(body, RptBean.class);
                                    if (null != rptBean && null != rptBean.href_download) {
                                        DownloadLogic.getInstance().startDownload(mContext, rptBean.href_download, info.appname,
                                                info.appid, info.icon, info.apk, info.versioncode, info.rpt_dc, info.rpt_dl, listInfo.rtp_method);
                                        PublicDao.insert(buildDmBean(listInfo, info, rptBean.href_download));
                                        return;
                                    }
                                }
                                DownloadCart.getInstance().remove(info.appid);
                                DownloadCart.getInstance().removeDownloadStatus(info.appid);
                                Toast.makeText(Application.getContext(), R.string.down_failed, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(Response<String> response) {
                                super.onError(response);
                                DownloadCart.getInstance().remove(info.appid);
                                DownloadCart.getInstance().removeDownloadStatus(info.appid);
                                Toast.makeText(Application.getContext(), R.string.down_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        ReportLogic.report(mContext, listInfo.rtp_method, info.rpt_cd.get(i), false, 0, null);
                    }
                }
            } else {
                if (report) {
                    ReportLogic.report(mContext, listInfo.rtp_method, info.rpt_cd, listInfo.flag_replace, null);
                }
                DownloadLogic.getInstance().startDownload(mContext, info.href_download, info.appname,
                        info.appid, info.icon, info.apk, info.versioncode, info.rpt_dc, info.rpt_dl, listInfo.rtp_method);
                PublicDao.insert(buildDmBean(listInfo, info, null));
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

    private static class TipsHolder extends BaseViewHolder<SearchModel> {

        private TextView tips;
        private OnTipsItemClickListener listener;

        private TipsHolder(View view, OnTipsItemClickListener l) {
            super(view);
            listener = l;
            tips = (TextView) view.findViewById(R.id.tips);
        }

        @Override
        public void onBind(SearchModel searchModel, int position) {
            if (null != listener) {
                StoreListInfo listInfo = listener.getResultInfo();
                StoreApkInfo info = listInfo.list.get(position);
                tips.setText(info.appname);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(tips.getText().toString());
                    }
                });
            }
        }
    }

    @Override
    public void onItemClick(String tips) {
        setText(tips);
    }

    @Override
    public void showRecommendItem(StoreApkInfo info) {
        if (!recommendId.containsKey(info.appid)) {
            ReportLogic.report(this, recommendListInfo.rtp_method, info.rpt_ss, recommendListInfo.flag_replace, null);
            recommendId.put(info.appid, info.rpt_ss);
        }
    }

    @Override
    public void showResultItem(StoreApkInfo info) {
        if (!resultId.containsKey(info.appid)) {
            ReportLogic.report(this, searchListInfo.rtp_method, info.rpt_ss, searchListInfo.flag_replace, null);
            resultId.put(info.appid, info.rpt_ss);
        }
    }

    @Override
    public StoreListInfo getResultInfo() {
        return searchListInfo;
    }

    @Override
    public StoreListInfo getRecommend() {
        return recommendListInfo;
    }

    private static class TitleHolder extends BaseViewHolder<SearchModel> {

        private Random random = new Random();
        private TextView title;

        private TitleHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
        }

        @Override
        public void onBind(SearchModel searchModel, int position) {
            String percent = mContext.getResources().getString(R.string.other_down);
            title.setText((random.nextInt(38) + 51) + "%" + percent);
        }
    }

    private static class ErrorHolder extends BaseViewHolder<SearchModel> {

        private ErrorHolder(View view) {
            super(view);
        }

        @Override
        public void onBind(SearchModel searchModel, int position) {

        }
    }

    private static class SearchModel {
        private static final int
                TYPE_ERROR = -1,//空
                TYPE_NORMAL = 0,//默认
                TYPE_TITLE = 1,//标题
                TYPE_TIPS = 2,//提示
                TYPE_RECOMMEND = 3;

        public int type;// 类型 标题或普通

        private SearchModel(int i) {
            type = i;
        }

        @Override
        public String toString() {
            return "type = " + type;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish = true;
    }
}
