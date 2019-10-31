package com.baibuti.biji.service.ocr.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * 包裹所有 OCRFrame (OCR 块)
 */
@Data
public class OCRRegion implements Serializable {

    private OCRPoint size;
    private int cnt;
    private OCRFrame[] frames;

    public OCRRegion(OCRPoint size, int cnt, OCRFrame[] frames) {
        this.size = size;
        this.cnt = cnt;
        this.frames = frames;
    }
}
