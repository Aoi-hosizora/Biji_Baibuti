package com.baibuti.biji.common.process;

import android.app.ProgressDialog;
import android.content.Context;

import com.baibuti.biji.common.retrofit.ServerErrorHandle;
import com.baibuti.biji.model.dto.ResponseDTO;
import com.google.gson.Gson;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class ProgressHandler {

    /**
     * 对于 Observable 显示加载框
     */
    public static <T> void process(
        Context context, String message, boolean cancelable,
        Observable<ResponseDTO<T>> observable, ResponseInterface<T> handler
    ) {

        boolean[] cancel = new boolean[] { false };

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(cancelable);

        Disposable disposable = observable.subscribe(
            (ResponseDTO<T> response) -> {
                if (cancel[0]) return;
                progressDialog.dismiss();

                // 应该都是 SUCCESS
                if (response.getCode() != ServerErrorHandle.SUCCESS)
                    handler.onError(ServerErrorHandle.fromResponseDTO(response));
                else
                    handler.onSuccess(response.getData());
            },
            (throwable) -> {
                if (cancel[0]) return;
                progressDialog.dismiss();
                // retrofit2.adapter.rxjava2.HttpException: HTTP 601 UNKNOWN

                // 请求码的错误
                if (throwable instanceof HttpException) {
                    ResponseBody responseBody = ((HttpException) throwable).response().errorBody();

                    Gson gson = new Gson();
                    String res = responseBody.string();
                    ResponseDTO resp = gson.fromJson(res, ResponseDTO.class);

                    handler.onError(ServerErrorHandle.fromResponseDTO(resp));
                } else {
                    // 网络的错误
                    throwable.printStackTrace();
                    handler.onFailed(throwable);
                }
            });

        progressDialog.setOnCancelListener((v) -> {
            cancel[0] = true;
            disposable.dispose();
        });
        progressDialog.show();
    }
}
