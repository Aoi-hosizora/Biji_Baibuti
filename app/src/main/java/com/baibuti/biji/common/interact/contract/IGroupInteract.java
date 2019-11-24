package com.baibuti.biji.common.interact.contract;

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
    Observable<MessageVO<Boolean>> insertGroup(Group group);
    Observable<MessageVO<Boolean>> updateGroup(Group group);
    Observable<MessageVO<Boolean>> updateGroupsOrder(Group[] groups);
    Observable<MessageVO<Boolean>> deleteGroup(int groupId, boolean isToDefault);
}
