package com.baibuti.biji.model.dto;

import lombok.Data;

public class OneFieldDTO {

    @Data
    public static class CountDTO {
        private int count;
    }

    @Data
    public static class ScheduleDTO {
        private String schedule;
    }

    @Data
    public static class FilenameDTO {
        private String filename;
    }

    /**
     * /raw/image/ type (form-data-part)
     */
    public static final String RawImageType_Note = "note";
}
