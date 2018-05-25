package com.mayunfei.downloadmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.mayunfei.downloadmanager.db.BundleBean;
import com.mayunfei.downloadmanager.db.DbUtil;
import com.mayunfei.downloadmanager.db.DownType;
import com.mayunfei.downloadmanager.db.ItemBean;
import com.mayunfei.downloadmanager.db.greendao.DaoMaster;
import com.mayunfei.downloadmanager.db.greendao.DaoSession;
import com.mayunfei.downloadmanager.download.TaskStatusListener;
import com.mayunfei.downloadmanager.download.http.ItemTask;

import java.io.File;

public class ItemDownloadTestActivity extends AppCompatActivity {

    private DaoSession daoSession;
    private Button mBtnStart;
    private Button mBtnStop;
    private BundleBean bundleBean;
    private ItemBean itemBean;
    private ItemTask itemTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_download_test);




        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(getApplicationContext(), "download.db");
        SQLiteDatabase writableDatabase = devOpenHelper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(writableDatabase);
//        BundleBean bundleBean = new BundleBean("123");
        daoSession = daoMaster.newSession();
        bundleBean = DbUtil.getBundleByKey(daoSession.getBundleBeanDao(), "123");
        if (bundleBean == null){
            bundleBean = new BundleBean("123", DownType.TYPE_SINGLE);
            daoSession.getBundleBeanDao().insertInTx(bundleBean);
            itemBean = new ItemBean();
            itemBean.setBundleId(bundleBean.getId());
            itemBean.setUrl("http://172.16.200.46:8081/downloadApk/CMA/test/13/apk/13_test.apk");
            itemBean.setPath(getPrivateAlbumStorageDir(this,"13_test.apk").getAbsolutePath());
            daoSession.insert(itemBean);
        }else {
            itemBean = bundleBean.getItemBeans().get(0);
        }


        initView();
    }

    private void initView() {
        mBtnStart = findViewById(R.id.btn_start);
        mBtnStop = findViewById(R.id.btn_stop);
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemTask = new ItemTask(null,itemBean, new TaskStatusListener<ItemBean>() {
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
                new Thread(itemTask).start();
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemTask.pause();
            }
        });
    }

    public static File getPrivateAlbumStorageDir(Context context, String albumName) {
        // Get the directory for the app's private pictures directory.
        File[] externalFilesDirs = ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_DOWNLOADS);
        if (externalFilesDirs.length>0){
            File filesDir = externalFilesDirs[0];
            File file = new File(filesDir, albumName);
            return file;
        }else {
            return null;
        }


    }public static File getPrivateAlbumStorageDir(Context context) {
        // Get the directory for the app's private pictures directory.
        File[] externalFilesDirs = ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_DOWNLOADS);
        if (externalFilesDirs.length>0){
            File filesDir = externalFilesDirs[0];
            return filesDir;
        }else {
            return null;
        }


    }
}
