package com.baibuti.biji.data.dao.daoInterface;

import com.baibuti.biji.data.po.SearchItem;

import java.util.List;

public interface ISearchItemDao {

    // 查
    List<SearchItem> queryAllSearchItems() throws Exception;
    SearchItem querySearchItemByUrl(String url) throws Exception;

    // 增删改
    long insertSearchItem(SearchItem searchItem) throws Exception;
    boolean deleteSearchItem(String url) throws Exception;
    int deleteSearchItems(List<SearchItem> searchItems) throws Exception;
}
