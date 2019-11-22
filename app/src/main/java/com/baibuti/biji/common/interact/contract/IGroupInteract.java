package com.baibuti.biji.common.interact.contract;

import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.Group;
import com.baibuti.biji.model.vo.MessageVO;

import java.util.List;

import io.reactivex.Observable;

public interface IGroupInteract {

    // 查
    Observable<MessageVO<List<Group>>> queryAllGroups();
    Observable<MessageVO<Group>> queryGroupById(int groupId);
    Observable<MessageVO<Group>> queryGroupByName(String name);
    Observable<MessageVO<Group>> queryDefaultGroup();

    // 增删改
    Observable<MessageVO<DbStatusType>> insertGroup(Group group);
    Observable<MessageVO<DbStatusType>> updateGroup(Group group);
    Observable<MessageVO<DbStatusType>> updateGroupsOrder(Group[] groups);
    Observable<MessageVO<DbStatusType>> deleteGroup(int groupId, boolean isToDefault);
}
