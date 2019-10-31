package com.baibuti.biji.service.auth;

import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.service.auth.dto.LoginDTO;
import com.baibuti.biji.service.auth.dto.AuthRespDTO;
import com.baibuti.biji.service.auth.dto.RegisterDTO;
import com.baibuti.biji.service.retrofit.RetrofitFactory;
import com.baibuti.biji.service.retrofit.ServerErrorHandle;

import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AuthService {

    public static Throwable login(String username, String password) {
        return login(username, password, 0);
    }

    private static Throwable login(String username, String password, int expiration) {
        Observable<ResponseDTO<AuthRespDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(RetrofitFactory.getHeader())
            .login(new LoginDTO(username, password, expiration))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<AuthRespDTO> response = observable.toFuture().get();
            if (response.getCode() != 200)
                return ServerErrorHandle.parseErrorMessage(response);

            return null;
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            return ex;
        }
    }

    public static boolean logout()  {
        Observable<ResponseDTO<AuthRespDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .logout()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<AuthRespDTO> response = observable.toFuture().get();
            return response.getCode() == 200;
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static Throwable register(String username, String password) {
        Observable<ResponseDTO<AuthRespDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(RetrofitFactory.getHeader())
            .register(new RegisterDTO(username, password))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<AuthRespDTO> response = observable.toFuture().get();
            if (response.getCode() != 200)
                return ServerErrorHandle.parseErrorMessage(response);

            return null;
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            return ex;
        }
    }
}
