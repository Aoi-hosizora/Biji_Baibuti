package com.baibuti.biji.common.interact.client;

import android.content.Context;

import com.baibuti.biji.common.interact.contract.IGroupInteract;
import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dao.local.GroupDao;
import com.baibuti.biji.model.po.Group;
import com.baibuti.biji.model.vo.MessageVO;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class GroupInteract implements IGroupInteract {
    
    private Context context;

    public GroupInteract(Context context) {
        this.context = context;
    }

    @Override
    public Observable<MessageVO<List<Group>>> queryAllGroups() {
        return Observable.create(
            (ObservableEmitter<MessageVO<List<Group>>> emitter) -> {
                GroupDao groupDao = new GroupDao(context);
                emitter.onNext(new MessageVO<>(groupDao.queryAllGroups()));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Group>> queryGroupById(int groupId) {
        return Observable.create(
            (ObservableEmitter<MessageVO<Group>> emitter) -> {
                GroupDao groupDao = new GroupDao(context);
                emitter.onNext(new MessageVO<>(groupDao.queryGroupById(groupId)));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Group>> queryGroupByName(String name) {
        return Observable.create(
            (ObservableEmitter<MessageVO<Group>> emitter) -> {
                GroupDao groupDao = new GroupDao(context);
                emitter.onNext(new MessageVO<>(groupDao.queryGroupByName(name)));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Group>> queryDefaultGroup() {
        return Observable.create(
            (ObservableEmitter<MessageVO<Group>> emitter) -> {
                GroupDao groupDao = new GroupDao(context);
                emitter.onNext(new MessageVO<>(groupDao.queryDefaultGroup()));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Boolean>> insertGroup(Group group) {
        return Observable.create(
            (ObservableEmitter<MessageVO<Boolean>> emitter) -> {
                GroupDao groupDao = new GroupDao(context);
                DbStatusType status = groupDao.insertGroup(group);
                if (status == DbStatusType.DUPLICATED)
                    emitter.onNext(new MessageVO<>(false, "Group Name Duplicate"));
                else if (status == DbStatusType.FAILED)
                    emitter.onNext(new MessageVO<>(false, "Group Insert Failed"));
                else
                    emitter.onNext(new MessageVO<>(true));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Boolean>> updateGroup(Group group) {
        return Observable.create(
            (ObservableEmitter<MessageVO<Boolean>> emitter) -> {
                GroupDao groupDao = new GroupDao(context);
                DbStatusType status = groupDao.updateGroup(group);
                if (status == DbStatusType.DUPLICATED)
                    emitter.onNext(new MessageVO<>(false, "Group Name Duplicate"));
                else if (status == DbStatusType.DEFAULT)
                    emitter.onNext(new MessageVO<>(false, "Could Not Update Default Group"));
                else if (status == DbStatusType.FAILED)
                    emitter.onNext(new MessageVO<>(false, "Group Update Failed"));
                else
                    emitter.onNext(new MessageVO<>(true));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Boolean>> updateGroupsOrder(Group[] groups) {
        return Observable.create(
            (ObservableEmitter<MessageVO<Boolean>> emitter) -> {
                GroupDao groupDao = new GroupDao(context);
                DbStatusType status = groupDao.updateGroupsOrder(groups);
                if (status == DbStatusType.FAILED)
                    emitter.onNext(new MessageVO<>(false, "Group Update Failed"));
                else
                    emitter.onNext(new MessageVO<>(true));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Boolean>> deleteGroup(int groupId, boolean isToDefault) {
        return Observable.create(
            (ObservableEmitter<MessageVO<Boolean>> emitter) -> {
                GroupDao groupDao = new GroupDao(context);
                DbStatusType status = groupDao.deleteGroup(groupId, isToDefault);
                if (status == DbStatusType.FAILED)
                    emitter.onNext(new MessageVO<>(false, "Group Delete Failed"));
                else
                    emitter.onNext(new MessageVO<>(true));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
