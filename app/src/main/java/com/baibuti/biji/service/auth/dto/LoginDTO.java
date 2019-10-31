package com.baibuti.biji.service.auth.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class LoginDTO implements Serializable {

    private String username;
    private String password;
    private int expiration;

    public LoginDTO(String username, String password, int expiration) {
        this.username = username;
        this.password = password;
        this.expiration = expiration;
    }
}
