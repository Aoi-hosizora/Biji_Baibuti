package com.baibuti.biji.common.interact.server;

import com.baibuti.biji.common.interact.contract.IDocumentInteract;
import com.baibuti.biji.model.dto.DocumentDTO;
import com.baibuti.biji.model.po.Document;
import com.baibuti.biji.common.auth.AuthManager;
import com.baibuti.biji.common.retrofit.RetrofitFactory;
import com.baibuti.biji.model.vo.MessageVO;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class DocumentNetInteract implements IDocumentInteract {

    @Override
    public Observable<MessageVO<List<Document>>> queryAllDocuments() {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getAllDocuments()
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<List<Document>>(false, responseDTO.getMessage());
                List<Document> fromDocuments = new ArrayList<>();
                Collections.addAll(fromDocuments, DocumentDTO.toDocuments(responseDTO.getData()));
                return new MessageVO<>(fromDocuments);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<List<Document>>> queryDocumentByClassId(int cid) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getDocumentByClassId(cid)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<List<Document>>(false, responseDTO.getMessage());
                List<Document> fromDocuments = new ArrayList<>();
                Collections.addAll(fromDocuments, DocumentDTO.toDocuments(responseDTO.getData()));
                return new MessageVO<>(fromDocuments);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Document>> queryDocumentById(int id) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getDocumentById(id)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Document>(false, responseDTO.getMessage());
                return new MessageVO<>(responseDTO.getData().toDocument());
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @return SUCCESS | FAILED | UPLOAD_FAILED
     */
    @Override
    public Observable<MessageVO<Boolean>> insertDocument(Document document) {
        File file = new File(document.getFilename());
        RequestBody requestFile = RequestBody.create(MediaType.parse(""), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .insertDocument(body, document.getDocClass().getId())
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Boolean>(false, responseDTO.getMessage());
                document.setId(responseDTO.getData().getId());
                return new MessageVO<>(true);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @return SUCCESS | FAILED
     */
    @Override
    public Observable<MessageVO<Boolean>> updateDocument(Document document) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .updateDocument(document.getId(), document.getFilename(), document.getDocClass().getId())
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Boolean>(false, responseDTO.getMessage());
                return new MessageVO<>(true);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @return SUCCESS | FAILED
     */
    @Override
    public Observable<MessageVO<Boolean>> deleteDocument(int id) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteDocument(id)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Boolean>(false, responseDTO.getMessage());
                return new MessageVO<>(true);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
