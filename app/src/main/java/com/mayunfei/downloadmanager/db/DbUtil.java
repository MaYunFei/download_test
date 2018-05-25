package com.mayunfei.downloadmanager.db;

import com.mayunfei.downloadmanager.db.greendao.BundleBeanDao;
import com.mayunfei.downloadmanager.db.greendao.ItemBeanDao;

import java.util.List;

public class DbUtil {
    public static BundleBean getBundleByKey(BundleBeanDao dao,String key){

        return dao.queryBuilder().where(BundleBeanDao.Properties.Key.eq(key))
                .build().unique();

    }
    public static List<ItemBean> getFinishItemBean(ItemBeanDao dao, long bundleId){
        return dao.queryBuilder().where(ItemBeanDao.Properties.BundleId.eq(bundleId))
                .where(ItemBeanDao.Properties.Status.eq(DownState.STATUS_FINISH))
                .build().list();
    }
}
