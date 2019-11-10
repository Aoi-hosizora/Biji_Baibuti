package com.baibuti.biji.model.dao.net;

import com.baibuti.biji.model.dto.FileUrlDTO;
import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.service.auth.AuthManager;
import com.baibuti.biji.service.retrofit.RetrofitFactory;
import com.baibuti.biji.service.retrofit.ServerErrorHandle;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class UploadFileNetDao {

    public boolean uploadImage(String path) {

        File img = new File(path);
        HashMap<String, RequestBody> requestBodyHashMap = new HashMap<>();
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), img);
        requestBodyHashMap.put("img", requestBody);

        Observable<ResponseDTO<FileUrlDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .uploadImage(requestBodyHashMap)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<FileUrlDTO> response = observable.toFuture().get();
            return response.getCode() == ServerErrorHandle.SUCCESS;
        } catch (ExecutionException | InterruptedException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean deleteImage(String filename) {

        Observable<ResponseDTO<FileUrlDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteImage(new FileUrlDTO(filename))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<FileUrlDTO> response = observable.toFuture().get();
            return response.getCode() == ServerErrorHandle.SUCCESS;
        } catch (ExecutionException | InterruptedException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
