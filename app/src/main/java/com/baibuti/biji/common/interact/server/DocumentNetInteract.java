package com.baibuti.biji.common.interact.server;

import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.common.interact.contract.IDocumentInteract;
import com.baibuti.biji.model.dto.DocumentDTO;
import com.baibuti.biji.model.po.Document;
import com.baibuti.biji.common.auth.AuthManager;
import com.baibuti.biji.common.retrofit.RetrofitFactory;
import com.baibuti.biji.model.vo.MessageVO;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DocumentNetInteract implements IDocumentInteract {

    @Override
    public Observable<MessageVO<List<Document>>> queryAllDocuments() {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getAllDocuments()
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<List<Document>>(false, responseDTO.getMessage());
                return new MessageVO<>(Arrays.asList(DocumentDTO.toDocuments(responseDTO.getData())));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<List<Document>>> queryDocumentByClassId(int cid) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getAllDocuments()
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<List<Document>>(false, responseDTO.getMessage());
                return new MessageVO<>(Arrays.asList(DocumentDTO.toDocuments(responseDTO.getData())));
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
    public Observable<MessageVO<DbStatusType>> insertDocument(Document document) {
        // TODO
        File file = new File(document.getFilename());
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .insertDocument(file, document.getDocClass().getId())
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<DbStatusType>(false, responseDTO.getMessage());
                return new MessageVO<>(DbStatusType.SUCCESS);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @return SUCCESS | FAILED
     */
    @Override
    public Observable<MessageVO<DbStatusType>> updateDocument(Document document) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .updateDocument(document.getId(), document.getFilename(), document.getDocClass().getId())
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<DbStatusType>(false, responseDTO.getMessage());
                return new MessageVO<>(DbStatusType.SUCCESS);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @return SUCCESS | FAILED
     */
    @Override
    public Observable<MessageVO<DbStatusType>> deleteDocument(int id) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteDocument(id)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<DbStatusType>(false, responseDTO.getMessage());
                return new MessageVO<>(DbStatusType.SUCCESS);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
