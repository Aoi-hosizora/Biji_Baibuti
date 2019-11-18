package com.baibuti.biji.model.dao.daoInterface;

import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.SearchItem;

import java.util.List;

public interface ISearchItemDao {

    // 查
    List<SearchItem> queryAllSearchItems() throws ServerException;
    SearchItem querySearchItemById(int id) throws ServerException;

    // 增删改
    long insertSearchItem(SearchItem searchItem) throws ServerException;
    boolean deleteSearchItem(int id) throws ServerException;
    int deleteSearchItems(List<SearchItem> searchItems) throws ServerException;
}
