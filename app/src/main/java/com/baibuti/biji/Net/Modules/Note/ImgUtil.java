package com.baibuti.biji.Net.Modules.Note;

import com.baibuti.biji.Net.Models.RespBody.MessageResp;
import com.baibuti.biji.Net.Models.RespObj.ServerErrorException;
import com.baibuti.biji.Net.Models.RespObj.UploadStatus;
import com.baibuti.biji.Net.Models.RespType;
import com.baibuti.biji.Net.Modules.Auth.AuthMgr;
import com.baibuti.biji.Net.NetUtil;
import com.baibuti.biji.Net.Urls;

import java.io.File;
import java.net.URI;

public class ImgUtil {

    private static final String ImgUploadUrl = Urls.NoteUrl + "/img/upload";
    private static final String GetImgUrl = Urls.NoteUrl + "/img/blob/%s/%s";

    public static UploadStatus uploadImg(URI uri) throws ServerErrorException {
        File img = new File(uri);
        RespType resp = NetUtil.httpPostFileSync(ImgUploadUrl, "noteimg", img, NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken()));
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                return new UploadStatus(msg.getDetail(), msg.getMessage());
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

    public void GetImg() {
        // TODO
    }
}
