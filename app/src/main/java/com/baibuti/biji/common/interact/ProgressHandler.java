package com.baibuti.biji.common.interact;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.baibuti.biji.common.download.DownloadHandler;
import com.baibuti.biji.common.download.JsDownloadListener;
import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.model.vo.MessageVO;
import com.google.gson.Gson;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class ProgressHandler {

    private static final int MIN_PROGRESS_TIME = 50; // 50ms

    /**
     * 对于 Observable 显示加载框
     */
    public static <T> void process(Observable<MessageVO<T>> observable, InteractInterface<T> handler) {
        process(null, "", false, observable, handler);
    }

    /**
     * 对于 Observable 显示加载框
     */
    public static <T> void process(
        Context context, String message, boolean cancelable,
        Observable<MessageVO<T>> observable, InteractInterface<T> handler
    ) {

        boolean[] cancel = new boolean[]{false};
        ProgressDialog[] progressDialog = new ProgressDialog[1];
        if (context != null && !message.isEmpty()) { // << 可选
            progressDialog[0] = new ProgressDialog(context);
            progressDialog[0].setMessage(message);
            progressDialog[0].setCancelable(cancelable);
        }

        Disposable disposable = observable.subscribe(
            (MessageVO<T> messageVO) -> {
                new Handler().postDelayed(() -> {
                    if (progressDialog[0] != null) // <<
                        progressDialog[0].dismiss();
                }, MIN_PROGRESS_TIME);
                if (cancel[0]) return;

                // 应该都是 SUCCESS
                if (messageVO.isSuccess())
                    handler.onSuccess(messageVO.getData());
                else
                    handler.onError(MessageErrorParser.fromMessageVO(messageVO));
            },
            (throwable) -> {
                new Handler().postDelayed(() -> {
                    if (progressDialog[0] != null) // <<
                        progressDialog[0].dismiss();
                }, MIN_PROGRESS_TIME);
                if (cancel[0]) return;
                // retrofit2.adapter.rxjava2.HttpException: HTTP 601 UNKNOWN

                // 请求码的错误
                if (throwable instanceof HttpException) {
                    ResponseBody responseBody = ((HttpException) throwable).response().errorBody();

                    Gson gson = new Gson();
                    String res = responseBody == null ? "Null Response Error" : responseBody.string();
                    ResponseDTO resp = gson.fromJson(res, ResponseDTO.class);

                    handler.onError(MessageErrorParser.fromMessageVO(new MessageVO(false, resp.getMessage())));
                } else {
                    // 网络的错误
                    throwable.printStackTrace();
                    handler.onFailed(throwable);
                }
            }, () -> {
                if (progressDialog[0] != null) // <<
                    progressDialog[0].dismiss();
            });

        if (progressDialog[0] != null) { // <<
            progressDialog[0].setOnCancelListener((v) -> {
                cancel[0] = true;
                disposable.dispose();
            });
            progressDialog[0].show();
        }
    }

    public interface OnDownloadListener {
        void onFailed(String message);

        void onComplete();
    }

    /**
     * 下载文件
     * @param file 本地文件名
     * @param url 下载链接
     * @param hasToken 是否需要认证
     */
    public static void download(
        Context context, String message,
        File file, String url, boolean hasToken, OnDownloadListener listener
    ) {
        ProgressDialog[] progressDialog = new ProgressDialog[]{new ProgressDialog(context)};
        progressDialog[0].setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog[0].setMessage(message);
        progressDialog[0].setProgress(0);

        DownloadHandler downloadHandler = new DownloadHandler(new JsDownloadListener() {
            @Override
            public void onStartDownload(long length) {
                progressDialog[0].setMax((int) length);
            }

            @Override
            public void onProgress(int progress) {
                progressDialog[0].setProgress(progress);
            }

            @Override
            public void onFail(String errorInfo) {
                progressDialog[0].dismiss();
                Log.e("", "onFail: " + errorInfo);
                listener.onFailed(errorInfo);
            }
        });

        Disposable disposable = downloadHandler.download(url, file, hasToken)
            .subscribe(inputStream -> {

            }, throwable -> {
                progressDialog[0].dismiss();
                throwable.printStackTrace();
                listener.onFailed(throwable.getMessage());
            }, listener::onComplete);


        progressDialog[0].setCancelable(true);
        progressDialog[0].setOnCancelListener((v) -> disposable.dispose());
        progressDialog[0].show();
    }
}
