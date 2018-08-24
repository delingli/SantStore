package com.hai.store.sqlite;

import com.hai.store.Application;
import com.hai.store.bean.DmBean;

import java.util.List;

public class PublicDao {

    private static StoreDao storeDao;

    public static synchronized void insert(DmBean dmBean) {
        checkDao();
        if (storeDao.queryExist(dmBean.packageName)) {
            storeDao.update(dmBean);
        } else {
            storeDao.insert(dmBean);
        }
    }

    public static synchronized List<DmBean> queryList() {
        checkDao();
        return storeDao.query();
    }

    public static synchronized DmBean queryBean(String pkgName) {
        checkDao();
        return storeDao.queryPkgName(pkgName);
    }

    public static synchronized void delete(String pkgName) {
        checkDao();
        storeDao.delete(pkgName);
    }

    private static synchronized void checkDao() {
        if (null == storeDao) storeDao = new StoreDao(Application.getContext());
    }
}
