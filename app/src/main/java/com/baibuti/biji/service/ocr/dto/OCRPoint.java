package com.baibuti.biji.service.ocr.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * 构成 OCRFrame 的一个点
 */
@Data
public class OCRPoint implements Serializable {

    private int x;
    private int y;

    public static OCRPoint zeroPoint = new OCRPoint(0, 0);

    public OCRPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
