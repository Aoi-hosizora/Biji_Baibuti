package com.baibuti.biji.model.dao.local;

import android.content.Context;

import com.baibuti.biji.model.dao.daoInterface.IScheduleDao;

public class ScheduleDao implements IScheduleDao {

    private Context context;

    public ScheduleDao(Context context) {
        this.context = context;
    }

    /**
     * 查询本地课表
     * @return Json
     */
    @Override
    public String querySchedule() {

    }

    /**
     * 新建 / 更新 本地课表文件
     * @param json Json 格式课表
     * @return 是否新建 / 更新成功
     */
    @Override
    public boolean newSchedule(String json) {

    }

    /**
     * 删除本地课表文件
     * @return 是否成功删除
     */
    @Override
    public boolean deleteSchedule() {

    }
}
