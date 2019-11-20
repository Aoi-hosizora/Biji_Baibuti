package com.baibuti.biji.model.dao.net;

import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dao.daoInterface.IDocClassDao;
import com.baibuti.biji.model.dto.DocClassDTO;
import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.DocClass;
import com.baibuti.biji.common.auth.AuthManager;
import com.baibuti.biji.common.retrofit.RetrofitFactory;
import com.baibuti.biji.common.retrofit.ServerErrorHandle;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DocClassNetDao implements IDocClassDao {

    @Override
    public List<DocClass> queryAllDocClasses() throws ServerException {
        Observable<ResponseDTO<DocClassDTO[]>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getAllDocClasses()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<DocClassDTO[]> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return Arrays.asList(DocClassDTO.toFileClasses(response.getData()));
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    @Override
    public DocClass queryDocClassById(int id) throws ServerException {
        Observable<ResponseDTO<DocClassDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getDocClassById(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<DocClassDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return response.getData().toFileClass();
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    @Override
    public DocClass queryDocClassByName(String name) throws ServerException {
        Observable<ResponseDTO<DocClassDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getDocClassByName(name)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<DocClassDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return response.getData().toFileClass();
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    @Override
    public DocClass queryDefaultDocClass() throws ServerException {
        Observable<ResponseDTO<DocClassDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getDefaultDocClass()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<DocClassDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return response.getData().toFileClass();
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    /**
     * @param docClass SUCCESS | FAILED | DUPLICATED
     */
    @Override
    public DbStatusType insertDocClass(DocClass docClass) throws ServerException {
        Observable<ResponseDTO<DocClassDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .insertDocClass(docClass.getName())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<DocClassDTO> response = observable.toFuture().get();
            switch (response.getCode()) {
                case ServerErrorHandle.SUCCESS:
                    return DbStatusType.SUCCESS;
                case ServerErrorHandle.HAS_EXISTED:
                case ServerErrorHandle.DATABASE_FAILED:
                    return DbStatusType.FAILED;
                case ServerErrorHandle.DUPLICATE_FAILED:
                    return DbStatusType.DUPLICATED;
                default:
                    throw ServerErrorHandle.parseErrorMessage(response);
            }
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    /**
     * @return SUCCESS | FAILED | DUPLICATED | DEFAULT
     */
    @Override
    public DbStatusType updateDocClass(DocClass docClass) throws ServerException {
        Observable<ResponseDTO<DocClassDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .updateDocClass(docClass.getId(), docClass.getName())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<DocClassDTO> response = observable.toFuture().get();
            switch (response.getCode()) {
                case ServerErrorHandle.SUCCESS:
                    return DbStatusType.SUCCESS;
                case ServerErrorHandle.NOT_FOUND:
                case ServerErrorHandle.DATABASE_FAILED:
                    return DbStatusType.FAILED;
                case ServerErrorHandle.DUPLICATE_FAILED:
                    return DbStatusType.DUPLICATED;
                case ServerErrorHandle.DEFAULT_FAILED:
                    return DbStatusType.DEFAULT;
                default:
                    throw ServerErrorHandle.parseErrorMessage(response);
            }
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    /**
     * @return SUCCESS | FAILED | DEFAULT
     */
    @Override
    public DbStatusType deleteDocClass(int id, boolean isToDefault) throws ServerException {
        Observable<ResponseDTO<DocClassDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteDocClass(id, isToDefault)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<DocClassDTO> response = observable.toFuture().get();
            switch (response.getCode()) {
                case ServerErrorHandle.SUCCESS:
                    return DbStatusType.SUCCESS;
                case ServerErrorHandle.NOT_FOUND:
                case ServerErrorHandle.DATABASE_FAILED:
                    return DbStatusType.FAILED;
                case ServerErrorHandle.DEFAULT_FAILED:
                    return DbStatusType.DEFAULT;
                default:
                    throw ServerErrorHandle.parseErrorMessage(response);
            }
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }
}

/*

    public static File getShareCode(String fileClassName){

        return NetHelper.httpGetFileSync(
                GetShareCodeUrl + "?docClass=" + fileClassName,
                "Share",
                fileClassName + ".png",
                NetHelper.getOneHeader("Authorization", AuthManager.getInstance().getToken())
        );
    }

 */