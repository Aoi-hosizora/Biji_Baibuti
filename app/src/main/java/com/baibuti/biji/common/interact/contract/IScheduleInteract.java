package com.baibuti.biji.common.interact.contract;

import android.util.Pair;

import com.baibuti.biji.model.vo.MessageVO;

import io.reactivex.Observable;

public interface IScheduleInteract {

    Observable<MessageVO<Pair<String, Integer>>> querySchedule();
    Observable<MessageVO<Boolean>> updateSchedule(String schedule, int currWeek);
    Observable<MessageVO<Boolean>> deleteSchedule();
}
