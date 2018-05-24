package com.mayunfei.downloadmanager;

import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.mayunfei.downloadmanager.db.BundleBean;
import com.mayunfei.downloadmanager.db.DownType;
import com.mayunfei.downloadmanager.db.greendao.BundleBeanDao;
import com.mayunfei.downloadmanager.db.greendao.DaoMaster;
import com.mayunfei.downloadmanager.db.greendao.DaoSession;
import com.mayunfei.downloadmanager.db.greendao.ItemBeanDao;
import com.mayunfei.downloadmanager.download.DownloadManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest //重度依赖
public class DownloadTest {

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
    public void addTask(){
        DownloadManager downloadManager = new DownloadManager(daoSession);
        BundleBean bundleBean = new BundleBean();
        bundleBean.setPath("/user");
        bundleBean.setCompletedSize(0);
        bundleBean.setTotalSize(100);
        bundleBean.setType(DownType.TYPE_SINGLE);

        downloadManager.addBundle(bundleBean,null);
    }

}
