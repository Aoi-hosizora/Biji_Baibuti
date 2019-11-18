package com.baibuti.biji.model.dao.daoInterface;

import com.baibuti.biji.model.dto.ServerException;

public interface IScheduleDao {

    String querySchedule() throws ServerException;
    boolean updateSchedule(String schedule) throws ServerException;
    boolean deleteSchedule() throws ServerException;
}
