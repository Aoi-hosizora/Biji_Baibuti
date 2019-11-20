package com.baibuti.biji.model.dao.daoInterface;

import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.Group;

import java.util.List;

public interface IGroupDao {

    // 查
    List<Group> queryAllGroups() throws ServerException;
    Group queryGroupById(int groupId) throws ServerException;
    Group queryGroupByName(String name) throws ServerException;
    Group queryDefaultGroup() throws ServerException;

    // 增删改
    DbStatusType insertGroup(Group group) throws ServerException;
    DbStatusType updateGroup(Group group) throws ServerException;
    DbStatusType updateGroupsOrder(Group[] groups) throws ServerException;
    DbStatusType deleteGroup(int groupId, boolean isToDefault) throws ServerException;
}
