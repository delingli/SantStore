package com.hai.appstore;

import android.test.AndroidTestCase;
import android.util.Log;

import com.hai.store.bean.DmBean;
import com.hai.store.sqlite.StoreDao;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sant on 7/20/17.
 */

public class DaoTest extends AndroidTestCase {
    private StoreDao dao;
    private DmBean mDmBean;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        dao = new StoreDao(mContext);
        ArrayList<String> acurl = new ArrayList<>();
        ArrayList<String> dcurl = new ArrayList<>();
        ArrayList<String> iurl = new ArrayList<>();
        acurl.add("acurl1111");
        acurl.add("acurl2222");
        acurl.add("acurl3333");
        dcurl.add("dcurl1111");
        dcurl.add("dcurl2222");
        dcurl.add("dcurl3333");
        iurl.add("iurl1111");
        iurl.add("iurl2222");
        iurl.add("iurl3333");
        mDmBean = new DmBean();
        mDmBean.appId = "appid";
        mDmBean.packageName = "pkg";
        mDmBean.appName = "name";
        mDmBean.versionCode = "code";
        mDmBean.versionName = "vname";
        mDmBean.size = null;
        mDmBean.iconUrl = "iconurl";
        mDmBean.downUrl = "downurl";
        mDmBean.repDc = dcurl;
        mDmBean.repInstall = iurl;
        mDmBean.repAc = null;
        mDmBean.repDel = "delUrl";
        mDmBean.method = null;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testInsert() throws Exception {
        dao.insert(mDmBean);
        DmBean bean = dao.queryPkgName(mDmBean.packageName);
        assertEquals(mDmBean.packageName, bean.packageName);
        assertEquals(null, bean.method);
        assertEquals(null, bean.size);
    }

    public void testQuery() throws Exception {
        List<DmBean> query = dao.query();
        Log.e("testQuery", "query == " + query);
        if (null != query) {
            Log.e("testQuery", "null != query");
            DmBean dmBean = query.get(0);
            assertEquals("pkg", dmBean.packageName);
        }
    }

    public void testQueryPkgName() throws Exception {
        boolean pkg = dao.queryExist("pkg");
        if (pkg) {
            assertEquals(null, mDmBean.size);
            mDmBean.size = "8765";
            dao.update(mDmBean);
            DmBean dmBean = dao.queryPkgName("pkg");
            assertEquals("8765", dmBean.size);
            boolean pkg1 = dao.queryExist("pkg");
            assertEquals(true, pkg1);
        } else {
            dao.insert(mDmBean);
            DmBean dmBean = dao.queryPkgName("pkg");
            assertEquals("pkg", dmBean.packageName);
            boolean pkg1 = dao.queryExist("pkg");
            assertEquals(true, pkg1);
        }
    }

    public void testDelete() throws Exception {
        dao.delete("pkg");
        boolean pkg = dao.queryExist("pkg");
        assertEquals(false, pkg);
    }
}
