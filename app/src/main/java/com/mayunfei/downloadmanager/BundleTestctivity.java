package com.mayunfei.downloadmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mayunfei.downloadmanager.db.DownState.STATUS_DELETE;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_DOWNLOADING;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_ERROR;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_FINISH;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_PAUSE;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_UNSTART;
import static com.mayunfei.downloadmanager.db.DownState.STATUS_WAITING;

public class BundleTestctivity extends AppCompatActivity {

    private static final String TAG = "BundleTestctivity";
    private DownloadManager downloadManager;
    private List<BundleBean> bundleBeanList;
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

    List<String> keys;

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
//                            adapter.notifyItemChanged(finalI);
                            adapter.notifyDataSetChanged();
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
            Log.i("+++",downEvent.toString());
            if (downEvent.getStatus() != STATUS_DOWNLOADING || downEvent.getStatus() != STATUS_WAITING ) { //排除 正在队列 或者正在下载的情况
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getAllStatus();
                    }
                });
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
                Log.e("+++",downEvent.toString());
                showAllPause();
                return;
            }

        }
        showAllStart();
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
        for (int i = 0; i < 50; i++) {
            BundleBean bundleBean = new BundleBean("1234" + i, 3);
            keys.add("1234" + i);
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
