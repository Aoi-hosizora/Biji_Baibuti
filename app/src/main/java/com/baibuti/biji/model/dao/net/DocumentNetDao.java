package com.baibuti.biji.model.dao.net;

import com.baibuti.biji.model.dao.daoInterface.IDocumentDao;
import com.baibuti.biji.model.dto.DocumentDTO;
import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.Document;
import com.baibuti.biji.service.auth.AuthManager;
import com.baibuti.biji.service.retrofit.RetrofitFactory;
import com.baibuti.biji.service.retrofit.ServerErrorHandle;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DocumentNetDao implements IDocumentDao {

    @Override
    public List<Document> queryAllDocuments() throws ServerException {
        Observable<ResponseDTO<DocumentDTO[]>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getAllDocuments()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<DocumentDTO[]> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return Arrays.asList(DocumentDTO.toDocuments(response.getData()));
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    @Override
    public List<Document> queryDocumentsByClassName(String className) throws ServerException {
        Observable<ResponseDTO<DocumentDTO[]>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getDocumentsByFileClass(className)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<DocumentDTO[]> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return Arrays.asList(DocumentDTO.toDocuments(response.getData()));
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    @Override
    public Document queryDocumentById(int id) throws ServerException {
        Observable<ResponseDTO<DocumentDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getDocumentById(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<DocumentDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return response.getData().toDocument();
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    @Override
    public long insertDocument(Document document) throws ServerException {
        Observable<ResponseDTO<DocumentDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .insertDocument(DocumentDTO.toDocument(document))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<DocumentDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return response.getData().getId();
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    @Override
    public boolean updateDocument(Document document) throws ServerException {
        Observable<ResponseDTO<DocumentDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .updateDocument(DocumentDTO.toDocument(document))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<DocumentDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return true;
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    @Override
    public boolean deleteDocument(int id) throws ServerException {
        Observable<ResponseDTO<DocumentDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteDocument(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<DocumentDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return true;
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }
}

/*

    public static boolean getSharedFiles(String params) throws ServerException {
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
                throw new ServerException(msg.getMessage(), msg.getDetail(), code);
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

        try (FileOutputStream fileOutputStream = new FileOutputStream(getFilePath(foldername) + filename)) {
            fileOutputStream.write(bFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new File(getFilePath(foldername) + filename);
    }

 */