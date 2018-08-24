package com.hai.store.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hai.store.bean.DmBean;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.hai.store.sqlite.DBConstant.NAME;
import static com.hai.store.sqlite.DBConstant.RPT_AC;
import static com.hai.store.sqlite.DBConstant.RPT_DEL;
import static com.hai.store.sqlite.DBConstant.RPT_DOWN_ED;
import static com.hai.store.sqlite.DBConstant.RPT_INSTALL;
import static com.hai.store.sqlite.DBConstant.RPT_MET;
import static com.hai.store.sqlite.DBConstant.S_DID;
import static com.hai.store.sqlite.DBConstant.S_ICON;
import static com.hai.store.sqlite.DBConstant.S_NAME;
import static com.hai.store.sqlite.DBConstant.S_PKG;
import static com.hai.store.sqlite.DBConstant.S_SIZE;
import static com.hai.store.sqlite.DBConstant.S_URL;
import static com.hai.store.sqlite.DBConstant.S_VC;
import static com.hai.store.sqlite.DBConstant.S_VN;

public class StoreDao {

    private StoreSQLiteHelper mySQLiteHelper;
    private Gson gson;

    public StoreDao(Context context) {
        if (null == mySQLiteHelper) {
            mySQLiteHelper = StoreSQLiteHelper.getInstance(context);
        }
        gson = new Gson();
    }

    /*增*/
    public void insert(DmBean dmBean) {
        SQLiteDatabase database = mySQLiteHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(S_PKG, dmBean.packageName);
        putValues(dmBean, cv);
        database.insert(NAME, null, cv);
        database.close();
    }

    /*改*/
    public void update(DmBean dmBean) {
        SQLiteDatabase database = mySQLiteHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        putValues(dmBean, cv);
        String whereClause = S_PKG + " = ?";
        String[] whereArgs = new String[]{dmBean.packageName};
        database.update(NAME, cv, whereClause, whereArgs);
        database.close();
    }

    /*删*/
    public void delete(String packageName) {
        SQLiteDatabase database = mySQLiteHelper.getWritableDatabase();
        String whereClause = S_PKG + " = ?";  //占位符
        String[] whereArgs = new String[]{packageName};
        database.delete(NAME, whereClause, whereArgs);
        database.close();
    }

    /*查*/
    public List<DmBean> query() {
        List<DmBean> dm = new ArrayList<>();
        SQLiteDatabase database = mySQLiteHelper.getWritableDatabase();
        Cursor cursor = database.query(NAME, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                DmBean dmBean = getDmBean(cursor);
                dm.add(dmBean);
            } while (cursor.moveToNext());
        }
        if (null != cursor) cursor.close();
        database.close();
        return dm;
    }

    public DmBean queryPkgName(String pkgName) {
        DmBean bean = null;
        SQLiteDatabase database = mySQLiteHelper.getWritableDatabase();
        String selection = S_PKG + " = ?"; // 查询条件
        String[] selectionArgs = new String[]{pkgName};// 条件的值
        Cursor cursor = database.query(NAME, null, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            bean = getDmBean(cursor);
        }
        if (null != cursor) cursor.close();
        database.close();
        return bean;
    }

    public boolean queryExist(String pkgName) {
        boolean exist = false;
        SQLiteDatabase database = mySQLiteHelper.getWritableDatabase();
        String[] columns = new String[]{S_PKG}; // 要返回哪几个列的数据.如果传入null就等价于select  *,
        String selection = S_PKG + " = ?"; // 查询条件
        String[] selectionArgs = new String[]{pkgName};// 条件的值
        Cursor cursor = database.query(NAME, columns, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            exist = true;
        }
        if (null != cursor) cursor.close();
        database.close();
        return exist;
    }

    @NonNull
    private DmBean getDmBean(Cursor cursor) {
        DmBean dmBean = new DmBean();
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        dmBean.packageName = cursor.getString(1);
        dmBean.appId = cursor.getString(2);
        dmBean.appName = cursor.getString(3);
        dmBean.versionCode = cursor.getString(4);
        dmBean.versionName = cursor.getString(5);
        dmBean.size = cursor.getString(6);
        dmBean.iconUrl = cursor.getString(7);
        dmBean.downUrl = cursor.getString(8);

        String downed = cursor.getString(9);
        if (!TextUtils.isEmpty(downed))
            dmBean.repDc = gson.fromJson(downed, type);

        String repInstall = cursor.getString(10);
        if (!TextUtils.isEmpty(repInstall))
            dmBean.repInstall = gson.fromJson(repInstall, type);

        String repAc = cursor.getString(11);
        if (!TextUtils.isEmpty(repAc))
            dmBean.repAc = gson.fromJson(repAc, type);

        String repDel = cursor.getString(12);
        if (!TextUtils.isEmpty(repDel))
            dmBean.repDel = repDel;

        String method = cursor.getString(13);
        if (!TextUtils.isEmpty(method)) {
            dmBean.method = method;
        }
        return dmBean;
    }

    private void putValues(DmBean dmBean, ContentValues cv) {
        cv.put(S_DID, dmBean.appId);
        cv.put(S_NAME, dmBean.appName);
        cv.put(S_VC, dmBean.versionCode);
        cv.put(S_VN, dmBean.versionName);
        cv.put(S_SIZE, dmBean.size);
        cv.put(S_ICON, dmBean.iconUrl);
        cv.put(S_URL, dmBean.downUrl);
        if (null != dmBean.repDc) {
            cv.put(RPT_DOWN_ED, gson.toJson(dmBean.repDc));
        }
        if (null != dmBean.repInstall) {
            cv.put(RPT_INSTALL, gson.toJson(dmBean.repInstall));
        }
        if (null != dmBean.repAc) {
            cv.put(RPT_AC, gson.toJson(dmBean.repAc));
        }
        if (null != dmBean.repDel) {
            cv.put(RPT_DEL, dmBean.repDel);
        }
        if (null != dmBean.method) {
            cv.put(RPT_MET, dmBean.method);
        }
    }
}
