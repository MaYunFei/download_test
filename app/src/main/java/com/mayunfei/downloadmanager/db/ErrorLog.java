package com.mayunfei.downloadmanager.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class ErrorLog {
    @Id
    private Long id;
    @NotNull
    @Index(unique = true)
    private String error;
    private String url;
    private String data;
    @Generated(hash = 444472930)
    public ErrorLog(Long id, @NotNull String error, String url, String data) {
        this.id = id;
        this.error = error;
        this.url = url;
        this.data = data;
    }
    @Generated(hash = 1694956548)
    public ErrorLog() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getError() {
        return this.error;
    }
    public void setError(String error) {
        this.error = error;
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getData() {
        return this.data;
    }
    public void setData(String data) {
        this.data = data;
    }
}
