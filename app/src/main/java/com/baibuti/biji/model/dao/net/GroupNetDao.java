package com.baibuti.biji.model.dao.net;

import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.service.auth.AuthManager;
import com.baibuti.biji.service.retrofit.RetrofitFactory;
import com.baibuti.biji.model.dao.daoInterface.IGroupDao;
import com.baibuti.biji.model.dto.GroupDTO;
import com.baibuti.biji.model.po.Group;
import com.baibuti.biji.service.retrofit.ServerErrorHandle;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class GroupNetDao implements IGroupDao {

    @Override
    public List<Group> queryAllGroups() throws ServerException {
        Observable<ResponseDTO<GroupDTO[]>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getAllGroups()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<GroupDTO[]> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return Arrays.asList(GroupDTO.toGroups(response.getData()));
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    @Override
    public Group queryGroupById(int id) throws ServerException {
        Observable<ResponseDTO<GroupDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getGroupById(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<GroupDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return response.getData().toGroup();
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    @Override
    public Group queryGroupByName(String name) throws ServerException {
        Observable<ResponseDTO<GroupDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getGroupByName(name)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<GroupDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return response.getData().toGroup();
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    @Override
    public Group queryDefaultGroup() throws ServerException {
        Observable<ResponseDTO<GroupDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getDefaultGroup()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<GroupDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return response.getData().toGroup();
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    /**
     * @return SUCCESS | FAILED | DUPLICATED
     */
    @Override
    public DbStatusType insertGroup(Group group) throws ServerException {
        Observable<ResponseDTO<GroupDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .insertGroup(group.getName(), group.getColor())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<GroupDTO> response = observable.toFuture().get();
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
    public DbStatusType updateGroup(Group group) throws ServerException {
        Observable<ResponseDTO<GroupDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .updateGroup(group.getId(), group.getName(), group.getOrder(), group.getColor())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<GroupDTO> response = observable.toFuture().get();
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
     * @return SUCCESS | FAILED  | DEFAULT
     */
    @Override
    public DbStatusType deleteGroup(int id, boolean isToDefault) throws ServerException {
        Observable<ResponseDTO<GroupDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteGroup(id, isToDefault)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<GroupDTO> response = observable.toFuture().get();
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
        } catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }
}
