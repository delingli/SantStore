package com.hai.store.data;

import android.content.Context;
import android.util.Log;

import com.hai.store.bean.DmBean;
import com.hai.store.sqlite.PublicDao;
import com.hai.store.utils.ApkUtils;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DownloadCart {

    private static final DownloadCart INSTANCE = new DownloadCart();
    private static final Object LOCK = new Object();

    /**
     * apkCarStatus 保存状态：下载中(downloading)，下载完成（install），未下载(download), 安装完成(open)
     */
    private Map<String, Integer> apkCarStatus = new LinkedHashMap<>();

    /**
     * apkCarDownloadStatus 记录下载进度
     */
    private Map<String, DownloadStatus> apkCarDownloadStatus = new LinkedHashMap<>();

    public static DownloadCart getInstance() {
        synchronized (LOCK) {
            return INSTANCE;
        }
    }

    private DownloadCart() {

    }

    public void setApkStatus(String appId, int status) {
        apkCarStatus.put(appId, status);
    }

    public Integer getApkStatus(String appId) {
        return null == apkCarStatus.get(appId) ? 0 : apkCarStatus.get(appId);
    }

    public Map<String, Integer> getApkStatus() {
        return apkCarStatus;
    }

    public boolean inquire(String appId) {
        return null != apkCarStatus.get(appId);
    }

    public void remove(String appId) {
        apkCarStatus.remove(appId);
    }

    public void removeDownloadStatus(String appId) {
        apkCarDownloadStatus.remove(appId);
    }

    public void clear() {
        apkCarStatus.clear();
    }

    public void setApkCarDownloadStatus(String appId, DownloadStatus downloadStatus) {
        apkCarDownloadStatus.put(appId, downloadStatus);
    }

    public Map<String, DownloadStatus> getApkCarDownloadStatus() {
        return apkCarDownloadStatus;
    }

    public DownloadStatus getApkCarDownloadStatus(String appId) {
        return apkCarDownloadStatus.get(appId);
    }

    public static class DownloadStatus {
        public String appId;
        public long totalSize;
        public long currentSize;
        public float fraction;
        public String iconUrl;
        public String appName;
        public String downUrl;
        public String packageName;
        public String versionCode;
        public List<String> rpt_dc;
        public String rpt_dl;
        public String rtp_method;

        public DownloadStatus(long totalSize, long currentSize, float fraction, String iconUrl,
                              String appName, String downUrl, String appId, String pkgName, String versionCode,
                              List<String> rpt_dc, String rpt_dl, String rtp_method) {
            this.currentSize = currentSize;
            this.totalSize = totalSize;
            this.fraction = fraction;
            this.iconUrl = iconUrl;
            this.appName = appName;
            this.downUrl = downUrl;
            this.appId = appId;
            this.packageName = pkgName;
            this.versionCode = versionCode;
            this.rpt_dc = rpt_dc;
            this.rpt_dl = rpt_dl;
            this.rtp_method = rtp_method;
        }
    }

    public static void initStore(Context context){
        List<DmBean> dmBeanList = PublicDao.queryList();
        if (null != dmBeanList) {
            for (DmBean d : dmBeanList) {
                if (null != d) {
                    File file = new File(DownloadLogic.buildUrl(context, d.appName));
                    if (file.isFile() && file.exists()&&file.length()==Long.valueOf(d.size)) {
                        int status = ApkUtils.checkNeedDownload(context, d.packageName, Integer.valueOf(d.versionCode));
                        INSTANCE.setApkStatus(d.appId, status == ApkUtils.DOWNLOAD ? ApkUtils.INSTALL : status);
                        INSTANCE.setApkCarDownloadStatus(d.appId, new DownloadStatus(Long.valueOf(d.size),
                                Long.valueOf(d.size), 1, d.iconUrl, d.appName,
                                d.downUrl, d.appId, d.packageName, d.versionCode,
                                d.repDc, d.repDel, d.method));
                    } else {
                        PublicDao.delete(d.packageName);
                    }
                }
            }
        }
    }
}
