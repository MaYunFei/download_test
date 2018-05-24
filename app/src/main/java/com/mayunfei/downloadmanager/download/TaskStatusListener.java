package com.mayunfei.downloadmanager.download;

public interface TaskStatusListener<T> {

    void onPause(T entity);

    void onFinish(T entity);

    void onError(T entity,Exception e);

    void onUpdate(T entity,long speed);

}
