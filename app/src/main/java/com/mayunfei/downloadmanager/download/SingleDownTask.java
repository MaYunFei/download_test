package com.mayunfei.downloadmanager.download;

import com.mayunfei.downloadmanager.db.BundleBean;
import com.mayunfei.downloadmanager.db.ItemBean;
import com.mayunfei.downloadmanager.db.greendao.DaoSession;
import com.mayunfei.downloadmanager.db.greendao.ItemBeanDao;
import com.mayunfei.downloadmanager.download.http.ItemTask;

import java.util.List;

import okhttp3.OkHttpClient;

public class SingleDownTask extends DownloadTask implements TaskStatusListener<ItemBean> {

    ItemTask itemTask;

    public SingleDownTask(OkHttpClient okHttpClient, BundleBean bundleBean, DaoSession daoSession, TaskStatusListener downloadTaskStatusListener) {
        super(okHttpClient, bundleBean, daoSession, downloadTaskStatusListener);
    }


    @Override
    public void run() {
        super.run();
        ItemBean itemBean = null;

        List<ItemBean> itemBeans = bundleBean.getItemBeans();
        if (itemBeans.isEmpty()) {
            itemBean = createSingle();
        }else {
            itemBean = itemBeans.get(0);
        }

        itemTask = new ItemTask(getHttpClient(),itemBean,this);
        itemTask.run();

    }

    private ItemBean createSingle() {
        ItemBean itemBean = new ItemBean();
        itemBean.setBundleId(bundleBean.getId());
        itemBean.setPath(bundleBean.getPath());
        itemBean.setUrl(bundleBean.getUrl());
        itemBeanDao.insert(itemBean);

        return itemBean;
    }

    @Override
    protected void doUpdate() {
        super.doUpdate();
    }

    @Override
    public void pause() {
        super.pause();
        if (itemTask!=null)
        itemTask.pause();
    }

    @Override
    public void onPause(ItemBean entity) {
        update(entity);
        doPause();
    }


    @Override
    public void onFinish(ItemBean entity) {
        update(entity);
        doFinish();
    }

    @Override
    public void onError(ItemBean entity, Exception e) {
        update(entity);
        doError(e);
    }

    private void update(ItemBean entity) {
        itemBeanDao.update(entity);
        event.setTotalSize(entity.getTotalSize());
        event.setCompletedSize(entity.getCompletedSize());
    }

    @Override
    public void onUpdate(ItemBean entity, long speed) {
        itemBeanDao.update(entity);
        event.setCompletedSize(entity.getCompletedSize());
        event.setTotalSize(entity.getTotalSize());
        event.setSpeed(speed);
        doUpdate();
    }
}
