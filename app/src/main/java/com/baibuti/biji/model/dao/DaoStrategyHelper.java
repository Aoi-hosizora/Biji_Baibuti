package com.baibuti.biji.model.dao;

import android.content.Context;

import com.baibuti.biji.model.dao.daoInterface.*;
import com.baibuti.biji.model.dao.local.*;
import com.baibuti.biji.model.dao.net.DocumentNetDao;
import com.baibuti.biji.model.dao.net.DocClassNetDao;
import com.baibuti.biji.model.dao.net.GroupNetDao;
import com.baibuti.biji.model.dao.net.NoteNetDao;
import com.baibuti.biji.model.dao.net.ScheduleNetDao;
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

    public IScheduleDao getScheduleDao() {
        if (!AuthManager.getInstance().isLogin())
            return new ScheduleDao();
        else
            return new ScheduleNetDao();
    }

    public IDocumentDao getDocumentDao(Context context) {
        if (!AuthManager.getInstance().isLogin())
            return new DocumentDao(context);
        else
            return new DocumentNetDao();
    }

    public IDocClassDao getDocClassDao(Context context) {
        if (!AuthManager.getInstance().isLogin())
            return new DocClassDao(context);
        else
            return new DocClassNetDao();
    }
}
