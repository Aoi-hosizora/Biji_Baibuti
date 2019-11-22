package com.baibuti.biji.common.interact.server;

import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.common.interact.contract.IDocClassInteract;
import com.baibuti.biji.model.dto.DocClassDTO;
import com.baibuti.biji.model.po.DocClass;
import com.baibuti.biji.common.auth.AuthManager;
import com.baibuti.biji.common.retrofit.RetrofitFactory;
import com.baibuti.biji.model.vo.MessageVO;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DocClassNetInteract implements IDocClassInteract {

    @Override
    public Observable<MessageVO<List<DocClass>>> queryAllDocClasses() {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getAllDocClasses()
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<List<DocClass>>(false, responseDTO.getMessage());
                return new MessageVO<>(Arrays.asList(DocClassDTO.toDocClasses(responseDTO.getData())));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<DocClass>> queryDocClassById(int id) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getDocClassById(id)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<DocClass>(false, responseDTO.getMessage());
                return new MessageVO<>(responseDTO.getData().toDocClass());
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<DocClass>> queryDocClassByName(String name) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getDocClassByName(name)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<DocClass>(false, responseDTO.getMessage());
                return new MessageVO<>(responseDTO.getData().toDocClass());
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<DocClass>> queryDefaultDocClass() {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getDefaultDocClass()
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<DocClass>(false, responseDTO.getMessage());
                return new MessageVO<>(responseDTO.getData().toDocClass());
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @param docClass SUCCESS | FAILED | DUPLICATED
     */
    @Override
    public Observable<MessageVO<Boolean>> insertDocClass(DocClass docClass) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .insertDocClass(docClass.getName())
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Boolean>(false, responseDTO.getMessage());
                return new MessageVO<>(true);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @return SUCCESS | FAILED | DUPLICATED | DEFAULT
     */
    @Override
    public Observable<MessageVO<Boolean>> updateDocClass(DocClass docClass) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .updateDocClass(docClass.getId(), docClass.getName())
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Boolean>(false, responseDTO.getMessage());
                return new MessageVO<>(true);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @return SUCCESS | FAILED | DEFAULT
     */
    @Override
    public Observable<MessageVO<Boolean>> deleteDocClass(int id, boolean isToDefault) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteDocClass(id, isToDefault)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Boolean>(false, responseDTO.getMessage());
                return new MessageVO<>(true);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
