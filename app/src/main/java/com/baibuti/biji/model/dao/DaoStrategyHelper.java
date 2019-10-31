package com.baibuti.biji.model.dao;

import android.content.Context;

import com.baibuti.biji.model.dao.daoInterface.*;
import com.baibuti.biji.model.dao.db.*;
import com.baibuti.biji.model.dao.net.GroupNetDao;
import com.baibuti.biji.model.dao.net.NoteNetDao;
import com.baibuti.biji.model.dao.net.SearchItemNetDao;
import com.baibuti.biji.service.auth.AuthManager;

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
        if (!AuthManager.getInstance().isLogin())
            return new NoteDao(context);
        else
            return new NoteNetDao();
    }

    public IGroupDao getGroupDao(Context context) {
        if (!AuthManager.getInstance().isLogin())
            return new GroupDao(context);
        else
            return new GroupNetDao();
    }

    public ISearchItemDao getSearchDao(Context context) {
        if (!AuthManager.getInstance().isLogin())
            return new SearchItemDao(context);
        else
            return new SearchItemNetDao();
    }
}
