package com.baibuti.biji.service.ocr;

import com.baibuti.biji.service.ocr.dto.OCRPoint;

import java.util.ArrayList;
import java.util.List;

public class OCRRegionUtil {

    /**
     * 判断 背景图片 是否是上下顶部碰边缘
     * @param wW WidgetSize.getWidth
     * @param wH WidgetSize.getHeight
     * @param iW ImageSize.getWidth
     * @param iH ImageSize.getHeight
     * @return boolean
     */
    private static boolean isVImg(int wW, int wH, int iW, int iH) {
        return ((double) wW) / ((double) wH) >
            ((double) iW) / ((double) iH);
    }

    /**
     * 根据 竖向横向 和 前后尺寸 转化点的坐标未布局里的坐标
     * @param point OCRPoint
     * @return OCRPoint
     */
    private static OCRPoint parsePnt(OCRPoint point, int wW, int wH, int iW, int iH) {
        boolean isVImg = isVImg(wW, wH, iW, iH);
        double x, y;
        if (isVImg) {
            double rate = ((double) wH / iH);
            double biasX = (wW - iW * rate) / 2;

            y = ((double) point.getY()) * rate;
            x = ((double) point.getX()) * rate + biasX;
        }
        else {
            double rate = ((double) wW / iW);
            double biasY = (wH - iH * rate) / 2;

            x = ((double) point.getX()) * rate;
            y = ((double) point.getY()) * rate + biasY;
        }

        return new OCRPoint((int) Math.floor(x), (int) Math.floor(y));
    }

    /**
     * 将所有的坐标均对应伸缩
     * @param points OCRPoint[]: frames.points
     * @return OCRPoint[]
     */
    public static OCRPoint[] parsePoints(OCRPoint[] points, int wW, int wH, int iW, int iH) {
        List<OCRPoint> ret = new ArrayList<>();
        for (OCRPoint pnt : points) {
            ret.add(parsePnt(pnt, wW, wH, iW, iH));
        }
        return ret.toArray(new OCRPoint[0]);
    }

}
