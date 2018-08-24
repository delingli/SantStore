package com.hai.store.data;


import android.content.Context;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.hai.store.Application;
import com.hai.store.L;
import com.hai.store.bean.DmBean;
import com.hai.store.sqlite.PublicDao;
import com.hai.store.utils.ApkUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadLogic {

    private static final String TAG = "DownloadLogic";
    private boolean DEBUG = L.DBG;
    private static final Object LOCK = new Object();
    private static DownloadLogic mApkDownload;

    private static final String DOWNLOAD_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

    private volatile List<DownloadListener> downloadListeners = new ArrayList<>();

    public static DownloadLogic getInstance() {
        synchronized (LOCK) {
            if (mApkDownload != null) {
                return mApkDownload;
            } else {
                mApkDownload = new DownloadLogic();
                return mApkDownload;
            }
        }
    }

    private Map<String, DmBean> mDownMap;
    private Queue<String> mDownPkgQueue;

    /*
    * Loop运行中的标记
    */
    private static AtomicBoolean mRunTag = new AtomicBoolean();

    /*
    * 最大限制处理个数
    * */
    private int MAX_COUNT = 0;

    private final AtomicInteger COUNTER = new AtomicInteger(MAX_COUNT);

    private Runnable downLoop = new Runnable() {
        @Override
        public void run() {
            COUNTER.set(MAX_COUNT);
            while (!mDownPkgQueue.isEmpty()) {
                mRunTag.set(true);
                if (COUNTER.get() > MAX_COUNT) {
                    SystemClock.sleep(500);
                    Log.e(TAG, "COUNTER = " + COUNTER + "sleep......");
                    continue;
                }
                COUNTER.incrementAndGet(); // ++
                final String pkgName = mDownPkgQueue.poll();

                Log.e(TAG, "downLoop pkgName : " + pkgName + ", COUNTER = " + COUNTER.get());

                DmBean dmBean = mDownMap.get(pkgName);
                PublicDao.insert(dmBean);
                startDownload(Application.getContext(), dmBean.downUrl, dmBean.appName, dmBean.appId,
                        dmBean.iconUrl, dmBean.packageName, dmBean.versionCode, dmBean.repDc,
                        dmBean.repDel, dmBean.method);
            }
            mDownMap.clear();
            Log.e(TAG, "Loop isEmpty");
            mRunTag.set(false);
        }
    };


    private DownloadLogic() {

    }

    public void addQueue(List<DmBean> list) {
        if (null == mDownMap) mDownMap = new HashMap<>();
        if (null == mDownPkgQueue) mDownPkgQueue = new LinkedList<>();

        if (null != list) {
            for (DmBean dmBean : list) {
                mDownMap.put(dmBean.packageName, dmBean);
                mDownPkgQueue.add(dmBean.packageName);
            }
            startLoop();
        }
    }

    private void startLoop() {
        if (mRunTag.get()) {
            Log.e(TAG, "loop are running or not start");
        } else {
            Log.e(TAG, "start loop");
            new Thread(downLoop).start();
        }
    }

    public static String buildUrl(Context context, String apkName) {
        return DownloadLogic.getDownloadCachePath(context) + "/" + apkName + ".apk";
    }

    public static String getDownloadCachePath(Context context) {
        return DOWNLOAD_PATH;
//        return context.getExternalCacheDir().getAbsolutePath();
    }

    public void setDownloadListener(DownloadListener downloadListener) {
        downloadListeners.add(downloadListener);
    }

    public void revokedDownloadListener(DownloadListener downloadListener) {
        if (downloadListeners.contains(downloadListener)) {
            downloadListeners.remove(downloadListener);
        }
    }

    public interface DownloadListener {

        void onProgressListener(String appId);

        void onError(String appId);

        void onStart(String appId);

        void onSuccess(String appId);
    }

    public void stopDownload(String tag) {
        if (DEBUG) Log.d(TAG, "stopDownload: " + tag);
        OkGo.getInstance().cancelTag(tag);
    }

    public void startDownload(final Context context, final String url, final String fileName,
                              final String appId, final String iconUrl, final String pkgName,
                              final String versionCode, final List<String> rpt_dc,
                              final String rpt_dl, final String rtp_method) {
        OkGo.<File>get(url)
                .tag(url)
                .execute(new FileCallback(getDownloadCachePath(context), fileName + ".apk") {
                    @Override
                    public void onSuccess(Response<File> response) {
                        if (DEBUG) Log.d(TAG, "onSuccess: ");
                        if (null != mDownMap) {
                            if (mDownMap.containsKey(pkgName) && !mDownPkgQueue.contains(pkgName)) {
                                mDownMap.remove(pkgName);
                                COUNTER.decrementAndGet();
                            }
                        }
                        DownloadCart.getInstance().setApkStatus(appId, ApkUtils.INSTALL);
                        for (DownloadListener listener : downloadListeners) {
                            if (null != listener) {
                                listener.onSuccess(appId);
                            }
                        }
                        if (null != rpt_dc) {
                            ReportLogic.report(context, rtp_method, rpt_dc, 0, null);
                        }
                        Toast.makeText(context, fileName + "下载成功", Toast.LENGTH_SHORT).show();
                        ApkUtils.install(context, DownloadLogic.getDownloadCachePath(context) + "/" + fileName + ".apk");
                    }

                    @Override
                    public void downloadProgress(Progress progress) {
                        super.downloadProgress(progress);
                        if (DEBUG) Log.d(TAG, "downloadProgress: ");
                        DownloadCart.DownloadStatus status = new DownloadCart.DownloadStatus(
                                progress.totalSize,
                                progress.currentSize,
                                progress.fraction,
                                iconUrl, fileName, url, appId, pkgName, versionCode, rpt_dc, rpt_dl, rtp_method);
                        DownloadCart.getInstance().setApkCarDownloadStatus(appId, status);
                        for (DownloadListener listener : downloadListeners) {
                            if (null != listener) {
                                listener.onProgressListener(appId);
                            }
                        }
                    }

                    @Override
                    public void onError(Response<File> response) {
                        super.onError(response);
                        if (DEBUG)
                            Log.d(TAG, "onError body: " + response.body() + ", message: " + response.message());
                        if (null != mDownMap) {
                            if (mDownMap.containsKey(pkgName) && !mDownPkgQueue.contains(pkgName)) {
                                mDownMap.remove(pkgName);
                                COUNTER.decrementAndGet();
                            }
                        }
                        if (DownloadCart.getInstance().inquire(appId)) {
                            DownloadCart.getInstance().setApkStatus(appId, ApkUtils.DOWNLOAD);
                        }
                        DownloadCart.DownloadStatus status = new DownloadCart.DownloadStatus(0, 0, 0,
                                iconUrl,
                                fileName,
                                url,
                                appId,
                                pkgName,
                                versionCode,
                                rpt_dc,
                                rpt_dl,
                                rtp_method);
                        if (null != DownloadCart.getInstance().getApkCarDownloadStatus(appId)) {
                            DownloadCart.getInstance().setApkCarDownloadStatus(appId, status);
                        }
                        for (DownloadListener listener : downloadListeners) {
                            if (null != listener) {
                                listener.onError(appId);
                            }
                        }
                        Toast.makeText(context, fileName + "下载失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onStart(Request<File, ? extends Request> request) {
                        super.onStart(request);
                        if (DEBUG) Log.d(TAG, "onStart: " + url);
                        DownloadCart.getInstance().setApkStatus(appId, ApkUtils.DOWNLOADING);
                        DownloadCart.DownloadStatus status = new DownloadCart.DownloadStatus(0, 0, 0,
                                iconUrl,
                                fileName,
                                url,
                                appId,
                                pkgName,
                                versionCode,
                                rpt_dc,
                                rpt_dl,
                                rtp_method);
                        DownloadCart.getInstance().setApkCarDownloadStatus(appId, status);
                        for (DownloadListener listener : downloadListeners) {
                            if (null != listener) {
                                listener.onStart(appId);
                            }
                        }
                        Toast.makeText(context, fileName + "开始下载", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
