package com.mayunfei.downloadmanager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.mayunfei.downloadmanager.db.BundleBean;
import com.mayunfei.downloadmanager.db.greendao.DaoMaster;
import com.mayunfei.downloadmanager.db.greendao.DaoSession;
import com.mayunfei.downloadmanager.download.DownloadManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static final String LOG_TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startActivity(new Intent(this,BundleDownloadM3u8TestActivity.class));
        return;
//        boolean canWrite = isExternalStorageWritable();
//        Toast.makeText(this, " canWrite = " +canWrite , Toast.LENGTH_SHORT).show();
//
//
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
//
//        }
//
//        File test = getPrivateAlbumStorageDir(this, "test");
//        test.mkdirs();
//        if (test != null){
//            Log.e(LOG_TAG, "path = " + test.getAbsolutePath());
//        }
//
//        File jsonFile = new File(test,"test.json");
//        try {
//            FileOutputStream outputStream = new FileOutputStream(jsonFile);
//            outputStream.write("1234".getBytes());
//            outputStream.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
////        test.mkdirs();
//
//        File root = new File("/sdcard/11111111111111111111/");
//
//        boolean mkdirs = root.mkdirs();
//
//
//        if (mkdirs){
//            Log.e(LOG_TAG,"创建成果");
//        }else {
//            Log.e(LOG_TAG,"创建失败");
//        }

//        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(getApplicationContext(),"download.db");
//        SQLiteDatabase writableDatabase = devOpenHelper.getWritableDatabase();
//        DaoMaster daoMaster = new DaoMaster(writableDatabase);

//        DownloadManager downloadManager = new DownloadManager(daoMaster.newSession());
//        BundleBean bundleBean = new BundleBean();
//        bundleBean.setKey("123");
//        downloadManager.addBundle();




    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
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


    }
}
