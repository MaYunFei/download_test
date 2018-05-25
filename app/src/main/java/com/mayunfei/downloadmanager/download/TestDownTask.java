package com.mayunfei.downloadmanager.download;

import android.util.Log;

import com.mayunfei.downloadmanager.db.BundleBean;
import com.mayunfei.downloadmanager.db.ItemBean;
import com.mayunfei.downloadmanager.db.greendao.DaoSession;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

import static com.mayunfei.downloadmanager.db.DownState.STATUS_WAITING;

public class TestDownTask extends DownloadTask {


    public TestDownTask(OkHttpClient okHttpClient, BundleBean bundleBean, DaoSession daoSession, TaskStatusListener downloadTaskStatusListener) {
        super(okHttpClient, bundleBean, daoSession, downloadTaskStatusListener);
        event.setStatus(STATUS_WAITING);
    }

    @Override
    public void run() {
        super.run();
        int count = (int) bundleBean.getCompletedSize();
        try {

            List<ItemBean> lists  =  createItems();


            event.setTotalSize(20);
            event.setCompletedSize(0);
            Thread t = Thread.currentThread();
            String name = t.getName();
            while (!isPause() && count < 20) {
                Thread.sleep(1000);
                count++;
                event.setCompletedSize(count);
                doUpdate();
                Log.e(name, event.toString());
            }

            if (isPause()) {
                doPause();
            } else {
                doFinish();
            }
        } catch (Exception e) {
            doError(e);
        }

    }

    private List<ItemBean> createItems() {
        List<ItemBean> itemBeans = new ArrayList<>();

        for (int i = 0;i<200;i++){
            itemBeans.add(new ItemBean());
        }

        return itemBeans;
    }


}
