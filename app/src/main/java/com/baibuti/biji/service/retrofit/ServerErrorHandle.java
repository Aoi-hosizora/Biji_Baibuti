package com.baibuti.biji.service.retrofit;

import com.baibuti.biji.data.dto.ResponseDTO;

public class ServerErrorHandle {

    public static Exception parseErrorMessage(ResponseDTO responseDTO) {
        return new Exception(responseDTO.getMessage());
    }
}
