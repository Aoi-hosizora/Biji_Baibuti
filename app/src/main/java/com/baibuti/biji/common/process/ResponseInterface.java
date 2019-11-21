package com.baibuti.biji.common.process;

public interface ResponseInterface<T> {

    /**
     * 成功返回数据
     */
    void onSuccess(T data);

    /**
     * 返回错误信息
     */
    void onError(String message);

    /**
     * 终端环境错误
     */
    void onFailed(Throwable throwable);
}
