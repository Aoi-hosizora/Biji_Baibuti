package com.baibuti.biji.model.dao.net;

import com.baibuti.biji.model.dto.ResponseDTO;
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
    public List<Group> queryAllGroups() throws Exception {
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
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public Group queryGroupById(int id) throws Exception {
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
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public Group queryDefaultGroup() throws Exception {
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
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public long insertGroup(Group group) throws Exception {
        Observable<ResponseDTO<GroupDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .insertGroup(GroupDTO.toGroupDTO(group))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<GroupDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return response.getData().getId();
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public boolean updateGroup(Group group) throws Exception {
        Observable<ResponseDTO<GroupDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .updateGroup(GroupDTO.toGroupDTO(group))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<GroupDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return true;
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public boolean deleteGroup(int id) throws Exception {
        Observable<ResponseDTO<GroupDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteGroup(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<GroupDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return true;
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
}
