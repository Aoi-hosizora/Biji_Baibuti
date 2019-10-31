package com.baibuti.biji.data.dao.net;

import com.baibuti.biji.data.dao.daoInterface.INoteDao;
import com.baibuti.biji.data.model.Note;
import com.baibuti.biji.data.dao.RetrofitFactory;
import com.baibuti.biji.data.dto.NoteDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class NoteNetDao implements INoteDao {

    @Override
    public List<Note> queryAllNotes() {
        Observable<Note[]> observable = RetrofitFactory.getInstance()
            .createRequest(RetrofitFactory.getHeader("", ""))
            .getAllNotes()
            .subscribeOn(Schedulers.io())
            .map(NoteDTO::toNotes)
            .observeOn(AndroidSchedulers.mainThread());

        try {
            return Arrays.asList(observable.toFuture().get());
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Note> queryNotesByGroupId(int id) {
        List<Note> notes = queryAllNotes();
        List<Note> groupNotes = new ArrayList<>();
        for (Note note : notes)
            if (note.getGroup().getId() == id)
                groupNotes.add(note);

        return groupNotes;
    }

    @Override
    public Note queryNoteById(int id) {
        Observable<Note> observable = RetrofitFactory.getInstance()
            .createRequest(RetrofitFactory.getHeader("", ""))
            .getNoteById(id)
            .subscribeOn(Schedulers.io())
            .map(NoteDTO::toNote)
            .observeOn(AndroidSchedulers.mainThread());

        try {
            return observable.toFuture().get();
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public long insertNote(Note note) {
        // TODO 同时上传图片
        Observable<Note> observable = RetrofitFactory.getInstance()
            .createRequest(RetrofitFactory.getHeader("", ""))
            .insertNote(NoteDTO.toNoteDTO(note))
            .subscribeOn(Schedulers.io())
            .map(NoteDTO::toNote)
            .observeOn(AndroidSchedulers.mainThread());

        try {
            Note new_note = observable.toFuture().get();
            return new_note.getId();
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean updateNote(Note note) {
        // TODO 同时上传图片
        Observable<Note> observable = RetrofitFactory.getInstance()
            .createRequest(RetrofitFactory.getHeader("", ""))
            .updateNote(NoteDTO.toNoteDTO(note))
            .subscribeOn(Schedulers.io())
            .map(NoteDTO::toNote)
            .observeOn(AndroidSchedulers.mainThread());

        try {
            observable.toFuture().get();
            return true;
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteNote(int id) {
        // TODO 同时判断，删除图片
        Observable<Note> observable = RetrofitFactory.getInstance()
            .createRequest(RetrofitFactory.getHeader("", ""))
            .deleteNote(id)
            .subscribeOn(Schedulers.io())
            .map(NoteDTO::toNote)
            .observeOn(AndroidSchedulers.mainThread());

        try {
            observable.toFuture().get();
            return true;
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
