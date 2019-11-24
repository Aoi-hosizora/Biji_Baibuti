package com.baibuti.biji.common.auth.dto;

import lombok.Data;

@Data
public class AuthRespDTO {

    private int id;
    private String username;
    private String token;
}
