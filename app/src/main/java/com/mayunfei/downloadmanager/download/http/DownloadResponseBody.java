package com.mayunfei.downloadmanager.download.http;

import android.util.Log;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * 自定义精度的body
 */
public class DownloadResponseBody extends ResponseBody {

    private ResponseBody responseBody;
    private DownloadProgressListener progressListener;
    private BufferedSource bufferedSource;
    private static final int UPDATE_TIME = 1000;

    public DownloadResponseBody(ResponseBody responseBody, DownloadProgressListener progressListener) {
        this.responseBody = responseBody;
        this.progressListener = progressListener;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;
            long tmpTime = System.currentTimeMillis();
            long speed = 0;
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long startTime = System.currentTimeMillis();
                long bytesRead = super.read(sink, byteCount);
                long endTime = System.currentTimeMillis();

                if (endTime - startTime > 0 && bytesRead > 0) {
                    speed = bytesRead / (endTime - startTime);
                }
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                if ((endTime - tmpTime > UPDATE_TIME || bytesRead == -1) && null != progressListener) {
                    tmpTime = endTime;
                    progressListener.update(totalBytesRead, responseBody.contentLength(), speed, bytesRead == -1);
                }
                return bytesRead;
            }
        };

    }
}
