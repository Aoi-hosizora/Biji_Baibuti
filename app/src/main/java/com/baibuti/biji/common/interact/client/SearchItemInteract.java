package com.baibuti.biji.common.interact.client;

import android.content.Context;

import com.baibuti.biji.common.interact.contract.ISearchItemInteract;
import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dao.local.SearchItemDao;
import com.baibuti.biji.model.po.SearchItem;
import com.baibuti.biji.model.vo.MessageVO;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SearchItemInteract implements ISearchItemInteract {

    private Context context;

    public SearchItemInteract(Context context) {
        this.context = context;
    }

    @Override
    public Observable<MessageVO<List<SearchItem>>> queryAllSearchItems() {
        return Observable.create(
            (ObservableEmitter<MessageVO<List<SearchItem>>> emitter) -> {
                SearchItemDao searchItemDao = new SearchItemDao(context);
                emitter.onNext(new MessageVO<>(searchItemDao.queryAllSearchItems()));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<SearchItem>> querySearchItemById(int id) {
        return Observable.create(
            (ObservableEmitter<MessageVO<SearchItem>> emitter) -> {
                SearchItemDao searchItemDao = new SearchItemDao(context);
                emitter.onNext(new MessageVO<>(searchItemDao.querySearchItemById(id)));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Boolean>> insertSearchItem(SearchItem searchItem) {
        return Observable.create(
            (ObservableEmitter<MessageVO<Boolean>> emitter) -> {
                SearchItemDao searchItemDao = new SearchItemDao(context);
                DbStatusType status = searchItemDao.insertSearchItem(searchItem);
                if (status == DbStatusType.FAILED)
                    emitter.onNext(new MessageVO<>(false, "StarItem Insert Failed"));
                else
                    emitter.onNext(new MessageVO<>(true));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Boolean>> deleteSearchItem(int id) {
        return Observable.create(
            (ObservableEmitter<MessageVO<Boolean>> emitter) -> {
                SearchItemDao searchItemDao = new SearchItemDao(context);
                DbStatusType status = searchItemDao.deleteSearchItem(id);
                if (status == DbStatusType.FAILED)
                    emitter.onNext(new MessageVO<>(false, "StarItem Delete Failed"));
                else
                    emitter.onNext(new MessageVO<>(true));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Integer>> deleteSearchItems(List<SearchItem> searchItems) {
        return Observable.create(
            (ObservableEmitter<MessageVO<Integer>> emitter) -> {
                SearchItemDao searchItemDao = new SearchItemDao(context);
                emitter.onNext(new MessageVO<>(searchItemDao.deleteSearchItems(searchItems)));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
