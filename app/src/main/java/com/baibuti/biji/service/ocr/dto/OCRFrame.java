package com.baibuti.biji.service.ocr.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * OCR 文字块，四个点定义
 */
@Data
public class OCRFrame implements Serializable {

    private OCRPoint[] points;
    private double scores;
    private String ocr;

    public OCRFrame(OCRPoint[] points, double scores, String ocr) {
        if (points.length > 4)
            this.points = new OCRPoint[] {points[0], points[1], points[2], points[3]};
        else if (points.length < 4)
            this.points = new OCRPoint[] {OCRPoint.zeroPoint, OCRPoint.zeroPoint, OCRPoint.zeroPoint, OCRPoint.zeroPoint};
        else
            this.points = points;

        this.scores = scores;
        this.ocr = ocr;
    }

    public OCRFrame(int X1, int Y1, int X2, int Y2, int X3, int Y3, int X4, int Y4, double scores, String ocr) {
        this(
            new OCRPoint[] {
                new OCRPoint(X1, Y1), new OCRPoint(X2, Y2), new OCRPoint(X3, Y3), new OCRPoint(X4, Y4)
            },
            scores, ocr
        );
    }

    /**
     * 从 区域集合 获得 OCR 结果
     * @param frames 多个 Frame
     * @return 所有识别结果
     */
    public static String getStrFromFrames(OCRFrame[] frames) {
        StringBuilder sb = new StringBuilder();
        for (OCRFrame frame : frames)
            sb.append(frame.getOcr()).append("\n");
        return sb.toString();
    }
}
