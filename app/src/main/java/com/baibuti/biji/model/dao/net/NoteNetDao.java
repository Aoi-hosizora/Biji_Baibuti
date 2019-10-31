package com.baibuti.biji.model.dao.net;

import com.baibuti.biji.model.dao.daoInterface.INoteDao;
import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.model.po.Note;
import com.baibuti.biji.service.auth.AuthManager;
import com.baibuti.biji.service.retrofit.RetrofitFactory;
import com.baibuti.biji.model.dto.NoteDTO;
import com.baibuti.biji.service.retrofit.ServerErrorHandle;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class NoteNetDao implements INoteDao {

    @Override
    public List<Note> queryAllNotes() throws Exception {
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
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public List<Note> queryNotesByGroupId(int id) throws Exception {
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
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public Note queryNoteById(int id) throws Exception {
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
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public long insertNote(Note note) throws Exception {
        // TODO 同时上传图片
        Observable<ResponseDTO<NoteDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .insertNote(NoteDTO.toNoteDTO(note))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<NoteDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return response.getData().getId();
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public boolean updateNote(Note note) throws Exception {
        // TODO 同时上传图片
        Observable<ResponseDTO<NoteDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .updateNote(NoteDTO.toNoteDTO(note))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<NoteDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return true;
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public boolean deleteNote(int id) throws Exception {
        // TODO 同时判断，删除图片
        Observable<ResponseDTO<NoteDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteNote(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<NoteDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return true;
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
}
