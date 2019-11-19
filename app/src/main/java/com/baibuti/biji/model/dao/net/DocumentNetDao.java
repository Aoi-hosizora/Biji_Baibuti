package com.baibuti.biji.model.dao.net;

import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dao.daoInterface.IDocumentDao;
import com.baibuti.biji.model.dto.DocumentDTO;
import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.Document;
import com.baibuti.biji.service.auth.AuthManager;
import com.baibuti.biji.service.retrofit.RetrofitFactory;
import com.baibuti.biji.service.retrofit.ServerErrorHandle;

import java.io.File;
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
    public List<Document> queryDocumentByClassId(int cid) throws ServerException {
        Observable<ResponseDTO<DocumentDTO[]>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getDocumentByClassId(cid)
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

    /**
     * @return SUCCESS | FAILED | UPLOAD_FAILED
     */
    @Override
    public DbStatusType insertDocument(Document document) throws ServerException {

        /*

        File img = new File(path);
        HashMap<String, RequestBody> requestBodyHashMap = new HashMap<>();
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), img);
        requestBodyHashMap.put("img", requestBody);

         */

        Observable<ResponseDTO<DocumentDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .insertDocument(new File(document.getFilename()), document.getDocClass().getId())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<DocumentDTO> response = observable.toFuture().get();
            switch (response.getCode()) {
                case ServerErrorHandle.SUCCESS:
                    return DbStatusType.SUCCESS;
                case ServerErrorHandle.HAS_EXISTED:
                case ServerErrorHandle.DATABASE_FAILED:
                    return DbStatusType.FAILED;
                case ServerErrorHandle.SAVE_FILE_FAILED:
                    return DbStatusType.UPLOAD_FAILED;
                default:
                    throw ServerErrorHandle.parseErrorMessage(response);
            }
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    /**
     * @return SUCCESS | FAILED
     */
    @Override
    public DbStatusType updateDocument(Document document) throws ServerException {
        Observable<ResponseDTO<DocumentDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .updateDocument(document.getId(), document.getFilename(), document.getDocClass().getId())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<DocumentDTO> response = observable.toFuture().get();
            switch (response.getCode()) {
                case ServerErrorHandle.SUCCESS:
                    return DbStatusType.SUCCESS;
                case ServerErrorHandle.NOT_FOUND:
                case ServerErrorHandle.DATABASE_FAILED:
                    return DbStatusType.FAILED;
                default:
                    throw ServerErrorHandle.parseErrorMessage(response);
            }
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    /**
     * @return SUCCESS | FAILED
     */
    @Override
    public DbStatusType deleteDocument(int id) throws ServerException {
        Observable<ResponseDTO<DocumentDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteDocument(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<DocumentDTO> response = observable.toFuture().get();
            switch (response.getCode()) {
                case ServerErrorHandle.SUCCESS:
                    return DbStatusType.SUCCESS;
                case ServerErrorHandle.NOT_FOUND:
                case ServerErrorHandle.DATABASE_FAILED:
                    return DbStatusType.FAILED;
                default:
                    throw ServerErrorHandle.parseErrorMessage(response);
            }
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

        try (FileOutputStream fileOutputStream = new FileOutputStream(getFilename(foldername) + filename)) {
            fileOutputStream.write(bFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new File(getFilename(foldername) + filename);
    }

 */