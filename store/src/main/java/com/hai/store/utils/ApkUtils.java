package com.hai.store.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.hai.store.data.DownloadCart;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ApkUtils {

    public static final int DOWNLOADING = -1; //点击为空
    public static final int DOWNLOAD = 0;
    public static final int UPDATE = 1;
    public static final int INSTALL = 2;
    public static final int OPEN = 3;

    public static int getStatus(Context context, String appId, String packageName, int versioncode) {
        int status = ApkUtils.checkNeedDownload(context, packageName, versioncode);
        if (DOWNLOAD == status || UPDATE == status) {
            if (DownloadCart.getInstance().inquire(appId)) {
                return DownloadCart.getInstance().getApkStatus(appId);
            }
        }
        return status;
    }

    public static int checkNeedDownload(Context context, String packageName, int versionCode) {
        PackageManager pm = context.getPackageManager();
        if (isAvailable(context, packageName)) {
            try {
                PackageInfo infoOld = pm.getPackageInfo(packageName, 0);
                if (infoOld.versionCode >= versionCode) {
                    return OPEN;
                } else {
                    return UPDATE;
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return DOWNLOAD;
    }

    /**
     * 检查手机上是否安装了指定的软件
     */
    public static boolean isAvailable(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 启动指定app
     */
    public static void startApp(Context context, String packageName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("START_APP", "此app不支持打开");
        }
    }

    /**
     * 获取apk文件的信息
     */
    public static PackageInfo getApkInfo(String absPath, Context context) {
        PackageManager pm = context.getPackageManager();
        return pm.getPackageArchiveInfo(absPath, 0);
    }

    /**
     * 获取apk文件的名称
     */
    public static String getApkName(String absPath, Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi = pm.getPackageArchiveInfo(absPath, 0);
        if (null != pi) {
            // the secret are these two lines....
            pi.applicationInfo.sourceDir = absPath;
            pi.applicationInfo.publicSourceDir = absPath;

            // Drawable ApkIcon = pi.applicationInfo.loadIcon(pm);
            return (String) pi.applicationInfo.loadLabel(pm);
        }
        return null;
    }

    /**
     * 获取apk文件的icon
     */
    public static Drawable getApkIcon(String absPath, Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageArchiveInfo(absPath, 0);

            // the secret are these two lines....
            pi.applicationInfo.sourceDir = absPath;
            pi.applicationInfo.publicSourceDir = absPath;
            return pi.applicationInfo.loadIcon(pm);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取apk文件的信息：版本号
     */
    public static String getApkVersionName(String absPath, Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(absPath, PackageManager.GET_ACTIVITIES);
        if (pkgInfo != null) {
//            ApplicationInfo appInfo = pkgInfo.applicationInfo;
            /* 必须加这两句，不然下面icon获取是default icon而不是应用包的icon */
//            appInfo.sourceDir = absPath;
//            appInfo.publicSourceDir = absPath;
//            String appName = pm.getApplicationLabel(appInfo).toString();// 得到应用名
//            String packageName = appInfo.packageName; // 得到包名
            return pkgInfo.versionName; // 得到版本信息
            /*两个其实是一样的 */
//            return pm.getApplicationIcon(appInfo);// 得到图标信息
//            Drawable icon = appInfo.loadIcon(pm);
        }
        return null;
    }

    /**
     * 获取当前文件夹下的apk
     */
    public static List<File> getAllPkgOfFolder(Context context, String path) {
        File root = new File(path);
        File[] files = root.listFiles();
        List<File> fileList = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                String url = f.getAbsolutePath();
                Log.e("apk", url);
                if (!f.isDirectory() && url.contains(".") && ".apk".equals(url.substring(url.lastIndexOf("."), url.length()))) {
                    String pkg = getPackageName(context, url);
                    if (null != pkg) {
                        fileList.add(f);
                    }
                }
            }
        }
        return fileList;
    }

    /**
     * 根据文件路径获取包名
     */
    public static String getPackageName(Context context, String filePath) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo info = packageManager.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            return appInfo.packageName;  //得到安装包名称
        }
        return null;
    }

    /**
     * 安装一个apk文件
     */
    public static void install(Context context, String uriFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(uriFile)), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 卸载一个app
     */
    public static void uninstall(Context context, String packageName) {
        Uri packageURI = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, packageURI);
        context.startActivity(intent);
    }

    public static Drawable getAppDrawable(Context context, String pkgName) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packageInfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (PackageInfo packageInfo : packageInfos) {
            if (pkgName.equals(packageInfo.packageName)) {
                return packageInfo.applicationInfo.loadIcon(pm);
            }
        }
        return null;
    }

    /**
     * 删除 文件
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    public static void blueInstall(Context context, File file, int ia) {
        if (file == null || !file.exists())
            return;
        if (hasPermission(context) && ia == 3) {//蓝蝴蝶
            Log.d("ApkUtils", "尝试静默安装");
            directInstall(file, context);
        } else {
            normalInstall(file, context);
            Log.d("ApkUtils", "普通安装");
        }
    }

    public static void tryInstall(Context context, File file) {
        if (file == null || !file.exists())
            return;
        if (hasPermission(context)) {//有安装权限
            Log.d("ApkUtils", "尝试静默安装");
            directInstall(file, context);
        } else {
            normalInstall(file, context);
            Log.d("ApkUtils", "普通安装");
        }
    }

    public static List<String> scanAllInstallAppList(Context context) {
        List<String> appList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packageInfos = pm.getInstalledPackages(0);
        for (PackageInfo info : packageInfos) {
            if (null != info) appList.add(info.packageName);
        }
        return appList;
    }

    public static boolean hasPermission(Context context) {
        return PackageManager.PERMISSION_GRANTED == context.checkCallingOrSelfPermission(Manifest.permission.INSTALL_PACKAGES);
    }

    private static void directInstall(File files, Context context) {
        PackageInstallObserver pio = new PackageInstallObserver(files);
        PackageManager pm = context.getPackageManager();
        pm.installPackage(Uri.fromFile(files), pio, PackageManager.INSTALL_REPLACE_EXISTING, null);
    }

    private static void normalInstall(File file, Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    private static final class PackageInstallObserver extends IPackageInstallObserver.Stub {

        private final File file;

        PackageInstallObserver(File file) {
            this.file = file;
        }

        @Override
        public void packageInstalled(String packageName, int returnCode) throws RemoteException {

        }
    }
}
