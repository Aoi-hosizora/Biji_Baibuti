package com.baibuti.biji.common.interact.client;

import android.content.Context;

import com.baibuti.biji.common.interact.contract.INoteInteract;
import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dao.local.NoteDao;
import com.baibuti.biji.model.po.Note;
import com.baibuti.biji.model.vo.MessageVO;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class NoteInteract implements INoteInteract {

    private Context context;

    public NoteInteract(Context context) {
        this.context = context;
    }

    @Override
    public Observable<MessageVO<List<Note>>> queryAllNotes() {
        return Observable.create(
            (ObservableEmitter<MessageVO<List<Note>>> emitter) -> {
                NoteDao noteDao = new NoteDao(context);
                emitter.onNext(new MessageVO<>(noteDao.queryAllNotes()));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<List<Note>>> queryNotesByGroupId(int id) {
        return Observable.create(
            (ObservableEmitter<MessageVO<List<Note>>> emitter) -> {
                NoteDao noteDao = new NoteDao(context);
                emitter.onNext(new MessageVO<>(noteDao.queryNotesByGroupId(id)));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Note>> queryNoteById(int id) {
        return Observable.create(
            (ObservableEmitter<MessageVO<Note>> emitter) -> {
                NoteDao noteDao = new NoteDao(context);
                emitter.onNext(new MessageVO<>(noteDao.queryNoteById(id)));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<DbStatusType>> insertNote(Note note) {
        return Observable.create(
            (ObservableEmitter<MessageVO<DbStatusType>> emitter) -> {
                NoteDao noteDao = new NoteDao(context);
                emitter.onNext(new MessageVO<>(noteDao.insertNote(note)));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<DbStatusType>> updateNote(Note note) {
        return Observable.create(
            (ObservableEmitter<MessageVO<DbStatusType>> emitter) -> {
                NoteDao noteDao = new NoteDao(context);
                emitter.onNext(new MessageVO<>(noteDao.updateNote(note)));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<DbStatusType>> deleteNote(int id) {
        return Observable.create(
            (ObservableEmitter<MessageVO<DbStatusType>> emitter) -> {
                NoteDao noteDao = new NoteDao(context);
                emitter.onNext(new MessageVO<>(noteDao.deleteNote(id)));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Integer>> deleteNotes(int[] id) {
        return Observable.create(
            (ObservableEmitter<MessageVO<Integer>> emitter) -> {
                NoteDao noteDao = new NoteDao(context);
                emitter.onNext(new MessageVO<>(noteDao.deleteNotes(id)));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
