package com.baibuti.biji.data.dao.daoInterface;

import com.baibuti.biji.data.model.Group;

import java.util.List;

public interface IGroupDao {

    // 查
    List<Group> queryAllGroups();
    Group queryGroupById(int groupId);
    Group queryDefaultGroup();

    // 增删改
    long insertGroup(Group group);
    boolean updateGroup(Group group);
    boolean deleteGroup(int groupId);
}
