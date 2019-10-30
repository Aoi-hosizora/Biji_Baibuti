package com.baibuti.biji.data.dao.net;

import com.baibuti.biji.data.dao.daoInterface.IGroupDao;
import com.baibuti.biji.data.model.Group;

import java.util.List;

public class GroupNetDao implements IGroupDao {

    @Override
    public List<Group> queryGroupAll() {
        return null;
    }

    @Override
    public Group queryGroupById(int groupId) {
        return null;
    }

    @Override
    public Group queryDefaultGroup() {
        return null;
    }

    @Override
    public long insertGroup(Group group) {
        return 0;
    }

    @Override
    public boolean updateGroup(Group group) {
        return false;
    }

    @Override
    public boolean deleteGroup(int groupId) {
        return false;
    }
}
