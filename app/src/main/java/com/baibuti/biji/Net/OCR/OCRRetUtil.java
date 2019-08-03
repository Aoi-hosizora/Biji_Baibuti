package com.baibuti.biji.Net.OCR;

import android.util.Log;

import com.baibuti.biji.Net.Models.Region;
import com.baibuti.biji.Net.NetUtil;
import com.baibuti.biji.Net.Urls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class OCRRetUtil {

    /**
     * 获得 OCR 结果
     * @param imgPath String
     * @return
     */
    public static Region getOCRRet(String imgPath) {
        File img = new File(imgPath);
        String resp = NetUtil.httpPostSync(Urls.OCRServerUrl, "img", img);
        Log.e("OCRRetUtil", "getOCRRet: " + resp.length());

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(resp);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
        }

        Region ret = null;

        if (jsonObject != null)
            try {
                ret = parseRegionJson(jsonObject);
            }
            catch (JSONException ex) {
                    ex.printStackTrace();
            }

        return ret;
    }

    /**
     * -> Region
     * @param json
     * @return
     * @throws JSONException
     */
    private static Region parseRegionJson(JSONObject json) throws JSONException {

        JSONObject sizeObj = json.getJSONObject("size");
        JSONArray framesArrayObj = json.getJSONArray("frames");

        Region.Point size = parsePointJson(sizeObj); // <<<
        int cnt = json.getInt("cnt"); // <<<

        Region.Frame[] frames = parseFramesJson(framesArrayObj); // <<<

        return new Region(size, cnt, frames);
    }

    /**
     * -> Point{}
     * @param json
     * @return
     * @throws JSONException
     */
    private static Region.Point parsePointJson(JSONObject json) throws JSONException {
        int sizeX = json.getInt("x");
        int sizeY = json.getInt("y");
        return new Region.Point(sizeX, sizeY);
    }

    /**
     * -> Point[]
     * @param json
     * @return
     * @throws JSONException
     */
    private static Region.Point[] parsePointsJson(JSONArray json) throws JSONException {
        Region.Point[] points = new Region.Point[json.length()];
        for (int i = 0; i < json.length(); i++) {
            JSONObject pointObj = json.getJSONObject(i);
            points[i] = parsePointJson(pointObj);
        }
        return points;
    }

    /**
     * -> Frame{}
     * @param json
     * @return
     * @throws JSONException
     */
    private static Region.Frame parseFrameJson(JSONObject json) throws JSONException {
        JSONArray pointsArrayObj = json.getJSONArray("points");

        Region.Point[] points = parsePointsJson(pointsArrayObj);
        double score = json.getDouble("score");
        String ocr = json.getString("ocr");

        return new Region.Frame(points, score, ocr);
    }

    /**
     * -> Frame[]
     * @param json
     * @return
     * @throws JSONException
     */
    private static Region.Frame[] parseFramesJson(JSONArray json) throws JSONException {
        Region.Frame[] frames = new Region.Frame[json.length()];
        for (int i = 0; i < json.length(); i++) {
            JSONObject frameObj = json.getJSONObject(i);
            frames[i] = parseFrameJson(frameObj);
        }
        return frames;
    }
}
