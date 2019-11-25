package com.baibuti.biji.common.download;

import android.util.Log;

import java.io.IOException;

import javax.annotation.ParametersAreNonnullByDefault;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okhttp3.internal.annotations.EverythingIsNonNull;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public class JsResponseBody extends ResponseBody {
    private ResponseBody responseBody;
    private JsDownloadListener downloadListener;

    private BufferedSource bufferedSource;

    JsResponseBody(ResponseBody responseBody, JsDownloadListener downloadListener) {
        this.responseBody = responseBody;
        this.downloadListener = downloadListener;
        downloadListener.onStartDownload(responseBody.contentLength());
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
    @EverythingIsNonNull
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;

            @Override
            @ParametersAreNonnullByDefault
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                Log.e("download", "read: " + (int) (totalBytesRead * 100 / responseBody.contentLength()));
                if (null != downloadListener) {
                    if (bytesRead != -1) {
                        downloadListener.onProgress((int) (totalBytesRead));
                    }
                }
                return bytesRead;
            }
        };
    }
}