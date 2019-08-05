package com.baibuti.biji.Net.Modules.Star;

import com.baibuti.biji.Data.Models.SearchItem;
import com.baibuti.biji.Net.Models.ReqBody.StarReqBody;
import com.baibuti.biji.Net.Models.RespBody.MessageResp;
import com.baibuti.biji.Net.Models.RespObj.ServerErrorException;
import com.baibuti.biji.Net.Models.RespType;
import com.baibuti.biji.Net.Modules.Auth.AuthMgr;
import com.baibuti.biji.Net.NetUtil;
import com.baibuti.biji.Net.Urls;

public class StarUtil {

    private static final String AllStarUrl = Urls.StarUrl + "/all";
    private static final String InsertStarUrl = Urls.StarUrl + "/insert";
    private static final String DeleteStarUrl = Urls.StarUrl + "/delete";

    public static SearchItem[] getAllStars() throws ServerErrorException {
        RespType resp = NetUtil.httpGetSync(AllStarUrl, NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken()));
        try {
            int code = resp.getCode();
            if (code == 200) {
                StarReqBody[] rets = StarReqBody.getStarRespsFromJson(resp.getBody());
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

        try {
            int code = resp.getCode();
            if (code == 200) {
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
