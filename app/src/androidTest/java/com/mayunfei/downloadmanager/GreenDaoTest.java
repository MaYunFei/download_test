package com.mayunfei.downloadmanager;

import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.mayunfei.downloadmanager.db.BundleBean;
import com.mayunfei.downloadmanager.db.ItemBean;
import com.mayunfei.downloadmanager.db.greendao.BundleBeanDao;
import com.mayunfei.downloadmanager.db.greendao.DaoMaster;
import com.mayunfei.downloadmanager.db.greendao.DaoSession;
import com.mayunfei.downloadmanager.db.greendao.ItemBeanDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@LargeTest //重度依赖
public class GreenDaoTest {
    DaoSession daoSession;
    private BundleBeanDao bundleBeanDao;
    private ItemBeanDao itemBeanDao;

    @Before
    public void setUp(){
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(InstrumentationRegistry.getTargetContext(),"download.db");
        SQLiteDatabase writableDatabase = devOpenHelper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(writableDatabase);
        daoSession = daoMaster.newSession();
        bundleBeanDao = daoSession.getBundleBeanDao();
        itemBeanDao = daoSession.getItemBeanDao();
    }

    @After
    public void finish() {
        daoSession.clear();

    }


    @Test
    public void installTaskBundle() throws Exception{

        BundleBean bundleBean = new BundleBean();
        bundleBean.setKey("123");
        bundleBeanDao.insert(bundleBean);
        List<ItemBean> list = new ArrayList<>();
        for (int i = 0;i<5;i++){
            ItemBean itemBean = new ItemBean();
            itemBean.setBundleId(bundleBean.getId());
            list.add(itemBean);
        }

//        itemBeanDao.insertInTx(list);

        itemBeanDao.insertOrReplaceInTx(list);//使用事务插入或替换数据

        for (ItemBean itemBean : list) {
            itemBean.setTotalSize(100);
        }
        bundleBean.refresh();
        bundleBean.resetItemBeans();
        List<ItemBean> itemBeans = bundleBean.getItemBeans();
        bundleBeanDao.update(bundleBean);

        itemBeanDao.insertOrReplaceInTx(list);


        bundleBean.getItemBeans().get(0).setCompletedSize(20);
        bundleBeanDao.update(bundleBean);
        long completedSize = bundleBean.getItemBeans().get(0).getCompletedSize();



    }

    @Test
    public void clearAll() throws Exception{
        bundleBeanDao.deleteAll();
        itemBeanDao.deleteAll();
    }

    @Test
    public void bundleGetItems() throws Exception{
        BundleBean bundleBean = new BundleBean();
        bundleBean.setKey("123");
        bundleBeanDao.insert(bundleBean);
        List<ItemBean> items = bundleBean.getItemBeans();
        assertEquals(items.isEmpty(),true);
        assertEquals(bundleBean.getId()>0,true);
    }


    @Test
    public void queryBundle()throws Exception{
        BundleBean bundleBean = new BundleBean();
        bundleBean.setKey("123456");
        bundleBeanDao.insertInTx(bundleBean);
        List<BundleBean> bundleBeans = bundleBeanDao.queryRaw("WHERE key = ?", "123456");
        assertEquals(bundleBeans.isEmpty(),false);

    }
}
