package com.baibuti.biji.model.dao.daoInterface;

public interface IScheduleDao {

    String querySchedule();
    boolean newSchedule(String schedule);
    boolean deleteSchedule();
}
