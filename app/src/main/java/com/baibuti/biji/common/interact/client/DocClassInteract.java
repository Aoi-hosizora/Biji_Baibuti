package com.baibuti.biji.common.interact.client;

import android.content.Context;

import com.baibuti.biji.common.interact.contract.IDocClassInteract;
import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dao.local.DocClassDao;
import com.baibuti.biji.model.po.DocClass;
import com.baibuti.biji.model.vo.MessageVO;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DocClassInteract implements IDocClassInteract {

    private Context context;

    public DocClassInteract(Context context) {
        this.context = context;
    }

    @Override
    public Observable<MessageVO<List<DocClass>>> queryAllDocClasses() {
        return Observable.create(
            (ObservableEmitter<MessageVO<List<DocClass>>> emitter) -> {
                DocClassDao docClassDao = new DocClassDao(context);
                emitter.onNext(new MessageVO<>(docClassDao.queryAllDocClasses()));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<DocClass>> queryDocClassById(int id) {
        return Observable.create(
            (ObservableEmitter<MessageVO<DocClass>> emitter) -> {
                DocClassDao docClassDao = new DocClassDao(context);
                emitter.onNext(new MessageVO<>(docClassDao.queryDocClassById(id)));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<DocClass>> queryDocClassByName(String name) {
        return Observable.create(
            (ObservableEmitter<MessageVO<DocClass>> emitter) -> {
                DocClassDao docClassDao = new DocClassDao(context);
                emitter.onNext(new MessageVO<>(docClassDao.queryDocClassByName(name)));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<DocClass>> queryDefaultDocClass() {
        return Observable.create(
            (ObservableEmitter<MessageVO<DocClass>> emitter) -> {
                DocClassDao docClassDao = new DocClassDao(context);
                emitter.onNext(new MessageVO<>(docClassDao.queryDefaultDocClass()));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<DbStatusType>> insertDocClass(DocClass docClass) {
        return Observable.create(
            (ObservableEmitter<MessageVO<DbStatusType>> emitter) -> {
                DocClassDao docClassDao = new DocClassDao(context);
                emitter.onNext(new MessageVO<>(docClassDao.insertDocClass(docClass)));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<DbStatusType>> updateDocClass(DocClass docClass) {
        return Observable.create(
            (ObservableEmitter<MessageVO<DbStatusType>> emitter) -> {
                DocClassDao docClassDao = new DocClassDao(context);
                emitter.onNext(new MessageVO<>(docClassDao.updateDocClass(docClass)));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<DbStatusType>> deleteDocClass(int id, boolean isToDefault) {
        return Observable.create(
            (ObservableEmitter<MessageVO<DbStatusType>> emitter) -> {
                DocClassDao docClassDao = new DocClassDao(context);
                emitter.onNext(new MessageVO<>(docClassDao.deleteDocClass(id, isToDefault)));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
