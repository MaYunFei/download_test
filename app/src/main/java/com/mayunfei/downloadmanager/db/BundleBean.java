package com.mayunfei.downloadmanager.db;

import android.support.annotation.CheckResult;
import android.support.annotation.Nullable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Unique;

import com.mayunfei.downloadmanager.db.greendao.DaoSession;
import com.mayunfei.downloadmanager.db.greendao.ItemBeanDao;
import com.mayunfei.downloadmanager.db.greendao.BundleBeanDao;

import static com.mayunfei.downloadmanager.db.DownState.STATUS_WAITING;

@Entity()
public class BundleBean {
    @Id(autoincrement = true)
    private Long id;
    @NotNull
    @Index(unique = true)
    private String key;
    private long totalSize = -1;
    private long completedSize;
    private String path;
    private int status;
    private int type;
    private String url;
    private String data;

    public BundleBean(String key,int type) {
        this.key = key;
        this.type = type;
    }

    @ToMany(referencedJoinProperty = "bundleId")
    private List<ItemBean> itemBeans;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 77207374)
    private transient BundleBeanDao myDao;

    @Generated(hash = 537118720)
    public BundleBean(Long id, @NotNull String key, long totalSize, long completedSize, String path,
            int status, int type, String url, String data) {
        this.id = id;
        this.key = key;
        this.totalSize = totalSize;
        this.completedSize = completedSize;
        this.path = path;
        this.status = status;
        this.type = type;
        this.url = url;
        this.data = data;
    }

    @Generated(hash = 68659340)
    public BundleBean() {
    }



    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 2005068927)
    public List<ItemBean> getItemBeans() {
        if (itemBeans == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ItemBeanDao targetDao = daoSession.getItemBeanDao();
            List<ItemBean> itemBeansNew = targetDao._queryBundleBean_ItemBeans(id);
            synchronized (this) {
                if (itemBeans == null) {
                    itemBeans = itemBeansNew;
                }
            }
        }
        return itemBeans;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 804628145)
    public synchronized void resetItemBeans() {
        itemBeans = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1504652449)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getBundleBeanDao() : null;
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


