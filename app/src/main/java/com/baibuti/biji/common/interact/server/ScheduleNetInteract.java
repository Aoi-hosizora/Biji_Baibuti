package com.baibuti.biji.common.interact.server;

import android.util.Pair;

import com.baibuti.biji.common.interact.contract.IScheduleInteract;
import com.baibuti.biji.common.auth.AuthManager;
import com.baibuti.biji.common.retrofit.RetrofitFactory;
import com.baibuti.biji.model.vo.MessageVO;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ScheduleNetInteract implements IScheduleInteract {

    @Override
    public Observable<MessageVO<Pair<String, Integer>>> querySchedule() {
         return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getSchedule()
            .map((responseDTO) -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Pair<String, Integer>>(false, responseDTO.getMessage());
                return new MessageVO<>(new Pair<>(responseDTO.getData().getSchedule(), responseDTO.getData().getWeek()));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Boolean>> updateSchedule(String schedule, int currWeek) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .updateSchedule(schedule, currWeek)
            .map((responseDTO) -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Boolean>(false, responseDTO.getMessage());
                return new MessageVO<>(true);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Boolean>> deleteSchedule() {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteSchedule()
            .map((responseDTO) -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Boolean>(false, responseDTO.getMessage());
                return new MessageVO<>(true);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
