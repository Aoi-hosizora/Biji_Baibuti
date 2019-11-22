package com.baibuti.biji.common.interact.contract;

import com.baibuti.biji.model.vo.MessageVO;

import io.reactivex.Observable;

public interface IScheduleInteract {

    Observable<MessageVO<String>> querySchedule();
    Observable<MessageVO<Boolean>> updateSchedule(String schedule);
    Observable<MessageVO<Boolean>> deleteSchedule();
}
