package com.baibuti.biji.Net.Modules.File;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.baibuti.biji.Data.Models.Document;
import com.baibuti.biji.Interface.IPushCallBack;
import com.baibuti.biji.Net.Models.ReqBody.DocumentReqBody;
import com.baibuti.biji.Net.Models.RespBody.MessageResp;
import com.baibuti.biji.Net.Models.RespObj.ServerErrorException;
import com.baibuti.biji.Net.Models.RespType;
import com.baibuti.biji.Net.Modules.Auth.AuthMgr;
import com.baibuti.biji.Net.NetUtil;
import com.baibuti.biji.Net.Urls;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
        RespType resp = NetUtil.httpGetSync(AllFileUrl + params, NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken()));
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                DocumentReqBody[] rets = DocumentReqBody.getFileRespsFromJson(resp.getBody());
                Log.e("", "getAllFiles: " + rets[0].getFilename());
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

        NetUtil.httpPostFileAsync(
                PostFileUrl,
                "file", new File(document.getDocumentPath()),
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
                            pushCallBack.onCallBack();
                        }
                    }
                }
        );
    }

    public static boolean deleteFile(Document document) throws ServerErrorException {
        RespType resp = NetUtil.httpPostPutDeleteSync(
                DeleteFileUrl, NetUtil.DELETE,
                DocumentReqBody.toFileReqBody(document).toJson(),
                NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken())
        );
        Log.e("", "deleteFile: " + document.getDocumentClassName() + " , " + document.getDocumentName());
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);
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
        RespType resp = NetUtil.httpPostPutDeleteSync(
                DeleteFileByClassUrl, NetUtil.DELETE,
                "{\"foldername\": \"" + fileClassName + "\"}",
                NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken())
        );
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);
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

    public static Document downloadFile(Document document) throws ServerErrorException {
        File file = NetUtil.httpGetFileSync(
                DownloadFileUrl + "?foldername=" + document.getDocumentClassName() +
                "&&filename=" + document.getDocumentName(),
                document.getDocumentClassName(),
                document.getDocumentName(),
                NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken())
        );
        document.setDocumentPath(file.getPath());
        return document;
    }

    public static void pushDocumentsAsync(Document[] documents, @NonNull IPushCallBack pushCallBack) throws ServerErrorException {
        Log.e("测试", "pushDocumentsAsync: Token is " + AuthMgr.getInstance().getToken());
        NetUtil.httpPostPutDeleteAsync(
                PushFileUrl, NetUtil.POST,
                DocumentReqBody.getJsonFromDocumentBodies(DocumentReqBody.toFileReqBodies(documents)),
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
                            Log.e("测试", "pushDocumentsAsync: newToken is " + AuthMgr.getInstance().getToken());
                            pushCallBack.onCallBack();
                        }
                    }
                }
        );
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
