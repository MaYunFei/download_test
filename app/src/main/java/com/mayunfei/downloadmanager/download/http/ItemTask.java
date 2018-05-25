package com.mayunfei.downloadmanager.download.http;

import android.util.Log;

import com.mayunfei.downloadmanager.db.DownState;
import com.mayunfei.downloadmanager.db.ItemBean;
import com.mayunfei.downloadmanager.download.ITask;
import com.mayunfei.downloadmanager.download.M3u8DownTask;
import com.mayunfei.downloadmanager.download.TaskStatusListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import static com.mayunfei.downloadmanager.db.DownState.STATUS_PAUSE;

public class ItemTask implements Runnable, DownloadProgressListener, ITask {


    private final OkHttpClient httpClient;
    private ItemBean itemBean;
    private Call requestCall;
    private volatile boolean  isCancel = false;
    private TaskStatusListener<ItemBean> listener;
    private static final int SPEED_CHECK = 200*1024;

    public ItemTask(OkHttpClient httpClient, ItemBean itemBean, TaskStatusListener<ItemBean> taskTaskStatusListener) {
        this.itemBean = itemBean;
        this.listener = taskTaskStatusListener;
        this.httpClient = httpClient;
    }


    @Override
    public void run() {
        start();
    }

    @Override
    public void start() {
//        OkHttpClient.Builder builder = new OkHttpClient.Builder();
//        builder.connectTimeout(60, TimeUnit.SECONDS);
//        builder.addInterceptor(new DownloadInterceptor(this));
//
//        OkHttpClient httpClient = builder.build();
        itemBean.setStatus(DownState.STATUS_DOWNLOADING);
        listener.onUpdate(itemBean,0);


        String url = itemBean.getUrl();
        Request request = new Request.Builder().url(url).addHeader("RANGE", "bytes=" + itemBean.getCompletedSize() + "-").build();
        if (isCancel) {
            listener.onPause(itemBean);
            return;
        }
        requestCall = httpClient.newCall(request);
        try {
            Response response = requestCall.execute();
            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                writeCaches(body, new File(itemBean.getPath()), itemBean);
            }
//            int i =0;
//            itemBean.setTotalSize(1);
//            while (i<=1 &&!isCancel){
//                Thread.sleep(250);
//                i++;
//                itemBean.setCompletedSize(i);
//                listener.onUpdate(itemBean,0);
//            }
//            if (isCancel){
//                listener.onPause(itemBean);
//            }else {
//                listener.onFinish(itemBean);
//            }

        } catch (Exception e) {
            if (!isCancel)
                listener.onError(itemBean, e);
        }

    }

    /**
     * 写入文件
     *
     * @param file
     * @param info
     * @throws IOException
     */
    public void writeCaches(ResponseBody responseBody, File file, ItemBean info) throws IOException {
        try {
            RandomAccessFile randomAccessFile = null;
            FileChannel channelOut = null;
            InputStream inputStream = null;
            try {
                if (!file.getParentFile().exists())
                    file.getParentFile().mkdirs();
                //断点续传
                long contentLength = responseBody.contentLength();

                long allLength = 0 == info.getTotalSize() ? responseBody.contentLength() : info.getCompletedSize() + contentLength;
                //大小
                itemBean.setTotalSize(allLength);

                inputStream = responseBody.byteStream();
                randomAccessFile = new RandomAccessFile(file, "rwd");
                channelOut = randomAccessFile.getChannel();
                MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE,
                        info.getCompletedSize(), allLength - info.getCompletedSize());
                byte[] buffer = new byte[1024 * 4];
                int len;
                int speedLength = 0;
                long startTime = System.currentTimeMillis();
                while ((len = inputStream.read(buffer)) != -1 && !isCancel) {
                    mappedBuffer.put(buffer, 0, len);

                    //累计下载长度
                    speedLength+=len;

                    itemBean.setCompletedSize(itemBean.getTotalSize() - contentLength + len);
                    itemBean.setStatus(DownState.STATUS_DOWNLOADING);
                    long endTIme = System.currentTimeMillis();
                    if (speedLength>=SPEED_CHECK||(endTIme-startTime)>1500) {
                        if (endTIme>startTime) {
                            Log.i("速度","speedLength/(endTIme-startTime) "+speedLength+"/"+"("+endTIme+"-"+startTime+")" + " "+ speedLength/(endTIme-startTime) );
                            listener.onUpdate(itemBean,speedLength/(endTIme-startTime));
                            speedLength = 0;
                            startTime = endTIme;
                        }
                    }
                }

                if (speedLength<SPEED_CHECK){
                    long endTIme = System.currentTimeMillis();
                    if (endTIme>startTime) {
                        listener.onUpdate(itemBean,speedLength/(endTIme-startTime));
//                        speedLength = 0;
//                        startTime = endTIme;
                    }
                }

                if (!isCancel){
                    itemBean.setStatus(DownState.STATUS_FINISH);
                    listener.onFinish(itemBean);
                }else {

                }

            } catch (IOException e) {
                throw e;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (channelOut != null) {
                    channelOut.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            }
        } catch (IOException e) {
            throw e;
        }
    }

//    public void writeCaches(ResponseBody responseBody, File file, ItemBean info) throws IOException {
//        try {
//
//            BufferedSink sink = null;
//            BufferedSource source = null;
//            RandomAccessFile mAccessFile = null;
//            try {
//                if (!file.getParentFile().exists())
//                    file.getParentFile().mkdirs();
//
//                if (!file.exists()){
//                    if (file.createNewFile())
//                        file.setWritable(true);
//                }
//                //断点续传
//                long allLength = 0 == info.getTotalSize() ? responseBody.contentLength() : info.getCompletedSize() + responseBody
//                        .contentLength();
//                //大小
//                itemBean.setTotalSize(allLength);
//
//                mAccessFile = new RandomAccessFile(file, "rw");
//                mAccessFile.seek(info.getCompletedSize());
//                sink = Okio.buffer(Okio.sink(new FileOutputStream(mAccessFile.getFD())));
//                source = responseBody.source();
//                sink.writeAll(source);
//
//            } catch (IOException e) {
//                throw e;
//            } finally {
//                if (sink != null) {
//                    sink.close();
//                }
//                if (source != null) {
//                    source.close();
//                }
//                if (mAccessFile != null) {
//                    mAccessFile.close();
//                }
//            }
//        } catch (IOException e) {
//            throw e;
//        }
//    }


    @Override
    public void update(long read, long count, long speed, boolean done) {
        itemBean.setCompletedSize(itemBean.getTotalSize() - count + read);
        itemBean.setStatus(done ? DownState.STATUS_FINISH : DownState.STATUS_DOWNLOADING);

        if (done) { //完成通知
            listener.onFinish(itemBean);
        } else {
            listener.onUpdate(itemBean, speed);
        }
    }

    @Override
    public void pause() {
        isCancel = true;
        if (requestCall != null) {
            requestCall.cancel();
        }

        itemBean.setStatus(STATUS_PAUSE);
        listener.onPause(itemBean);
    }


}
