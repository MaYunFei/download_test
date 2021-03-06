package com.mayunfei.downloadmanager.download;

import android.util.Log;

import com.mayunfei.downloadmanager.db.BundleBean;
import com.mayunfei.downloadmanager.db.greendao.BundleBeanDao;
import com.mayunfei.downloadmanager.db.greendao.DaoSession;
import com.mayunfei.downloadmanager.db.greendao.ItemBeanDao;

import java.util.Arrays;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

import okhttp3.OkHttpClient;

import static com.mayunfei.downloadmanager.db.DownState.STATUS_DOWNLOADING;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_ERROR;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_FINISH;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_PAUSE;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_WAITING;

public class DownloadTask extends Observable implements Runnable {
    protected final BundleBeanDao bundleBeanDao;
    protected final ItemBeanDao itemBeanDao;
    protected final DaoSession daoSession;
    protected BundleBean bundleBean;
    protected String key;
    protected DownEvent event;
    protected TaskStatusListener<DownloadTask> downloadStatusListener;
    private OkHttpClient httpClient;

    private volatile boolean isPause = false;

    public DownloadTask(OkHttpClient okHttpClient,BundleBean bundleBean,DaoSession daoSession, TaskStatusListener downloadTaskStatusListener) {
        this.bundleBean = bundleBean;
        this.downloadStatusListener = downloadTaskStatusListener;
        this.daoSession = daoSession;
        bundleBeanDao = daoSession.getBundleBeanDao();
        itemBeanDao = daoSession.getItemBeanDao();
        key = bundleBean.getKey();
        event = DownEvent.getEvent(bundleBean);
        this.httpClient = okHttpClient;

    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public String getKey() {
        return key;
    }


    @Override
    public void run() {
        if (isPause) {
            downloadStatusListener.onPause(this);
            return;
        }

    }



    @Override
    public synchronized void addObserver(Observer o) {
        if (o == null) return;
        super.addObserver(o);
    }

    private void updateEvent() {
        bundleBean.setCompletedSize(event.getCompletedSize());
        bundleBean.setTotalSize(event.getTotalSize());
        bundleBean.setStatus(event.getStatus());
        bundleBeanDao.update(bundleBean);
        setChanged();
        notifyObservers(event);
    }

    public void pause() {
        isPause = true;
        doPause();
    }

    public boolean isPause() {
        return isPause;
    }

    protected void doUpdate() {
        Log.w(Thread.currentThread().getName(),event.toString());
        event.setStatus(STATUS_DOWNLOADING);
        updateEvent();
    }

    protected void doError(Exception e) {
        event.setStatus(STATUS_ERROR);
        downloadStatusListener.onError(this,e);
        updateEvent();
        deleteObservers();
    }

    protected void doFinish() {
        event.setStatus(STATUS_FINISH);
        downloadStatusListener.onFinish(this);
        updateEvent();
        deleteObservers();
    }

    protected void doPause() {
        event.setStatus(STATUS_PAUSE);
        downloadStatusListener.onPause(this);
        updateEvent();
        deleteObservers();
    }


    public void waiting() {
        event.setStatus(STATUS_WAITING);
        updateEvent();
    }
}
