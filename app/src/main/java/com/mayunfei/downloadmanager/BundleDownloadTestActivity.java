package com.mayunfei.downloadmanager;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mayunfei.downloadmanager.ItemDownloadTestActivity.getPrivateAlbumStorageDir;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_DOWNLOADING;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_WAITING;

public class BundleDownloadTestActivity extends AppCompatActivity {

    private static final String TAG = "BundleTestctivity";
    private DownloadManager downloadManager;
    private List<BundleBean> bundleBeanList;
    private List<DownEvent> list;
    private String[] keys = {
            "12340",
            "12341",
            "12342",
            "12343",
            "12344",
            "12345",
            "12346",
            "12347",
            "12348",
            "12349",

    };

    private List<DownEvent> eventList;

    private StatusChangeListener downloadListener = new StatusChangeListener() {
        @Override
        protected void statusChange(DownEvent downEvent) {
            Log.i(TAG, downEvent.toString());


            for (int i = 0; i < bundleBeanList.size(); i++) {
                if (bundleBeanList.get(i).getKey().equals(downEvent.getKey())) {
                    eventList.get(i).refresh(downEvent);
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyItemChanged(finalI);
                        }
                    });
                    break;
                }
            }
            //数据修改，定时更新

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

            Log.e(TAG,"正在下载的监听  " + downEvent.toString());
            if (downEvent.getStatus() != STATUS_DOWNLOADING && downEvent.getStatus() != STATUS_WAITING ) { //排除 正在队列 或者正在下载的情况
                Log.e(TAG,"判断 全部 下载还是 别的 " + downEvent.toString());
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
        downloadManager.removeListener(Arrays.asList(keys), downloadListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 开启监听
        downloadManager.addListener(Arrays.asList(keys), downloadListener);
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
                Log.e("+++",downEvent.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAllPause();
                    }
                });

                return;
            }

        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showAllStart();
            }
        });

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
                downloadManager.pause(Arrays.asList(keys));
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
        for (int i = 0; i < 10; i++) {
            BundleBean bundleBean = new BundleBean("1234" + i, DownType.TYPE_SINGLE);
            bundleBean.setPath(getPrivateAlbumStorageDir(this,String.format("%s.apk",i)).getAbsolutePath());
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
        }
    }


}
