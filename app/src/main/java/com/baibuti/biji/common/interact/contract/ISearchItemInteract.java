package com.baibuti.biji.common.interact.contract;

import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.po.SearchItem;
import com.baibuti.biji.model.vo.MessageVO;

import java.util.List;

import io.reactivex.Observable;

public interface ISearchItemInteract {

    // 查
    Observable<MessageVO<List<SearchItem>>> queryAllSearchItems();
    Observable<MessageVO<SearchItem>> querySearchItemById(int id);

    // 增删改
    Observable<MessageVO<Boolean>> insertSearchItem(SearchItem searchItem);
    Observable<MessageVO<Boolean>> deleteSearchItem(int id);
    Observable<MessageVO<Integer>> deleteSearchItems(List<SearchItem> searchItems);
}
