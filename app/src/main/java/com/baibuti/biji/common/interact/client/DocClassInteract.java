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
    public Observable<MessageVO<Boolean>> insertDocClass(DocClass docClass) {
        return Observable.create(
            (ObservableEmitter<MessageVO<Boolean>> emitter) -> {
                DocClassDao docClassDao = new DocClassDao(context);
                DbStatusType status = docClassDao.insertDocClass(docClass);
                if (status == DbStatusType.DUPLICATED)
                    emitter.onNext(new MessageVO<>(false, "Document Class Name Duplicate"));
                else if (status == DbStatusType.FAILED)
                    emitter.onNext(new MessageVO<>(false, "Document Class Insert Failed"));
                else
                    emitter.onNext(new MessageVO<>(true));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Boolean>> updateDocClass(DocClass docClass) {
        return Observable.create(
            (ObservableEmitter<MessageVO<Boolean>> emitter) -> {
                DocClassDao docClassDao = new DocClassDao(context);
                DbStatusType status = docClassDao.updateDocClass(docClass);
                if (status == DbStatusType.DUPLICATED)
                    emitter.onNext(new MessageVO<>(false, "Document Class Name Duplicate"));
                else if (status == DbStatusType.DEFAULT)
                    emitter.onNext(new MessageVO<>(false, "Could Not Update Default Document Class"));
                else if (status == DbStatusType.FAILED)
                    emitter.onNext(new MessageVO<>(false, "Document Class Update Failed"));
                else
                    emitter.onNext(new MessageVO<>(true));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Boolean>> deleteDocClass(int id, boolean isToDefault) {
        return Observable.create(
            (ObservableEmitter<MessageVO<Boolean>> emitter) -> {
                DocClassDao docClassDao = new DocClassDao(context);
                DbStatusType status = docClassDao.deleteDocClass(id, isToDefault);
                if (status == DbStatusType.DEFAULT)
                    emitter.onNext(new MessageVO<>(false, "Could Not Delete Default Document Class"));
                else if (status == DbStatusType.FAILED)
                    emitter.onNext(new MessageVO<>(false, "Document Class Delete Failed"));
                else
                    emitter.onNext(new MessageVO<>(true));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
