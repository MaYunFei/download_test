package com.mayunfei.downloadmanager.download.http;

import com.mayunfei.downloadmanager.db.ItemBean;
import com.mayunfei.downloadmanager.db.greendao.ItemBeanDao;
import com.mayunfei.downloadmanager.download.TaskStatusListener;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ItemTaskTest {



    @Test
    public void testDownload() throws Exception{
        ItemBeanDao itemBeanDao = Mockito.mock(ItemBeanDao.class);
        
        final ItemBean itemBean = new ItemBean();

        itemBean.setUrl("http://172.16.200.46:8081/downloadApk/CMA/test/13/apk/13_test.apk");
        itemBean.setPath("/Users/yunfei/Documents/code/local_code/DownloadManager/13_test.apk");
        ItemTask itemTask = new ItemTask(itemBean, new TaskStatusListener<ItemBean>() {
            @Override
            public void onPause(ItemBean entity) {

            }

            @Override
            public void onFinish(ItemBean entity) {

            }

            @Override
            public void onError(ItemBean entity, Exception e) {

            }

            @Override
            public void onUpdate(ItemBean entity, long speed) {

            }
        });
        itemTask.run();
    }

}