package com.baibuti.biji.data.dao.daoInterface;

import com.baibuti.biji.data.model.SearchItem;

import java.util.List;

public interface ISearchItemDao {

    // 查
    List<SearchItem> queryAllSearchItems();
    SearchItem querySearchItemByUrl(String Url);

    // 增删改
    long insertSearchItem(SearchItem searchItem);
    boolean deleteSearchItem(String url);
    int deleteSearchItems(List<SearchItem> searchItems);
}
