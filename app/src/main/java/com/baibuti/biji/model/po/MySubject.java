package com.baibuti.biji.model.po;

import com.alibaba.fastjson.annotation.JSONField;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.model.ScheduleEnable;

import java.util.List;

import lombok.Data;

/**
 * 自定义实体类需要实现ScheduleEnable接口并实现getSchedule()
 *
 * @see ScheduleEnable#getSchedule()
 */
@Data
public class MySubject implements ScheduleEnable {

    private static final String EXTRAS_ID = "extras_id";

    @JSONField(name = "id")
    private int id = 0;

    // 信息

    @JSONField(name = "docName")
    private String name;

    @JSONField(name = "time")
    private String time;

    @JSONField(name = "room")
    private String room;

    @JSONField(name = "teacher")
    private String teacher;

    // 时间

    @JSONField(name = "start")
    private int start;

    @JSONField(name = "step")
    private int step;

    // 日期

    @JSONField(name = "day")
    private int day;

    /**
     * 上课周次
     */
    @JSONField(name = "weekList")
    private List<Integer> weekList;

    // 其他

    @JSONField(serialize = false, deserialize = false)
    private String term;

    @JSONField(serialize = false, deserialize = false)
    private int colorRandom;

    @JSONField(serialize = false, deserialize = false)
    private String url;

    public MySubject() { }

    @Override
    @JSONField(serialize = false, deserialize = false)
    public Schedule getSchedule() {
        Schedule schedule = new Schedule();
        schedule.setDay(this.day);
        schedule.setName(this.name);
        schedule.setRoom(this.room);
        schedule.setStart(this.start);
        schedule.setStep(this.step);
        schedule.setTeacher(this.teacher);
        schedule.setWeekList(this.weekList);
        schedule.setColorRandom(2);
        schedule.putExtras(EXTRAS_ID, getId());
        return schedule;
    }
}