package com.mayunfei.downloadmanager.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class ItemBean {
    @Id
    private Long id;
    private long bundleId;
    private long totalSize;
    private long completedSize;
    private String path;
    private String url;
    private int status;
    @Generated(hash = 1908751265)
    public ItemBean(Long id, long bundleId, long totalSize, long completedSize,
            String path, String url, int status) {
        this.id = id;
        this.bundleId = bundleId;
        this.totalSize = totalSize;
        this.completedSize = completedSize;
        this.path = path;
        this.url = url;
        this.status = status;
    }
    @Generated(hash = 95333960)
    public ItemBean() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public long getBundleId() {
        return this.bundleId;
    }
    public void setBundleId(long bundleId) {
        this.bundleId = bundleId;
    }
    public long getTotalSize() {
        return this.totalSize;
    }
    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }
    public long getCompletedSize() {
        return this.completedSize;
    }
    public void setCompletedSize(long completedSize) {
        this.completedSize = completedSize;
    }
    public String getPath() {
        return this.path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public int getStatus() {
        return this.status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"id\":")
                .append(id);
        sb.append(",\"bundleId\":")
                .append(bundleId);
        sb.append(",\"totalSize\":")
                .append(totalSize);
        sb.append(",\"completedSize\":")
                .append(completedSize);
        sb.append(",\"path\":\"")
                .append(path).append('\"');
        sb.append(",\"url\":\"")
                .append(url).append('\"');
        sb.append(",\"status\":")
                .append(status);
        sb.append('}');
        return sb.toString();
    }
}
