package com.mayunfei.downloadmanager.download;

import com.mayunfei.downloadmanager.db.BundleBean;
import com.mayunfei.downloadmanager.db.DbUtil;
import com.mayunfei.downloadmanager.db.ItemBean;
import com.mayunfei.downloadmanager.db.greendao.DaoSession;
import com.mayunfei.downloadmanager.download.http.ItemTask;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import okhttp3.OkHttpClient;

import static com.mayunfei.downloadmanager.db.DownState.STATUS_DOWNLOADING;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_ERROR;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_FINISH;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_PAUSE;

public class M3u8DownTask extends DownloadTask implements TaskStatusListener<ItemBean> {
    //当前的 任务
    private ItemTask itemTask;
    private Queue<ItemBean> itemBeanQueue;
    private Exception exception;

    public M3u8DownTask(OkHttpClient okHttpClient, BundleBean bundleBean, DaoSession daoSession, TaskStatusListener downloadTaskStatusListener) {
        super(okHttpClient, bundleBean, daoSession, downloadTaskStatusListener);
        itemBeanQueue = new ArrayDeque<>();
    }


    @Override
    public void run() {
        super.run();
        List<ItemBean> itemBeans = bundleBean.getItemBeans();
        if (itemBeans.isEmpty()){
            itemBeans.addAll( createItems());
        }

        event.setTotalSize(itemBeans.size());
        event.setCompletedSize(bundleBean.getCompletedSize());
        itemBeanQueue.clear();
        itemBeanQueue.addAll(itemBeans);




        ItemBean itemBean  = itemBeanQueue.poll();
        while (!isPause()&&(itemBean!=null) ){
            if (itemBean.getStatus() == STATUS_FINISH){ //已经完成 不用再管
                itemBean = itemBeanQueue.poll();
                continue;
            }
            itemTask = new ItemTask(getHttpClient(),itemBean,this);
            itemTask.run();
            itemBean = itemBeanQueue.poll();
        }

        checkFinish();


    }

    private List<ItemBean> createItems() {
        List<ItemBean> list = new ArrayList<>();
        bundleBean.getData();

        for (int i = 0;i<200;i++){
            ItemBean itemBean = new ItemBean();
            itemBean.setBundleId(bundleBean.getId());
            itemBean.setPath(bundleBean.getPath()+File.separator+String.format("%s.ts",i));
            itemBean.setUrl(String.format("http://172.16.200.46:8081/downloadApk/CMA/test/13/apk/%s.ts",i));
            list.add(itemBean);
        }

        itemBeanDao.insertInTx(list);
        //https://juejin.im/entry/58e5a83f2f301e00622be9ec
//        itemBeanDao.detachAll();
        bundleBean.setTotalSize(list.size());

        return list;
    }

    @Override
    public void pause() {
        itemBeanQueue.clear();
        if (itemTask!=null)
            itemTask.pause();


        super.pause();
    }

    @Override
    public void onPause(ItemBean entity) {
        entity.setStatus(STATUS_PAUSE);
        itemBeanDao.update(entity);
    }

    @Override
    public void onFinish(ItemBean entity) {
        //如果多线程需要考虑 原子操作
//         bundleBean.setCompletedSize(bundleBean.getCompletedSize()+1);
        entity.setStatus(STATUS_FINISH);
        itemBeanDao.update(entity);
        event.setCompletedSize(DbUtil.getFinishItemBean(itemBeanDao,entity.getBundleId()).size());
        doUpdate();
        checkFinish();
    }

    private void checkFinish() {
        if (itemBeanQueue.size() == 0){
            if (event.getCompletedSize()>=event.getTotalSize()) {
                doFinish();
            }else {
                if (!isPause()) {
                    doError(exception);
                }
            }
        }
    }

    @Override
    public void onError(ItemBean entity, Exception e) {
        this.exception = e;
        entity.setStatus(STATUS_ERROR);
        itemBeanDao.update(entity);
        doUpdate();
        itemBeanQueue.clear();
        checkFinish();
    }

    @Override
    public void onUpdate(ItemBean entity, long speed) {
        entity.setStatus(STATUS_DOWNLOADING);
        daoSession.startAsyncSession().update(entity);
//        itemBeanDao.update(entity);
        event.setSpeed(speed);
        doUpdate();
    }
}
