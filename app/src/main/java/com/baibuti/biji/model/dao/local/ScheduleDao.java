package com.baibuti.biji.model.dao.local;

import android.content.Context;

import com.baibuti.biji.model.dao.daoInterface.IScheduleDao;
import com.baibuti.biji.util.fileUtil.SaveNameUtil;
import com.baibuti.biji.util.imgDocUtil.TextUtil;

import java.io.File;
import java.io.IOException;

public class ScheduleDao implements IScheduleDao {

    public ScheduleDao() { }

    /**
     * 查询本地课表
     * @return Json, empty for error
     */
    @Override
    public String querySchedule() {
        String filename = SaveNameUtil.getScheduleFileName(SaveNameUtil.LOCAL);
        String content = TextUtil.readFromFile(filename);
        return content == null ? "" : content;
    }

    /**
     * 新建 / 更新 本地课表文件
     * @param json Json 格式课表
     * @return 是否新建 / 更新成功
     */
    @Override
    public boolean newSchedule(String json) {
        String filename = SaveNameUtil.getScheduleFileName(SaveNameUtil.LOCAL);
        return TextUtil.writeIntoFile(filename, json);
    }

    /**
     * 删除本地课表文件
     * @return 是否成功删除
     */
    @Override
    public boolean deleteSchedule() {
        String filename = SaveNameUtil.getScheduleFileName(SaveNameUtil.LOCAL);
        try {
            File file = new File(filename);
            if (file.exists() && !file.delete())
                throw new IOException();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
