package com.mayunfei.downloadmanager.download.http;


/**
 * 成功回调处理
 */
public interface DownloadProgressListener {
    /**
     * 下载进度
     * @param read
     * @param count
     * @param speed
     * @param done
     */
    void update(long read, long count,long speed, boolean done);
}
