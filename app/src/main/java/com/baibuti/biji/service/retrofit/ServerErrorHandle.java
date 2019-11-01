package com.baibuti.biji.service.retrofit;

import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.model.dto.ServerException;

public class ServerErrorHandle {

    public static final int SUCCESS = 200;

    /**
     * 服务器端的错误
     */
    public static ServerException parseErrorMessage(ResponseDTO responseDTO) {
        return new ServerException(
            responseDTO.getCode(),
            responseDTO.getMessage()
        );
    }

    /**
     * 客户端获取请求的错误
     *      InterruptedException
     *      ExecutionException
     */
    public static ServerException getClientError(Throwable throwable) {
        if (throwable instanceof ServerException)
            return (ServerException) throwable;

        return new ServerException(throwable.getMessage());
    }
}
