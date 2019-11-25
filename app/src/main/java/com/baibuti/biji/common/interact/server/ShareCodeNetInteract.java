package com.baibuti.biji.common.interact.server;

import com.baibuti.biji.model.dto.DocumentDTO;
import com.baibuti.biji.model.dto.ShareCodeDTO;
import com.baibuti.biji.model.po.DocClass;
import com.baibuti.biji.model.po.Document;
import com.baibuti.biji.common.auth.AuthManager;
import com.baibuti.biji.common.retrofit.RetrofitFactory;
import com.baibuti.biji.model.po.ShareCodeItem;
import com.baibuti.biji.model.vo.MessageVO;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ShareCodeNetInteract {

    // /**
    //  * 默认的期限
    //  */
    // public static final int DEFAULT_EX = 3600;

    /**
     * 获得共享码内容
     */
    public Observable<MessageVO<List<Document>>> getShareCodeContents(String sc) {

        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getShareCodeContents(sc)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<List<Document>>(false, responseDTO.getMessage());
                else {
                    List<Document> fromDocuments = new ArrayList<>();
                    Collections.addAll(fromDocuments, DocumentDTO.toDocuments(responseDTO.getData()));
                    return new MessageVO<>(fromDocuments);
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 为 docs 新建共享码
     */
    public Observable<MessageVO<String>> newShareCode(Document[] documents, int exp) {
        Integer[] ids = new Integer[documents.length];
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

    /**
     * 获取用户当前所有分享码
     */
    public Observable<MessageVO<List<ShareCodeItem>>> getAllShareCode() {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getAllShareCode()
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<List<ShareCodeItem>>(false, responseDTO.getMessage());
                List<ShareCodeItem> fromScs = new ArrayList<>();
                Collections.addAll(fromScs, ShareCodeDTO.toShareCodeItems(responseDTO.getData()));
                return new MessageVO<>(fromScs);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 删除用户的一些共享码
     */
    public Observable<MessageVO<Integer>> deleteShareCodes(String[] scs) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteShareCodes(scs)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Integer>(false, responseDTO.getMessage());
                else
                    return new MessageVO<>(responseDTO.getData().getCount());
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 删除用户的所有共享码
     */
    public Observable<MessageVO<Integer>> deleteUserAllShareCodes() {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteUserShareCodes()
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Integer>(false, responseDTO.getMessage());
                else
                    return new MessageVO<>(responseDTO.getData().getCount());
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
