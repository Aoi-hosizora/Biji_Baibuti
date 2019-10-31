package com.baibuti.biji.data.dao.net;

import com.baibuti.biji.data.dto.ResponseDTO;
import com.baibuti.biji.service.auth.AuthManager;
import com.baibuti.biji.service.retrofit.RetrofitFactory;
import com.baibuti.biji.data.dao.daoInterface.ISearchItemDao;
import com.baibuti.biji.data.dto.SearchItemDTO;
import com.baibuti.biji.data.po.SearchItem;
import com.baibuti.biji.service.retrofit.ServerErrorHandle;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SearchItemNetDao implements ISearchItemDao {

    @Override
    public List<SearchItem> queryAllSearchItems() throws Exception {
        Observable<ResponseDTO<SearchItemDTO[]>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getAllStars()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<SearchItemDTO[]> response = observable.toFuture().get();
            if (response.getCode() != 200)
                throw ServerErrorHandle.parseErrorMessage(response);

            return Arrays.asList(SearchItemDTO.toSearchItems(response.getData()));
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    // TODO 接口待改
    @Override
    public SearchItem querySearchItemByUrl(String url) throws Exception {
        List<SearchItem> searchItems = queryAllSearchItems();

        for (SearchItem searchItem : searchItems)
            if (searchItem.getUrl().equals(url))
                return searchItem;
        return null;
    }

    @Override
    public long insertSearchItem(SearchItem searchItem) throws Exception {
        Observable<ResponseDTO<SearchItemDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .insertStar(SearchItemDTO.toSearchItemDTO(searchItem))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<SearchItemDTO> response = observable.toFuture().get();
            if (response.getCode() != 200)
                throw ServerErrorHandle.parseErrorMessage(response);

            return 1;
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public boolean deleteSearchItem(String url) throws Exception {
        Observable<ResponseDTO<SearchItemDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteStar(url)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<SearchItemDTO> response = observable.toFuture().get();
            if (response.getCode() != 200)
                throw ServerErrorHandle.parseErrorMessage(response);

            return true;
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    // TODO 接口待改
    @Override
    public int deleteSearchItems(List<SearchItem> searchItems) throws Exception {
        Observable<ResponseDTO<SearchItemDTO[]>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteStars(SearchItemDTO.toSearchItemUrls(searchItems))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<SearchItemDTO[]> response = observable.toFuture().get();
            if (response.getCode() != 200)
                throw ServerErrorHandle.parseErrorMessage(response);

            return response.getData().length;
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
}
