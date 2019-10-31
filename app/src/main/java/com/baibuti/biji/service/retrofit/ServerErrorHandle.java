package com.baibuti.biji.service.retrofit;

import com.baibuti.biji.model.dto.ResponseDTO;

public class ServerErrorHandle {

    public static final int SUCCESS = 200;

    public static Exception parseErrorMessage(ResponseDTO responseDTO) {
        return new Exception(responseDTO.getMessage());
    }
}
