package com.baibuti.biji.net.module.file;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.baibuti.biji.model.po.Document;
import com.baibuti.biji.iGlobal.IPushCallBack;
import com.baibuti.biji.net.model.reqBody.DocumentReqBody;
import com.baibuti.biji.net.model.respBody.MessageResp;
import com.baibuti.biji.net.model.respObj.ServerErrorException;
import com.baibuti.biji.net.model.RespType;
import com.baibuti.biji.service.auth.AuthManager;
import com.baibuti.biji.net.NetHelper;
import com.baibuti.biji.service.Urls;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.internal.annotations.EverythingIsNonNull;

public class DocumentUtil {

    private static final String AllFileUrl = Urls.FileUrl + "/all";
    private static final String PostFileUrl = Urls.FileUrl + "/upload";
    private static final String DeleteFileUrl = Urls.FileUrl + "/delete";
    private static final String DeleteFileByClassUrl = Urls.FileUrl + "/delete_all";
    private static final String DownloadFileUrl = Urls.FileUrl + "/download";
    private static final String PushFileUrl = Urls.FileUrl + "/push";
    private static final String GetSharedDocuments = Urls.FileUrl + "/get_share";

    /**
     * 下载路径
     */
    public final static String FILE_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath()+"/Biji/";

    public static String getFilePath(String foldername){
        File file = new File(FILE_PATH+foldername+"/");
        if(!file.exists()){
            try{
                file.mkdirs();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return FILE_PATH+foldername+"/";
    }

    public static Document[] getAllFiles(String params) throws ServerErrorException {
        RespType resp = NetHelper.httpGetSync(AllFileUrl + params, NetHelper.getOneHeader("Authorization", AuthManager.getInstance().getToken()));
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthManager.getInstance().setToken(newToken);

                DocumentReqBody[] rets = DocumentReqBody.getFileRespsFromJson(resp.getBody());
                return DocumentReqBody.toDocuments(rets);
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

    public static void postFile(Document document, @NonNull IPushCallBack pushCallBack) throws ServerErrorException{
        Map<String, String> k_v = new HashMap<>();
        k_v.put("id", document.getId()+"");
        k_v.put("foldername", document.getClassName());
        NetHelper.httpPostFileAsync(
                PostFileUrl, k_v,
                "file", new File(document.getPath()),
                NetHelper.getOneHeader("Authorization", AuthManager.getInstance().getToken()),
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
                                AuthManager.getInstance().setToken(newToken);
                            pushCallBack.onCallBack();
                        }
                    }
                }
        );
    }

    public static boolean deleteFile(Document document) throws ServerErrorException {

        Log.e("测试", "deleteFile: " + DocumentReqBody.toFileReqBody(document).toJson());

        RespType resp = NetHelper.httpPostPutDeleteSync(
                DeleteFileUrl, NetHelper.DELETE,
                DocumentReqBody.toFileReqBody(document).toJson(),
                NetHelper.getOneHeader("Authorization", AuthManager.getInstance().getToken())
        );
        Log.e("", "deleteFile: " + document.getClassName() + " , " + document.getDocName());
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthManager.getInstance().setToken(newToken);
                return true;
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerErrorException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean deleteFiles(String fileClassName) throws ServerErrorException {
        RespType resp;
        try {
            resp = NetHelper.httpPostPutDeleteSync(
                    DeleteFileByClassUrl, NetHelper.DELETE,
                    new JSONObject().put("foldername", fileClassName).toString(),
                    NetHelper.getOneHeader("Authorization", AuthManager.getInstance().getToken())
            );
        }catch(JSONException e){
            e.printStackTrace();
            return false;
        }
        Log.e("测试", "deleteFiles: " + resp.getBody());
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthManager.getInstance().setToken(newToken);
                return true;
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerErrorException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean downloadFile(Document document) throws ServerErrorException {
        File file = NetHelper.httpGetFileSync(
                DownloadFileUrl + "?foldername=" + document.getClassName() +
                "&&filename=" + document.getDocName() +
                "&&id=" + document.getId(),
                document.getClassName(),
                document.getDocName(),
                NetHelper.getOneHeader("Authorization", AuthManager.getInstance().getToken())
        );
        if(null != file) {
            document.setPath(file.getPath());
            return true;
        }
        return false;
    }

    public static void pushDocumentsAsync(Document[] documents, @NonNull IPushCallBack pushCallBack) throws ServerErrorException {
        for(Document document: documents){
            Log.e("测试", "pushDocumentsAsync: \n"
            + document.getId() + ' ' + document.getDocName() + ' ' + document.getClassName() + '\n');
        }
        NetHelper.httpPostPutDeleteAsync(
                PushFileUrl, NetHelper.POST,
                DocumentReqBody.getJsonFromDocumentBodies(DocumentReqBody.toFileReqBodies(documents)),
                NetHelper.getOneHeader("Authorization", AuthManager.getInstance().getToken()),
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
                                AuthManager.getInstance().setToken(newToken);
                            Log.e("测试", "pushDocumentsAsync: newToken is " + AuthManager.getInstance().getToken());
                            pushCallBack.onCallBack();
                        }
                    }
                }
        );
    }

    public static boolean getSharedFiles(String params) throws ServerErrorException {
        RespType resp = NetHelper.httpGetSync(GetSharedDocuments + params,
                NetHelper.getOneHeader("Authorization", AuthManager.getInstance().getToken()));
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthManager.getInstance().setToken(newToken);

                return true;
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerErrorException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static String getFileType(String filename){
        String type;
        if (filename.endsWith(".doc") || filename.endsWith(".docx")) {
            type="doc";
        }else if(filename.endsWith(".ppt") || filename.endsWith(".pptx")){
            type="ppt";
        }else if(filename.endsWith(".xls") || filename.endsWith(".xlsx")){
            type="xls";
        }else if(filename.endsWith(".pdf")){
            type="pdf";
        }else if(filename.endsWith(".txt")){
            type="txt";
        }else if(filename.endsWith(".zip")){
            type="zip";
        }else{
            type="unknown";
        }
        return type;
    }

    public static File writeBytesToFile(byte[] bFile, String foldername, String filename) {

        try (FileOutputStream fileOuputStream = new FileOutputStream(getFilePath(foldername) + filename)) {
            fileOuputStream.write(bFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new File(getFilePath(foldername) + filename);
    }

}
