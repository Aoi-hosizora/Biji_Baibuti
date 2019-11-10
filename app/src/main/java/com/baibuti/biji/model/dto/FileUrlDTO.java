package com.baibuti.biji.model.dto;

import lombok.Data;

@Data
public class FileUrlDTO {

    public FileUrlDTO(String filename) {
        this.filename = filename;
    }

    private String filename;
}
