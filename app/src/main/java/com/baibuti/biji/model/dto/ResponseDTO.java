package com.baibuti.biji.model.dto;

import lombok.Data;

/**
 * 服务器接口 标准返回格式
 * @param <T> 返回数据类型 nullable
 */
@Data
public class ResponseDTO<T> {

    private int code;
    private String message;
    private T data;

    public ResponseDTO(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
