package com.baibuti.biji.service.auth;

import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.service.auth.dto.LoginDTO;
import com.baibuti.biji.service.auth.dto.AuthRespDTO;
import com.baibuti.biji.service.auth.dto.RegisterDTO;
import com.baibuti.biji.service.retrofit.RetrofitFactory;
import com.baibuti.biji.service.retrofit.ServerErrorHandle;

import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class AuthService {

    public synchronized static AuthRespDTO login(String username, String password) throws ServerException {
        return login(username, password, 0);
    }

    private synchronized static AuthRespDTO login(String username, String password, int expiration) throws ServerException {
        Observable<Response<ResponseDTO<AuthRespDTO>>> observable = RetrofitFactory.getInstance()
            .createRequest(RetrofitFactory.getHeader())
            .login(new LoginDTO(username, password, expiration))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            Response<ResponseDTO<AuthRespDTO>> response = observable.toFuture().get();
            if (response.code() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response.body());

            AuthRespDTO respDTO = response.body().getData();
            respDTO.setToken(response.headers().get("Authorization"));

            return respDTO;
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    public synchronized static void logout() throws ServerException {
        Observable<ResponseDTO<AuthRespDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .logout()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<AuthRespDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS) {
                throw ServerErrorHandle.parseErrorMessage(response);
            }
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    public synchronized static AuthRespDTO register(String username, String password) throws ServerException {
        Observable<ResponseDTO<AuthRespDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(RetrofitFactory.getHeader())
            .register(new RegisterDTO(username, password))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<AuthRespDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return response.getData();
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }
}
