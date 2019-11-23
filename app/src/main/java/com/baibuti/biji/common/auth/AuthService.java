package com.baibuti.biji.common.auth;

import com.baibuti.biji.common.auth.dto.AuthRespDTO;
import com.baibuti.biji.common.retrofit.RetrofitFactory;
import com.baibuti.biji.model.vo.MessageVO;


import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AuthService {

    /**
     * 获取当前登录用户
     */
    public static Observable<MessageVO<AuthRespDTO>> currentAuth() {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .currentUser()
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<AuthRespDTO>(false, responseDTO.getMessage());
                return new MessageVO<>(responseDTO.getData());
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<MessageVO<AuthRespDTO>> login(String username, String password, int expiration) {
        return RetrofitFactory.getInstance()
            .createRequest(RetrofitFactory.getHeader())
            .login(username, password, expiration)
            .map(response -> {
                if (response.code() != 200)
                    return new MessageVO<AuthRespDTO>(false, response.body().getMessage());
                AuthRespDTO respDTO = response.body().getData();
                respDTO.setToken(response.headers().get("Authorization"));
                return new MessageVO<>(respDTO);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<MessageVO<AuthRespDTO>> register(String username, String password) {
        return RetrofitFactory.getInstance()
            .createRequest(RetrofitFactory.getHeader())
            .register(username, password)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<AuthRespDTO>(false, responseDTO.getMessage());
                return new MessageVO<>(responseDTO.getData());
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<MessageVO<Boolean>> logout() {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .logout()
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Boolean>(false, responseDTO.getMessage());
                return new MessageVO<>(true);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
