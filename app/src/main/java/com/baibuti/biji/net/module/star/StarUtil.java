package com.baibuti.biji.net.module.star;

import android.support.annotation.NonNull;
import android.util.Log;

import com.baibuti.biji.data.model.SearchItem;
import com.baibuti.biji.iGlobal.IPushCallBack;
import com.baibuti.biji.net.model.reqBody.StarReqBody;
import com.baibuti.biji.net.model.respBody.MessageResp;
import com.baibuti.biji.net.model.respObj.ServerErrorException;
import com.baibuti.biji.net.model.RespType;
import com.baibuti.biji.net.module.auth.AuthMgr;
import com.baibuti.biji.net.NetUtil;
import com.baibuti.biji.net.Urls;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.internal.annotations.EverythingIsNonNull;

public class StarUtil {

    private static final String AllStarUrl = Urls.StarUrl + "/all";
    private static final String InsertStarUrl = Urls.StarUrl + "/insert";
    private static final String DeleteStarUrl = Urls.StarUrl + "/delete";

    public static SearchItem[] getAllStars() throws ServerErrorException {
        RespType resp = NetUtil.httpGetSync(AllStarUrl, NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken()));
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                StarReqBody[] rets = StarReqBody.getStarRespsFromJson(resp.getBody());
                Log.e("", "getAllStars: " + rets[0].getUrl() );
                return StarReqBody.toSearchItems(rets);
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerErrorException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static SearchItem insertStar(SearchItem searchItem) throws ServerErrorException {
        RespType resp = NetUtil.httpPostPutDeleteSync(
                InsertStarUrl, NetUtil.PUT,
                StarReqBody.toStarReqBody(searchItem).toJson(),
                NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken())
        );

        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                StarReqBody ret = StarReqBody.getStarRespFromJson(resp.getBody());
                return ret.toSearchItem();
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerErrorException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static SearchItem deleteStar(SearchItem searchItem) throws ServerErrorException {
        RespType resp = NetUtil.httpPostPutDeleteSync(
                DeleteStarUrl, NetUtil.DELETE,
                StarReqBody.toStarReqBody(searchItem).toJson(),
                NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken())
        );
        Log.e("", "deleteStar: " + searchItem.getTitle() + " , " + searchItem.getUrl());
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                StarReqBody ret = StarReqBody.getStarRespFromJson(resp.getBody());
                return ret.toSearchItem();
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerErrorException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
