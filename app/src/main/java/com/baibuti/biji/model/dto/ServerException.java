package com.baibuti.biji.model.dto;

import lombok.Getter;

public class ServerException extends Exception {

    @Getter
    private int code;
    @Getter
    private String message;

    public ServerException(String message) {
        this(-1, message);
    }

    public ServerException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}