package com.baibuti.biji.model.dao.net;

import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dto.OneFieldDTO;
import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.common.auth.AuthManager;
import com.baibuti.biji.common.retrofit.RetrofitFactory;
import com.baibuti.biji.model.dao.daoInterface.ISearchItemDao;
import com.baibuti.biji.model.dto.SearchItemDTO;
import com.baibuti.biji.model.po.SearchItem;
import com.baibuti.biji.common.retrofit.ServerErrorHandle;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SearchItemNetDao implements ISearchItemDao {

    @Override
    public List<SearchItem> queryAllSearchItems() throws ServerException {
        Observable<ResponseDTO<SearchItemDTO[]>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getAllStars()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<SearchItemDTO[]> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return Arrays.asList(SearchItemDTO.toSearchItems(response.getData()));
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    @Override
    public SearchItem querySearchItemById(int id) throws ServerException {
        Observable<ResponseDTO<SearchItemDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getStarById(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<SearchItemDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return response.getData().toSearchItem();
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    /**
     * @param searchItem SUCCESS | FAILED
     */
    @Override
    public DbStatusType insertSearchItem(SearchItem searchItem) throws ServerException {
        Observable<ResponseDTO<SearchItemDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .insertStar(searchItem.getTitle(), searchItem.getUrl(), searchItem.getContent())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<SearchItemDTO> response = observable.toFuture().get();
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
     * @return SUCCESS | FAILED
     */
    @Override
    public DbStatusType deleteSearchItem(int id) throws ServerException {
        Observable<ResponseDTO<SearchItemDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteStar(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<SearchItemDTO> response = observable.toFuture().get();
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
    public int deleteSearchItems(List<SearchItem> searchItems) throws ServerException {

        int[] ids = new int[searchItems.size()];
        for (int i = 0; i < ids.length; i++)
            ids[i] = searchItems.get(i).getId();

        Observable<ResponseDTO<OneFieldDTO.CountDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteStars(ids)
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
}
