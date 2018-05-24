package com.mayunfei.downloadmanager.db;

import com.mayunfei.downloadmanager.db.greendao.BundleBeanDao;

import java.util.List;

public class DbUtil {
    public static BundleBean getBundleByKey(BundleBeanDao dao,String key){
        List<BundleBean> bundleBeans = dao.queryRaw("WHERE key = ?", key);
        if (!bundleBeans.isEmpty())
            return bundleBeans.get(0);
        return null;
    }
}
