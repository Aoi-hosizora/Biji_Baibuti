package com.baibuti.biji.service.retrofit;

import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.model.dto.ServerException;

public class ServerErrorHandle {

    public static final int SUCCESS = 200;
    public static final int UNAUTHORIZED = 401;
    public static final int NOT_FOUND = 404;
    public static final int INTERNAL_SERVER_ERROR = 500;

    public static final int DATABASE_FAILED = 600;
    public static final int HAS_EXISTED = 601;
    public static final int DUPLICATE_FAILED = 602;
    public static final int DEFAULT_FAILED = 603;
    public static final int SAVE_FILE_FAILED = 604;

    public static final int BAD_REQUEST = 400;
    public static final int FORBIDDEN = 403;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int NOT_ACCEPTABLE = 406;

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
