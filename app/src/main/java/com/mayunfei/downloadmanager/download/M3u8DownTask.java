package com.mayunfei.downloadmanager.download;

import com.mayunfei.downloadmanager.db.BundleBean;
import com.mayunfei.downloadmanager.db.ItemBean;
import com.mayunfei.downloadmanager.db.greendao.DaoSession;
import com.mayunfei.downloadmanager.download.http.ItemTask;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.mayunfei.downloadmanager.db.DownState.STATUS_FINISH;

public class M3u8DownTask extends DownloadTask implements TaskStatusListener<ItemBean> {
    //当前的 任务
    private ItemTask itemTask;
    private Queue<ItemBean> itemBeanQueue;
    private Exception exception;

    public M3u8DownTask(BundleBean bundleBean, DaoSession daoSession, TaskStatusListener downloadTaskStatusListener) {
        super(bundleBean, daoSession, downloadTaskStatusListener);
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
        itemBeanQueue.addAll(itemBeans);




        ItemBean itemBean  = itemBeanQueue.poll();
        while (!isPause()&&(itemBean!=null) ){
            if (itemBean.getStatus() == STATUS_FINISH){ //已经完成 不用再管
                itemBean = itemBeanQueue.poll();
                continue;
            }
            itemTask = new ItemTask(itemBean,this);
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
        itemBeanDao.detachAll();
        bundleBean.setTotalSize(list.size());

        return list;
    }

    @Override
    public void pause() {
        super.pause();
        itemBeanQueue.clear();
        if (itemTask!=null)
        itemTask.pause();
    }

    @Override
    public void onPause(ItemBean entity) {
        itemBeanDao.update(entity);
    }

    @Override
    public void onFinish(ItemBean entity) {
        //如果多线程需要考虑 原子操作
//         bundleBean.setCompletedSize(bundleBean.getCompletedSize()+1);
        event.setCompletedSize(event.getCompletedSize()+1);
        itemBeanDao.update(entity);
        doUpdate();
        checkFinish();
    }

    private void checkFinish() {
        if (itemBeanQueue.size() == 0){
            itemBeanDao.detachAll();
            if (event.getCompletedSize()>=event.getTotalSize()) {
                doFinish();
            }else {
                doError(exception);
            }
        }
    }

    @Override
    public void onError(ItemBean entity, Exception e) {
        this.exception = e;
        itemBeanDao.update(entity);
        doUpdate();
        itemBeanQueue.clear();
        checkFinish();
    }

    @Override
    public void onUpdate(ItemBean entity, long speed) {
        itemBeanDao.update(entity);
        event.setSpeed(speed);
        doUpdate();
    }
}
