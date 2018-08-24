package com.hai.store.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.hai.store.bean.DmBean;
import com.hai.store.data.DownloadCart;
import com.hai.store.data.ReportLogic;
import com.hai.store.sqlite.PublicDao;
import com.hai.store.utils.ApkUtils;

import java.util.Iterator;
import java.util.Map;

public class ApkReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getData().getSchemeSpecificPart();
        if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_ADDED)) {
            Log.i("ApkReceiver", "onReceive: ADDED");
            checkSB(context, packageName, true);
        } else if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_REPLACED)) {
            Log.i("ApkReceiver", "onReceive: REPLACED");
            checkSB(context, packageName, false);
        } else if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_REMOVED)) {
            Log.i("ApkReceiver", "onReceive: " + packageName);
            checkRemove(packageName);
        }
    }

    private void checkSB(Context context, String packageName, boolean added) {
        Map<String, DownloadCart.DownloadStatus> status = DownloadCart.getInstance().getApkCarDownloadStatus();
        for (String appId : status.keySet()) {
            DownloadCart.DownloadStatus downloadStatus = status.get(appId);
            if (null != downloadStatus && packageName.equals(downloadStatus.packageName)) {
                DownloadCart.getInstance().setApkStatus(downloadStatus.appId, ApkUtils.OPEN);
                if (added) {
                    DmBean dmBean = PublicDao.queryBean(packageName);
                    if (null != dmBean) {
                        ReportLogic.report(context, dmBean.method, dmBean.repInstall, 0, null);
                        ApkUtils.startApp(context, packageName);
                        ReportLogic.report(context, dmBean.method, dmBean.repAc, 0, null);
                    }
                }
            }
        }
    }

    private void checkRemove(String packageName) {
        Map<String, DownloadCart.DownloadStatus> status = DownloadCart.getInstance().getApkCarDownloadStatus();
        Iterator<Map.Entry<String, DownloadCart.DownloadStatus>> it = status.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, DownloadCart.DownloadStatus> next = it.next();
            DownloadCart.DownloadStatus downloadStatus = next.getValue();
            if (null != downloadStatus && packageName.equals(downloadStatus.packageName)) {
                it.remove();
                DownloadCart.getInstance().remove(downloadStatus.appId);
                return;
            }
        }
    }
}
