package com.baibuti.biji.service.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
public class AuthRespDTO {

    private String username;
    private String status;

    @Setter
    private String token;
}
