package com.baibuti.biji.data.dao.daoInterface;

import com.baibuti.biji.data.po.Group;

import java.util.List;

public interface IGroupDao {

    // 查
    List<Group> queryAllGroups() throws Exception;
    Group queryGroupById(int groupId) throws Exception;
    Group queryDefaultGroup() throws Exception;

    // 增删改
    long insertGroup(Group group) throws Exception;
    boolean updateGroup(Group group) throws Exception;
    boolean deleteGroup(int groupId) throws Exception;
}
