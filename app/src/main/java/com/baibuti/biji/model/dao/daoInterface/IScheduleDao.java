package com.baibuti.biji.model.dao.daoInterface;

import com.baibuti.biji.model.dto.ServerException;

public interface IScheduleDao {

    String querySchedule() throws ServerException;
    boolean newSchedule(String schedule) throws ServerException;
    boolean deleteSchedule() throws ServerException;
}
