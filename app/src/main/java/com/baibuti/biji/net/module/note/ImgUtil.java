package com.baibuti.biji.net.module.note;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.baibuti.biji.net.model.reqBody.DelImgReqBody;
import com.baibuti.biji.net.model.respBody.MessageResp;
import com.baibuti.biji.net.model.respObj.ServerErrorException;
import com.baibuti.biji.net.model.respObj.UploadStatus;
import com.baibuti.biji.net.model.RespType;
import com.baibuti.biji.net.module.auth.AuthMgr;
import com.baibuti.biji.net.NetUtil;
import com.baibuti.biji.net.Urls;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.annotations.EverythingIsNonNull;

public class ImgUtil {

    private static final String ImgUploadUrl = Urls.ImageUrl + "/upload";
    private static final String ImgUploadKey = "noteimg";
    public static final String GetImgUrlHead = Urls.ImageUrl + "/blob/";
    private static final String GetImgUrl = GetImgUrlHead + "%s/%s";
    private static final String DeleteImgUrl = Urls.ImageUrl + "/delete";

    public static UploadStatus uploadImg(String path) throws ServerErrorException {
        Log.e("", "uploadImg: " + ImgUploadUrl );
        File img = new File(path);
        RespType resp = NetUtil.httpPostFileSync(ImgUploadUrl,
            ImgUploadKey, img,
            NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken()));
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());

                String newFileName = msg.getDetail(); // aoihosizora/2019080919590813.
                Log.e("", "uploadImg: " + newFileName );
                newFileName = String.format(Locale.CHINA,
                    GetImgUrl,
                    newFileName.split("/")[0], newFileName.split("/")[1]
                ); // xxx/note/img/blob/aoihosizora/2019080919590813.jpg

                return new UploadStatus(newFileName, msg.getMessage());
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

    public interface IImageBack {
        void onGetImg(Bitmap bitmap);
    }

    /**
     * ImgPopupDialog OCRActivity 用
     * 新线程 异步!!!
     */
    @WorkerThread
    public static void GetImgAsync(String url, IImageBack imageBack) {
        Log.e("", "GetImg: " + url );
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        try {
            client.newCall(request).enqueue(new Callback() {
                @EverythingIsNonNull
                @Override
                public void onFailure(Call call, IOException e) { }

                @EverythingIsNonNull
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    InputStream in = response.body().byteStream();
                    imageBack.onGetImg(BitmapFactory.decodeStream(in));
                }
            });
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public static void DeleteImgsAsync(String[] urls) {
        NetUtil.httpPostPutDeleteAsync(
            DeleteImgUrl, NetUtil.DELETE,
            DelImgReqBody.toJsons(DelImgReqBody.toReqBodiesFromUrls(urls)),
            NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken()),
            new Callback() {
                @Override
                @EverythingIsNonNull
                public void onFailure(Call call, IOException e) { }

                @Override
                @EverythingIsNonNull
                public void onResponse(Call call, Response response) throws IOException {
                    int code = response.code();
                    if (code == 200) {
                        String newToken = response.headers().get("Authorization");
                        if (newToken != null && !(newToken.isEmpty()))
                            AuthMgr.getInstance().setToken(newToken);
                    }
                }
            }
        );
    }
}
