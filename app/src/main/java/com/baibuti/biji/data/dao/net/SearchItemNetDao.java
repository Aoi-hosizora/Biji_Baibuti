package com.baibuti.biji.data.dao.net;

import com.baibuti.biji.data.dao.daoInterface.ISearchItemDao;
import com.baibuti.biji.data.model.SearchItem;

import java.util.List;

public class SearchItemNetDao implements ISearchItemDao {

    @Override
    public List<SearchItem> queryAllSearchItems() {
        return null;
    }

    @Override
    public SearchItem querySearchItemByUrl(String Url) {
        return null;
    }

    @Override
    public long insertSearchItem(SearchItem searchItem) {
        return 0;
    }

    @Override
    public boolean deleteSearchItem(String url) {
        return false;
    }

    @Override
    public int deleteSearchItems(List<SearchItem> searchItems) {
        return 0;
    }
}
