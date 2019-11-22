package com.baibuti.biji.common.interact.client;

import android.content.Context;
import android.util.Pair;

import com.baibuti.biji.common.interact.contract.IScheduleInteract;
import com.baibuti.biji.model.dao.local.ScheduleDao;
import com.baibuti.biji.model.vo.MessageVO;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ScheduleInteract implements IScheduleInteract {

    private Context context;

    public ScheduleInteract(Context context) {
        this.context = context;
    }

    @Override
    public Observable<MessageVO<Pair<String, Integer>>> querySchedule() {
        return Observable.create(
            (ObservableEmitter<MessageVO<Pair<String, Integer>>> emitter) -> {
                ScheduleDao scheduleDao = new ScheduleDao(context);
                emitter.onNext(new MessageVO<>(scheduleDao.querySchedule()));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Boolean>> updateSchedule(String schedule, int curWeek) {
        return Observable.create(
            (ObservableEmitter<MessageVO<Boolean>> emitter) -> {
                ScheduleDao scheduleDao = new ScheduleDao(context);
                boolean status = scheduleDao.updateSchedule(schedule, curWeek);
                if (!status)
                    emitter.onNext(new MessageVO<>(false, "Update Schedule Failed"));
                else
                    emitter.onNext(new MessageVO<>(true));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Boolean>> deleteSchedule() {
        return Observable.create(
            (ObservableEmitter<MessageVO<Boolean>> emitter) -> {
                ScheduleDao scheduleDao = new ScheduleDao(context);
                emitter.onNext(new MessageVO<>(scheduleDao.deleteSchedule()));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
