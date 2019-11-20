package com.baibuti.biji.model.dao.net;

import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dao.daoInterface.INoteDao;
import com.baibuti.biji.model.dto.OneFieldDTO;
import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.Note;
import com.baibuti.biji.common.Urls;
import com.baibuti.biji.common.auth.AuthManager;
import com.baibuti.biji.common.retrofit.RetrofitFactory;
import com.baibuti.biji.model.dto.NoteDTO;
import com.baibuti.biji.common.retrofit.ServerErrorHandle;
import com.baibuti.biji.util.imgTextUtil.StringUtil;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class NoteNetDao implements INoteDao {

    @Override
    public List<Note> queryAllNotes() throws ServerException {
        Observable<ResponseDTO<NoteDTO[]>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getAllNotes()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<NoteDTO[]> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return Arrays.asList(NoteDTO.toNotes(response.getData()));
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    @Override
    public List<Note> queryNotesByGroupId(int id) throws ServerException {
        Observable<ResponseDTO<NoteDTO[]>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getNotesByGroupId(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<NoteDTO[]> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return Arrays.asList(NoteDTO.toNotes(response.getData()));
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    @Override
    public Note queryNoteById(int id) throws ServerException {
        Observable<ResponseDTO<NoteDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getNoteById(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<NoteDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return response.getData().toNote();
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
    public DbStatusType insertNote(Note note) throws ServerException {

        // 上传笔记图片
        try {
            uploadImage(note);
        } catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            return DbStatusType.UPLOAD_FAILED;
        }

        Observable<ResponseDTO<NoteDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .insertNote(note.getTitle(), note.getContent(), note.getGroup().getId(),
                note.getCreateTime_FullString(), note.getUpdateTime_FullString())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<NoteDTO> response = observable.toFuture().get();
            switch (response.getCode()) {
                case ServerErrorHandle.SUCCESS:
                    return DbStatusType.SUCCESS;
                case ServerErrorHandle.HAS_EXISTED:
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
     * @return SUCCESS | FAILED | UPLOAD_FAILED
     */
    @Override
    public DbStatusType updateNote(Note note) throws ServerException {

        // 上传笔记图片
        try {
            uploadImage(note);
        } catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            return DbStatusType.UPLOAD_FAILED;
        }

        Observable<ResponseDTO<NoteDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .updateNote(note.getId(), note.getTitle(), note.getContent(), note.getGroup().getId())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<NoteDTO> response = observable.toFuture().get();
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
     * SUCCESS | FAILED
     */
    @Override
    public DbStatusType deleteNote(int id) throws ServerException {

        Observable<ResponseDTO<NoteDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteNote(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<NoteDTO> response = observable.toFuture().get();
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

    @Override
    public int deleteNotes(int[] ids) throws ServerException {

        Observable<ResponseDTO<OneFieldDTO.CountDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteNotes(ids)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<OneFieldDTO.CountDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return response.getData().getCount();
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 上传所有笔本地图片
     */
    private void uploadImage(Note note) throws ServerException, InterruptedException, ExecutionException {

        List<String> textList = StringUtil.cutStringByImgTag(note.getContent()); // 所有
        Set<String> uploadUrl = new TreeSet<>(); // 所有本地图片
        for (String blocks : textList)
            if (!(blocks.contains("<img") && blocks.contains("src=")))
                uploadUrl.add(blocks);

        for (String url : uploadUrl.toArray(new String[0])) {
            Observable<ResponseDTO<OneFieldDTO.FilenameDTO>> observable = RetrofitFactory.getInstance()
                .createRequest(AuthManager.getInstance().getAuthorizationHead())
                .uploadImage(new File(url), OneFieldDTO.RawImageType_Note)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

            try {
                ResponseDTO<OneFieldDTO.FilenameDTO> response = observable.toFuture().get();
                if (response.getCode() != ServerErrorHandle.SUCCESS)
                    throw ServerErrorHandle.parseErrorMessage(response);
                String newUrl = Urls.BaseServerEndPoint + response.getData().getFilename();
                note.setContent(note.getContent().replace(url, newUrl));
            }
            catch (ServerException | InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
                throw ex;
            }
        }
    }
}
