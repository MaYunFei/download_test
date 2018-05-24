package com.mayunfei.downloadmanager.download;

import com.mayunfei.downloadmanager.db.BundleBean;

import static com.mayunfei.downloadmanager.db.DownState.STATUS_DELETE;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_DOWNLOADING;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_ERROR;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_FINISH;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_PAUSE;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_UNSTART;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_WAITING;

public class DownEvent {
    private String key;
    private long totalSize = -1;
    private long completedSize = 0;
    private int status = STATUS_UNSTART;
    private double speed = 0;//速度

    public DownEvent(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getCompletedSize() {
        return completedSize;
    }

    public void setCompletedSize(long completedSize) {
        this.completedSize = completedSize;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public static DownEvent getPauseEvent(String key) {
        return getEvent(key, STATUS_PAUSE);
    }

    public static DownEvent getErrorEvent(String key) {
        return getEvent(key, STATUS_ERROR);
    }

    public static DownEvent getEvent(String key, int status) {
        DownEvent downEvent = new DownEvent(key);
        downEvent.setStatus(status);
        return downEvent;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"key\":\"")
                .append(key).append('\"');
        sb.append(",\"totalSize\":")
                .append(totalSize);
        sb.append(",\"completedSize\":")
                .append(completedSize);
        sb.append(",\"status\":");
        switch (status) {
            case STATUS_ERROR:
                sb.append("错误");
                break;
            case STATUS_PAUSE:
                sb.append("暂停");
                break;
            case STATUS_UNSTART:
                sb.append("还未开始");
                break;
            case STATUS_DELETE:
                sb.append("删除");
                break;
            case STATUS_DOWNLOADING:
                sb.append("正在下载");
                break;
            case STATUS_FINISH:
                sb.append("完成");
                break;
            case STATUS_WAITING:
                sb.append("等待");
                break;
            default:
                sb.append("啥都没传");
                break;

        }

        sb.append(",\"speed\":")
                .append(speed);
        sb.append('}');
        return sb.toString();
    }

    public void refresh(DownEvent downEvent) {
        this.setStatus(downEvent.getStatus());
        this.setCompletedSize(downEvent.getCompletedSize());
        this.setTotalSize(downEvent.getTotalSize());
        this.setSpeed(downEvent.getSpeed());
    }

    public static DownEvent getEvent(BundleBean db_bundle) {
        DownEvent downEvent = new DownEvent(db_bundle.getKey());

        downEvent.setTotalSize(db_bundle.getTotalSize());
        downEvent.setCompletedSize(db_bundle.getCompletedSize());
        downEvent.setStatus(db_bundle.getStatus());
        return downEvent;
    }
}
