package com.baibuti.biji.data.strategy;

import android.content.Context;

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

    public NoteDao getNoteDao(Context context) {
        return new NoteDao(context);
    }

    public GroupDao getGroupDao(Context context) {
        return new GroupDao(context);
    }

    public SearchItemDao getSearchDao(Context context) {
        return new SearchItemDao(context);
    }
}
