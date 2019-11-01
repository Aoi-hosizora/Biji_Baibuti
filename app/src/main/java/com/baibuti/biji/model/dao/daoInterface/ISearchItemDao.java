package com.baibuti.biji.model.dao.daoInterface;

import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.SearchItem;

import java.util.List;

public interface ISearchItemDao {

    // 查
    List<SearchItem> queryAllSearchItems() throws ServerException;
    SearchItem querySearchItemByUrl(String url) throws ServerException;

    // 增删改
    long insertSearchItem(SearchItem searchItem) throws ServerException;
    boolean deleteSearchItem(String url) throws ServerException;
    int deleteSearchItems(List<SearchItem> searchItems) throws ServerException;
}
