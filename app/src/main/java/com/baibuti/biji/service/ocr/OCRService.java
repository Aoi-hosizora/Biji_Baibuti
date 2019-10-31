package com.baibuti.biji.service.ocr;

import com.baibuti.biji.service.ocr.dto.OCRRegion;
import com.baibuti.biji.service.Urls;
import com.baibuti.biji.service.retrofit.RetrofitFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;

public class OCRService {

    interface OCRApi {

        @Multipart
        @POST("/ocr/upload")
        Observable<OCRRegion> upload(@PartMap Map<String, RequestBody> requestBodyMap);
    }

    /**
     * 获得 OCR 结果
     * @param imgPath String
     * @return Region
     */
    public static OCRRegion getOCRRet(String imgPath) {

        File img = new File(imgPath);
        HashMap<String, RequestBody> requestBodyHashMap = new HashMap<>();
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), img);
        requestBodyHashMap.put("img", requestBody);

        Observable<OCRRegion> observable = RetrofitFactory.getInstance()
            .createRequest(RetrofitFactory.getHeader(), Urls.OCRServerEndPoint, OCRApi.class)
            .upload(requestBodyHashMap)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            return observable.toFuture().get();
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
