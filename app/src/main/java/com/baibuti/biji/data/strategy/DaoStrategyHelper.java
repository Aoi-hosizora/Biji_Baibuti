package com.baibuti.biji.data.strategy;

import android.content.Context;

import com.baibuti.biji.data.dao.daoInterface.*;
import com.baibuti.biji.data.dao.db.*;

public class DaoStrategyHelper {

    private DaoStrategyHelper() { }

    private static DaoStrategyHelper Instance;

    public static DaoStrategyHelper getInstance() {
        if (Instance == null) {
            Instance = new DaoStrategyHelper();
        }
        return Instance;
    }

    public INoteDao getNoteDao(Context context) {
        return new NoteDao(context);
    }

    public IGroupDao getGroupDao(Context context) {
        return new GroupDao(context);
    }

    public ISearchItemDao getSearchDao(Context context) {
        return new SearchItemDao(context);
    }
}
