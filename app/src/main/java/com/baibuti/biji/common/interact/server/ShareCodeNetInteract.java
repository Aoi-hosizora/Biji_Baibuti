package com.baibuti.biji.common.interact.server;

import com.baibuti.biji.model.po.DocClass;
import com.baibuti.biji.model.po.Document;
import com.baibuti.biji.common.auth.AuthManager;
import com.baibuti.biji.common.retrofit.RetrofitFactory;
import com.baibuti.biji.model.vo.MessageVO;


import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ShareCodeNetInteract {

    /**
     * 默认的期限
     */
    public static final int DEFAULT_EX = 3600;

    /**
     * 为 docs 新建共享码
     */
    public Observable<MessageVO<String>> newShareCode(Document[] documents, int exp) {
        int[] ids = new int[documents.length];
        for (int i = 0; i < documents.length; i++)
            ids[i] = documents[i].getId();

        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .putDocToShare(exp, ids)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<String>(false, responseDTO.getMessage());
                else
                    return new MessageVO<>(responseDTO.getData().getSc());
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 为 DocClass 新建共享码
     */
    public Observable<MessageVO<String>> newShareCode(DocClass docClass, int exp) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .putDocClassToShare(exp, docClass.getId())
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<String>(false, responseDTO.getMessage());
                else
                    return new MessageVO<>(responseDTO.getData().getSc());
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

//    /**
//     * 获取用户当前所有分享码
//     */
//    public Observable<MessageVO<ShareCodeDTO[]>> getAllShareCode() throws ServerException {
//        Observable<ResponseDTO<ShareCodeDTO[]>> observable = RetrofitFactory.getInstance()
//            .createRequest(AuthManager.getInstance().getAuthorizationHead())
//            .getAllShareCode()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread());
//
//        try {
//            ResponseDTO<ShareCodeDTO[]> response = observable.toFuture().get();
//            if (response.getCode() != MessageErrorParser.SUCCESS)
//                throw MessageErrorParser.parseErrorMessage(response);
//
//            return response.getData();
//        } catch (ServerException | InterruptedException | ExecutionException ex) {
//            ex.printStackTrace();
//            throw MessageErrorParser.getClientError(ex);
//        }
//    }
//
//    /**
//     * 删除用户的一些共享码
//     */
//    public Observable<MessageVO<Integer>> deleteShareCodes(String[] scs) throws ServerException {
//        Observable<ResponseDTO<OneFieldDTO.CountDTO>> observable = RetrofitFactory.getInstance()
//            .createRequest(AuthManager.getInstance().getAuthorizationHead())
//            .deleteShareCodes(scs)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread());
//
//        try {
//            ResponseDTO<OneFieldDTO.CountDTO> response = observable.toFuture().get();
//            if (response.getCode() != MessageErrorParser.SUCCESS)
//                throw MessageErrorParser.parseErrorMessage(response);
//
//            return response.getData().getCount();
//        } catch (ServerException | InterruptedException | ExecutionException ex) {
//            ex.printStackTrace();
//            throw MessageErrorParser.getClientError(ex);
//        }
//    }
//
//    /**
//     * 删除用户的所有共享码
//     */
//    public Observable<MessageVO<Integer>> deleteUserAllShareCodes() throws ServerException {
//        Observable<ResponseDTO<OneFieldDTO.CountDTO>> observable = RetrofitFactory.getInstance()
//            .createRequest(AuthManager.getInstance().getAuthorizationHead())
//            .deleteUserShareCodes()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread());
//
//        try {
//            ResponseDTO<OneFieldDTO.CountDTO> response = observable.toFuture().get();
//            if (response.getCode() != MessageErrorParser.SUCCESS)
//                throw MessageErrorParser.parseErrorMessage(response);
//
//            return response.getData().getCount();
//        } catch (ServerException | InterruptedException | ExecutionException ex) {
//            ex.printStackTrace();
//            throw MessageErrorParser.getClientError(ex);
//        }
//    }
}
