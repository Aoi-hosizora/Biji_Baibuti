package com.baibuti.biji.common.interact;

import android.app.ProgressDialog;
import android.content.Context;

import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.model.vo.MessageVO;
import com.google.gson.Gson;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class ProgressHandler {

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

        boolean[] cancel = new boolean[] { false };

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(cancelable);

        Disposable disposable = observable.subscribe(
            (MessageVO<T> messageVO) -> {
                if (cancel[0]) return;
                progressDialog.dismiss();

                // 应该都是 SUCCESS
                if (messageVO.isSuccess())
                    handler.onSuccess(messageVO.getData());
                else
                    handler.onError(MessageErrorParser.fromMessageVO(messageVO));
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

                    handler.onError(MessageErrorParser.fromMessageVO(new MessageVO(false, resp.getMessage())));
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
