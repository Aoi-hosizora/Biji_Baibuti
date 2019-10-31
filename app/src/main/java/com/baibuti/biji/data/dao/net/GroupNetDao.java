package com.baibuti.biji.data.dao.net;

import com.baibuti.biji.data.dao.RetrofitFactory;
import com.baibuti.biji.data.dao.daoInterface.IGroupDao;
import com.baibuti.biji.data.dto.GroupDTO;
import com.baibuti.biji.data.model.Group;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class GroupNetDao implements IGroupDao {

    @Override
    public List<Group> queryAllGroups() {
        Observable<Group[]> observable = RetrofitFactory.getInstance()
            .createRequest(RetrofitFactory.getHeader("", ""))
            .getAllGroups()
            .subscribeOn(Schedulers.io())
            .map(GroupDTO::toGroups)
            .observeOn(AndroidSchedulers.mainThread());

        try {
            return Arrays.asList(observable.toFuture().get());
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public Group queryGroupById(int groupId) {
        Observable<Group> observable = RetrofitFactory.getInstance()
            .createRequest(RetrofitFactory.getHeader("", ""))
            .getGroupById(groupId)
            .subscribeOn(Schedulers.io())
            .map(GroupDTO::toGroup)
            .observeOn(AndroidSchedulers.mainThread());

        try {
            return observable.toFuture().get();
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public Group queryDefaultGroup() {
        Observable<Group[]> observable = RetrofitFactory.getInstance()
            .createRequest(RetrofitFactory.getHeader("", ""))
            .getAllGroups()
            .subscribeOn(Schedulers.io())
            .map(GroupDTO::toGroups)
            .observeOn(AndroidSchedulers.mainThread());

        try {
            Group[] groups = observable.toFuture().get();
            for (Group group : groups) {
                if (group.getName().equals(Group.DEF_GROUP.getName()))
                    return group;
            }
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public long insertGroup(Group group) {
        Observable<Group> observable = RetrofitFactory.getInstance()
            .createRequest(RetrofitFactory.getHeader("", ""))
            .insertGroup(GroupDTO.toGroupDTO(group))
            .subscribeOn(Schedulers.io())
            .map(GroupDTO::toGroup)
            .observeOn(AndroidSchedulers.mainThread());

        try {
            Group new_group = observable.toFuture().get();
            return new_group.getId();
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean updateGroup(Group group) {
        Observable<Group> observable = RetrofitFactory.getInstance()
            .createRequest(RetrofitFactory.getHeader("", ""))
            .updateGroup(GroupDTO.toGroupDTO(group))
            .subscribeOn(Schedulers.io())
            .map(GroupDTO::toGroup)
            .observeOn(AndroidSchedulers.mainThread());

        try {
            observable.toFuture().get();
            return true;
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteGroup(int id) {
        Observable<Group> observable = RetrofitFactory.getInstance()
            .createRequest(RetrofitFactory.getHeader("", ""))
            .deleteGroup(id)
            .subscribeOn(Schedulers.io())
            .map(GroupDTO::toGroup)
            .observeOn(AndroidSchedulers.mainThread());

        try {
            observable.toFuture().get();
            return true;
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
