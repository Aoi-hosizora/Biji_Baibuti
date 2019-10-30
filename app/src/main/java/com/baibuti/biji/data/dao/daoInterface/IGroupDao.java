package com.baibuti.biji.data.dao.daoInterface;

import com.baibuti.biji.data.model.Group;

import java.util.List;

public interface IGroupDao {

    // 查
    List<Group> queryGroupAll() throws Exception;
    Group queryGroupById(int groupId);
    Group queryDefaultGroup();

    // 增删改
    long insertGroup(Group group) throws Exception;
    boolean updateGroup(Group group) throws Exception;
    boolean deleteGroup(int groupId) throws Exception;
}
