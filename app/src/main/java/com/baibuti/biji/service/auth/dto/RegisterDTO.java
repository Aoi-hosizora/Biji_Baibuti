package com.baibuti.biji.service.auth.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class RegisterDTO implements Serializable {

    private String username;
    private String password;

    public RegisterDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
