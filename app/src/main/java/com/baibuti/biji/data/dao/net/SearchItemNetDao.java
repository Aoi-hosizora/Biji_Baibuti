package com.baibuti.biji.data.dao.net;

import com.baibuti.biji.data.dao.RetrofitFactory;
import com.baibuti.biji.data.dao.daoInterface.ISearchItemDao;
import com.baibuti.biji.data.dto.SearchItemDTO;
import com.baibuti.biji.data.model.SearchItem;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SearchItemNetDao implements ISearchItemDao {

    @Override
    public List<SearchItem> queryAllSearchItems() {
        Observable<SearchItem[]> observable = RetrofitFactory.getInstance()
            .createRequest(RetrofitFactory.getHeader("", ""))
            .getAllStars()
            .subscribeOn(Schedulers.io())
            .map(SearchItemDTO::toSearchItems)
            .observeOn(AndroidSchedulers.mainThread());

        try {
            return Arrays.asList(observable.toFuture().get());
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // TODO 接口待改
    @Override
    public SearchItem querySearchItemByUrl(String url) {
        List<SearchItem> searchItems = queryAllSearchItems();

        for (SearchItem searchItem : searchItems)
            if (searchItem.getUrl().equals(url))
                return searchItem;
        return null;
    }

    @Override
    public long insertSearchItem(SearchItem searchItem) {
        Observable<SearchItem> observable = RetrofitFactory.getInstance()
            .createRequest(RetrofitFactory.getHeader("", ""))
            .insertStar(SearchItemDTO.toSearchItemDTO(searchItem))
            .subscribeOn(Schedulers.io())
            .map(SearchItemDTO::toSearchItem)
            .observeOn(AndroidSchedulers.mainThread());

        try {
            observable.toFuture().get();
            return 1;
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean deleteSearchItem(String url) {
        Observable<SearchItem> observable = RetrofitFactory.getInstance()
            .createRequest(RetrofitFactory.getHeader("", ""))
            .deleteStar(url)
            .subscribeOn(Schedulers.io())
            .map(SearchItemDTO::toSearchItem)
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

    // TODO 接口待改
    @Override
    public int deleteSearchItems(List<SearchItem> searchItems) {
        Observable<SearchItem[]> observable = RetrofitFactory.getInstance()
            .createRequest(RetrofitFactory.getHeader("", ""))
            .deleteStars(SearchItemDTO.toSearchItemUrls(searchItems))
            .subscribeOn(Schedulers.io())
            .map(SearchItemDTO::toSearchItems)
            .observeOn(AndroidSchedulers.mainThread());

        try {
            SearchItem[] ret_items = observable.toFuture().get();
            return ret_items.length;
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            return 0;
        }
    }
}
