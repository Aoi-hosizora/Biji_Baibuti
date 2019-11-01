package com.baibuti.biji.model.dao.daoInterface;

import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.Group;

import java.util.List;

public interface IGroupDao {

    // 查
    List<Group> queryAllGroups() throws ServerException;
    Group queryGroupById(int groupId) throws ServerException;
    Group queryDefaultGroup() throws ServerException;

    // 增删改
    long insertGroup(Group group) throws ServerException;
    boolean updateGroup(Group group) throws ServerException;
    boolean deleteGroup(int groupId) throws ServerException;
}
