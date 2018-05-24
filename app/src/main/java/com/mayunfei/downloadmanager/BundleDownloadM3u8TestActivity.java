package com.mayunfei.downloadmanager;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.mayunfei.downloadmanager.db.BundleBean;
import com.mayunfei.downloadmanager.db.DownType;
import com.mayunfei.downloadmanager.db.greendao.DaoMaster;
import com.mayunfei.downloadmanager.download.DownEvent;
import com.mayunfei.downloadmanager.download.DownloadManager;
import com.mayunfei.downloadmanager.download.StatusChangeListener;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mayunfei.downloadmanager.ItemDownloadTestActivity.getPrivateAlbumStorageDir;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_DELETE;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_DOWNLOADING;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_ERROR;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_FINISH;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_PAUSE;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_UNSTART;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_WAITING;

public class BundleDownloadM3u8TestActivity extends AppCompatActivity {

    private static final String TAG = "BundleTestctivity";
    private static final int UPDATE_ADPATER = 1;
    private static final int UPDATE_ALL_OP = 2;
    private DownloadManager downloadManager;
    private List<BundleBean> bundleBeanList;
    private List<DownEvent> list;
    private List<String> keys;
//    private String[] keys = {
//            "12340",
//            "12341",
//            "12342",
//            "12343",
//            "12344",
//            "12345",
//            "12346",
//            "12347",
//            "12348",
//            "12349",
//
//    };

    private List<DownEvent> eventList;

    private StatusChangeListener downloadListener = new StatusChangeListener() {
        private long startTime = System.currentTimeMillis();

        @Override
        protected void statusChange(DownEvent downEvent) {
            Log.i(TAG, downEvent.toString());


            for (int i = 0; i < bundleBeanList.size(); i++) {
                if (bundleBeanList.get(i).getKey().equals(downEvent.getKey())) {
                    eventList.get(i).refresh(downEvent);
//                    final int finalI = i;
                    handler.removeMessages(UPDATE_ADPATER);
                    handler.sendEmptyMessage(UPDATE_ADPATER);

                    break;
                }
            }
            //数据修改，定时更新

        }
    };

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case UPDATE_ADPATER:
                    adapter.notifyDataSetChanged();
                    break;
                case UPDATE_ALL_OP:
                    if (msg.arg1 == 0) {
                        showAllPause();
                    }else {
                        showAllStart();
                    }
                    break;
            }
        }
    };

    private StatusChangeListener downLoading = new StatusChangeListener() {
        @Override
        protected void statusChange(final DownEvent downEvent) {
            if (downEvent.getStatus() == STATUS_DOWNLOADING) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvKey.setText(downEvent.getKey());
                        mTvSpeed.setText(downEvent.getCompletedSize() + "");
                    }
                });
            }

            Log.e("正在下载的监听  " ,downEvent.toString());
            if (downEvent.getStatus() != STATUS_DOWNLOADING && downEvent.getStatus() != STATUS_WAITING ) { //排除 正在队列 或者正在下载的情况
                Log.e("判断 全部 下载还是 别的 " ,downEvent.toString());
                for (DownEvent event : eventList) {
                    if (event.getKey().equals(downEvent.getKey())) {
                        event.refresh(downEvent);// 由于 并不知道 观察这那个先通知所以需要更新
                        break;
                    }
                }
                getAllStatus();

            }

        }
    };


    @Override
    protected void onPause() {
        super.onPause();
        //停止监听
        downloadManager.removeListener(keys, downloadListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 开启监听
        downloadManager.addListener(keys, downloadListener);
    }

    private TextView mTvKey;
    private TextView mTvSpeed;
    private Button mBtnStartAll;
    private Button mBtnPauseAll;
    private RecyclerView mRecyclerView;
    private HomeAdapter adapter;




    private void getAllStatus() {
        for (DownEvent downEvent : eventList) {
            if (downEvent.getStatus() == STATUS_DOWNLOADING || downEvent.getStatus() == STATUS_WAITING) {
                Log.e(TAG,downEvent.toString());
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        showAllPause();
//                    }
//                });
                Message obtain = Message.obtain();
                obtain.what = UPDATE_ALL_OP;
                obtain.arg1 = 0;
                handler.removeMessages(UPDATE_ALL_OP);
                handler.sendMessage(obtain);

                return;
            }

        }
        Message obtain = Message.obtain();
        obtain.what = UPDATE_ALL_OP;
        obtain.arg1 = 1;
        handler.removeMessages(UPDATE_ALL_OP);
        handler.sendMessageDelayed(obtain,500);

    }

    private void showAllStart() {
        mBtnStartAll.setVisibility(View.VISIBLE);
        mBtnPauseAll.setVisibility(View.GONE);
    }

    private void showAllPause() {
        mBtnStartAll.setVisibility(View.GONE);
        mBtnPauseAll.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bundle_testctivity);
        initView();
        eventList = new ArrayList<>();
        bundleBeanList = new ArrayList<>();
        keys = new ArrayList<>();
        QueryBuilder.LOG_SQL = true;
        QueryBuilder.LOG_VALUES = true;
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(getApplicationContext(), "download.db");
        SQLiteDatabase writableDatabase = devOpenHelper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(writableDatabase);

        downloadManager = new DownloadManager(daoMaster.newSession());
        downloadManager.setDownloadIngListener(downLoading);


        adapter = new HomeAdapter(R.layout.item_view, bundleBeanList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(adapter);
        test();
        for (int i = 0; i < bundleBeanList.size(); i++) {
            eventList.add(i, new DownEvent(bundleBeanList.get(i).getKey()));
        }

        mBtnPauseAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadManager.pause(keys);
            }

        });

        mBtnStartAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadManager.addBundle(bundleBeanList, downloadListener);
            }
        });

    }

    public void test() {
        for (int i = 0; i < 30; i++) {
            BundleBean bundleBean = new BundleBean("1234" + i, DownType.TYPE_M3U8);
            keys.add("1234"+i);
            bundleBean.setPath(getPrivateAlbumStorageDir(this).getAbsolutePath() + File.separator + i);
            bundleBean.setUrl(String.format("http://172.16.200.46:8081/downloadApk/CMA/test/13/apk/%s_test.apk",i));
            downloadManager.addBundle(bundleBean, downloadListener);
            bundleBeanList.add(bundleBean);
        }


    }

    private void initView() {
        mTvKey = findViewById(R.id.tv_key);
        mTvSpeed = findViewById(R.id.tv_speed);
        mBtnStartAll = findViewById(R.id.btn_start_all);
        mBtnPauseAll = findViewById(R.id.btn_pause_all);
        mRecyclerView = findViewById(R.id.recycler_view);
    }


    public class HomeAdapter extends BaseQuickAdapter<BundleBean, BaseViewHolder> {
        public HomeAdapter(int layoutResId, List data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, final BundleBean item) {
            // 加载网络图片
            Button btn_start = helper.getView(R.id.btn_start);
            Button btn_pause = helper.getView(R.id.btn_pause);
            btn_start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadManager.addBundle(item, downloadListener);
                    downloadManager.addListener(item.getKey(), downloadListener);
                    showAllPause();
                }
            });

            btn_pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadManager.pause(item.getKey());
                }
            });
            helper.setText(R.id.tv_key, item.getKey());
            if (eventList.get(getData().indexOf(item)) != null) {
                helper.setText(R.id.tv_complete, eventList.get(getData().indexOf(item)).getCompletedSize() + "");
            } else {

            }

            StringBuffer sb = new StringBuffer();
            switch (eventList.get(getData().indexOf(item)).getStatus()) {
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
            helper.setText(R.id.tv_status,sb.toString());
        }
    }


}
