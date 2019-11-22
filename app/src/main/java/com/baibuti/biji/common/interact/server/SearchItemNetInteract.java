package com.baibuti.biji.common.interact.server;

import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.common.auth.AuthManager;
import com.baibuti.biji.common.retrofit.RetrofitFactory;
import com.baibuti.biji.common.interact.contract.ISearchItemInteract;
import com.baibuti.biji.model.dto.SearchItemDTO;
import com.baibuti.biji.model.po.SearchItem;
import com.baibuti.biji.model.vo.MessageVO;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SearchItemNetInteract implements ISearchItemInteract {

    @Override
    public Observable<MessageVO<List<SearchItem>>> queryAllSearchItems() {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getAllStars()
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<List<SearchItem>>(false, responseDTO.getMessage());
                return new MessageVO<>(Arrays.asList(SearchItemDTO.toSearchItems(responseDTO.getData())));
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<SearchItem>> querySearchItemById(int id) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getStarById(id)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<SearchItem>(false, responseDTO.getMessage());
                return new MessageVO<>(responseDTO.getData().toSearchItem());
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @param searchItem SUCCESS | FAILED
     */
    @Override
    public Observable<MessageVO<Boolean>> insertSearchItem(SearchItem searchItem) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .insertStar(searchItem.getTitle(), searchItem.getUrl(), searchItem.getContent())
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Boolean>(false, responseDTO.getMessage());
                searchItem.setId(responseDTO.getData().getId());
                return new MessageVO<>(true);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @return SUCCESS | FAILED
     */
    @Override
    public Observable<MessageVO<Boolean>> deleteSearchItem(int id) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteStar(id)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Boolean>(false, responseDTO.getMessage());
                return new MessageVO<>(true);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Integer>> deleteSearchItems(List<SearchItem> searchItems) {
        int[] ids = new int[searchItems.size()];
        for (int i = 0; i < ids.length; i++)
            ids[i] = searchItems.get(i).getId();

        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteStars(ids)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Integer>(false, responseDTO.getMessage());
                return new MessageVO<>(responseDTO.getData().getCount());
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
