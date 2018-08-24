package com.hai.store.base;

import com.hai.store.bean.StoreApkInfo;
import com.hai.store.bean.StoreListInfo;

public interface OnTipsItemClickListener {

    void onItemClick(String tips);

    void showRecommendItem(StoreApkInfo info);

    void showResultItem(StoreApkInfo info);

    StoreListInfo getResultInfo();

    StoreListInfo getRecommend();
}
