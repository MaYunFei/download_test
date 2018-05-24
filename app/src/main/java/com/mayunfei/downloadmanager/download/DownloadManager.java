package com.mayunfei.downloadmanager.download;

import android.util.Log;

import com.mayunfei.downloadmanager.db.BundleBean;
import com.mayunfei.downloadmanager.db.DbUtil;
import com.mayunfei.downloadmanager.db.DownType;
import com.mayunfei.downloadmanager.db.greendao.DaoSession;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mayunfei.downloadmanager.db.DownState.STATUS_FINISH;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_WAITING;

public class DownloadManager extends StatusChangeListener implements TaskStatusListener<DownloadTask> {
    static final int MAX_PART_COUNT = 3;
    private static final String TAG = "DownloadManager";
    /**
     * 线程控制器
     */
    private ExecutorService executor;
    /**
     * 数据库 session
     */
    DaoSession daoSession;
    /**
     * 下载队列
     */
    private BlockingQueue<DownloadTask> downloadQueue;  //必须是全局监听
    /**
     * 下载任务
     */
    private Map<String, DownloadTask> downloadIngTasks; // 为什么用 map


    private StatusChangeListener downloadIngListener;


    public void setDownloadIngListener(StatusChangeListener downloadIngListener) {
        this.downloadIngListener = downloadIngListener;
    }

    public DownloadManager(DaoSession daoSession) {
        this.daoSession = daoSession;
        executor = Executors.newCachedThreadPool( new ThreadFactory() {
            private AtomicInteger count = new AtomicInteger();

            @Override
            public Thread newThread(final Runnable r) {
                final int number = count.incrementAndGet();
                return new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "start ++++++++ " + number);
                        r.run();
                        Log.i(TAG, "end ++++++++ " + number);
                    }
                }, "download   " + number);
            }
        });
        downloadIngTasks = new ConcurrentHashMap<>();
        downloadQueue = new LinkedBlockingQueue<>();

    }

    public void addBundle(BundleBean bundleBean,StatusChangeListener statusChangeListener) {
        for (DownloadTask downloadTask : downloadQueue) {
            if (downloadTask.getKey().equals(bundleBean.getKey())){
                return;
            }
        }
        if (downloadIngTasks.get(bundleBean.getKey()) != null) {
            return;
        }


        BundleBean db_bundle = DbUtil.getBundleByKey(daoSession.getBundleBeanDao(), bundleBean.getKey());
        if (db_bundle != null) {
            bundleBean.setId(db_bundle.getId());
            bundleBean.__setDaoSession(daoSession);
            bundleBean.resetItemBeans();
            bundleBean.refresh(); //刷新自己

            //TODO 添加 Task 等待队列判断

            //已完成
            if (bundleBean.getStatus() == STATUS_FINISH){
                DownEvent event = DownEvent.getEvent(bundleBean);
                statusChangeListener.statusChange(event);
                return;
            }

            statusChangeListener.statusChange(DownEvent.getEvent(db_bundle));

        } else {
            //插入或更新
            bundleBean.setStatus(STATUS_WAITING);
            daoSession.getBundleBeanDao().insertInTx(bundleBean);
        }
        DownloadTask downloadTask = null;
        if (db_bundle!=null){
            downloadTask = creatDownloadTask(db_bundle);
        }else {
            downloadTask = creatDownloadTask(bundleBean);
        }
        downloadTask.addObserver(statusChangeListener);

        if (downloadIngTasks.size() >= MAX_PART_COUNT) {
            downloadQueue.add(downloadTask);
        } else {
            startDownload(downloadTask);
        }
    }

    public void addBundle(List<BundleBean> bundleBeans,StatusChangeListener statusChangeListener) {
        for (BundleBean bundleBean : bundleBeans) {
            addBundle(bundleBean,statusChangeListener);
        }
    }


    private DownloadTask creatDownloadTask(BundleBean bundle) {
        switch (bundle.getType()) {
            case DownType.TYPE_SINGLE:
                return new SingleDownTask(bundle, daoSession,this);
            case DownType.TYPE_M3U8:
                return new M3u8DownTask(bundle, daoSession,this);
        }
        return new TestDownTask(bundle, daoSession,this);
    }


    private void startDownload(DownloadTask entity) {
        downloadIngTasks.put(entity.getKey(), entity);
        //加入 正在下载 监听   不用手动删除每次都自动删除
        entity.addObserver(this);
        executor.submit(entity);
    }

    /**
     * 暂停
     * @param key
     */
    public  void pause(String key){
        for (DownloadTask task : downloadQueue) {
            if (task.getKey().equals(key)){
                task.pause();
                downloadQueue.remove(task);

                return;
            }

        }
        DownloadTask downloadTask = downloadIngTasks.get(key);
        if (downloadTask != null) {
            downloadTask.pause();
        }
    }


    public  void pause(List<String> keys) {
        for (String key : keys) {
            pause(key);
        }
    }


    /**
     * 有没有真正的 下一个
     * @return
     */
    private boolean checkNext() {
        if (downloadIngTasks.size() >=MAX_PART_COUNT){
            return false;
        }
        DownloadTask nextEntity = downloadQueue.poll();

        if (nextEntity != null) {

            startDownload(nextEntity);
            return true;
        }
        return false;
    }



    /**
     * 监听
     */
    public void addListener(String key, StatusChangeListener statusChangeListener) {

        DownloadTask downloadTask = downloadIngTasks.get(key);
        if (downloadTask != null) {
            downloadTask.addObserver(statusChangeListener);
            return;
        }

        for (DownloadTask task : downloadQueue) {
            if (task.getKey().equals(key)) {
                task.addObserver(statusChangeListener);
                return;
            }
        }
        //每次添加 监听返回最近一次状态
        BundleBean db_bundle = DbUtil.getBundleByKey(daoSession.getBundleBeanDao(), key);
        if (db_bundle != null) {
            statusChangeListener.statusChange(DownEvent.getEvent(db_bundle));
        }else {
            statusChangeListener.statusChange(DownEvent.getErrorEvent(key));
        }


    }

    public void removeListener(String key, StatusChangeListener statusChangeListener) {
        DownloadTask downloadTask = downloadIngTasks.get(key);
        if (downloadTask != null) {
            downloadTask.deleteObserver(statusChangeListener);
            return;
        }

        for (DownloadTask task : downloadQueue) {
            if (task.getKey().equals(key)) {
                task.deleteObserver(statusChangeListener);
                return;
            }
        }

        statusChangeListener.statusChange(DownEvent.getPauseEvent(key));
    }

    public void addListener(List<String> keyList, StatusChangeListener statusChangeListener) {
        for (String key : keyList) {
            addListener(key, statusChangeListener);
        }

    }

    public void removeListener(List<String> keyList, StatusChangeListener statusChangeListener) {
        for (String key : keyList) {
            removeListener(key, statusChangeListener);
        }
    }


    @Override
    public void onPause(DownloadTask entity) {
        downloadIngTasks.remove(entity.getKey());
        onNextTask(entity);
    }

    @Override
    public void onFinish(DownloadTask entity) {
        downloadIngTasks.remove(entity.getKey());
        onNextTask(entity);
    }


    @Override
    public void onError(DownloadTask entity, Exception e) {
        L.e(TAG," 任务异常 " +e.toString() + " "+entity.getKey());
        downloadIngTasks.remove(entity.getKey());
        onNextTask(entity);
    }

    @Override
    public void onUpdate(DownloadTask entity, long speed) {
        //不监听 onUpdate
    }

    private void onNextTask(DownloadTask entity) {
        if (!checkNext()&& downloadIngTasks.size() == 0) {
            L.e(TAG,"全部下载完成");
        }
    }

    @Override
    protected void statusChange(DownEvent downEvent) {
        //正在下载中的任务会回调
        if (downloadIngListener!=null){
            downloadIngListener.statusChange(downEvent);
        }
    }



}
