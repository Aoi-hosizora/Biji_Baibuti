package com.baibuti.biji.common.interact;

public interface InteractInterface<T> {

    /**
     * 成功处理数据
     */
    void onSuccess(T data);

    /**
     * 返回错误信息，操作错误
     */
    void onError(String message);

    /**
     * 终端环境错误，网络错误
     */
    void onFailed(Throwable throwable);
}
