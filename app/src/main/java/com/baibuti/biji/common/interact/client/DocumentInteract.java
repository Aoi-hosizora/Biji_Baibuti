package com.baibuti.biji.common.interact.client;

import android.content.Context;

import com.baibuti.biji.common.interact.contract.IDocumentInteract;
import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dao.local.DocumentDao;
import com.baibuti.biji.model.po.Document;
import com.baibuti.biji.model.vo.MessageVO;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DocumentInteract implements IDocumentInteract {

    private Context context;

    public DocumentInteract(Context context) {
        this.context = context;
    }

    @Override
    public Observable<MessageVO<List<Document>>> queryAllDocuments() {
        return Observable.create(
            (ObservableEmitter<MessageVO<List<Document>>> emitter) -> {
                DocumentDao documentDao = new DocumentDao(context);
                emitter.onNext(new MessageVO<>(documentDao.queryAllDocuments()));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<List<Document>>> queryDocumentByClassId(int cid) {
        return Observable.create(
            (ObservableEmitter<MessageVO<List<Document>>> emitter) -> {
                DocumentDao documentDao = new DocumentDao(context);
                emitter.onNext(new MessageVO<>(documentDao.queryDocumentByClassId(cid)));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Document>> queryDocumentById(int id) {
        return Observable.create(
            (ObservableEmitter<MessageVO<Document>> emitter) -> {
                DocumentDao documentDao = new DocumentDao(context);
                emitter.onNext(new MessageVO<>(documentDao.queryDocumentById(id)));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Boolean>> insertDocument(Document document) {
        return Observable.create(
            (ObservableEmitter<MessageVO<Boolean>> emitter) -> {
                DocumentDao documentDao = new DocumentDao(context);
                DbStatusType status = documentDao.insertDocument(document);
                if (status == DbStatusType.FAILED)
                    emitter.onNext(new MessageVO<>(false, "Document Insert Failed"));
                else
                    emitter.onNext(new MessageVO<>(true));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Boolean>> updateDocument(Document document) {
        return Observable.create(
            (ObservableEmitter<MessageVO<Boolean>> emitter) -> {
                DocumentDao documentDao = new DocumentDao(context);
                DbStatusType status = documentDao.updateDocument(document);
                if (status == DbStatusType.FAILED)
                    emitter.onNext(new MessageVO<>(false, "Document Update Failed"));
                else
                    emitter.onNext(new MessageVO<>(true));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Boolean>> deleteDocument(int id) {
        return Observable.create(
            (ObservableEmitter<MessageVO<Boolean>> emitter) -> {
                DocumentDao documentDao = new DocumentDao(context);
                DbStatusType status = documentDao.deleteDocument(id);
                if (status == DbStatusType.FAILED)
                    emitter.onNext(new MessageVO<>(false, "Document Delete Failed"));
                else
                    emitter.onNext(new MessageVO<>(true));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
