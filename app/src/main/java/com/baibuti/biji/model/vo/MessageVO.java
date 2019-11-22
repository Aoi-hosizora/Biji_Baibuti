package com.baibuti.biji.model.vo;

import com.baibuti.biji.model.dto.ResponseDTO;

import lombok.Data;

@Data
public class MessageVO<T> {

    private boolean isSuccess;
    private String message;
    private T data;

    public MessageVO(boolean isSuccess, String message, T data) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.data = data;
    }

    public MessageVO(T data) {
        this(true, "Success", data);
    }

    public MessageVO(boolean isSuccess, String message) {
        this(isSuccess, message, null);
    }

    // public static <T> MessageVO<T> fromResponseDTO(ResponseDTO<T> responseDTO) {
    //     if (responseDTO.getCode() != 200)
    //         return new MessageVO<>(false, responseDTO.getMessage());
    //     else
    //         return new MessageVO<>(responseDTO.getData());
    // }
}
