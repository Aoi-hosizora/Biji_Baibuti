package com.baibuti.biji.Data.DB;

import android.content.Context;
import android.util.Log;

import com.baibuti.biji.Data.Models.Group;
import com.baibuti.biji.Data.Models.LogModule;
import com.baibuti.biji.Data.Models.Note;
import com.baibuti.biji.Data.Models.SearchItem;
import com.baibuti.biji.Data.Models.UtLog;
import com.baibuti.biji.Net.Models.RespObj.ServerErrorException;
import com.baibuti.biji.Net.Modules.Auth.AuthMgr;
import com.baibuti.biji.Net.Modules.Log.LogUtil;
import com.baibuti.biji.Net.Modules.Note.GroupUtil;
import com.baibuti.biji.Net.Modules.Note.NoteUtil;
import com.baibuti.biji.Net.Modules.Star.StarUtil;

import java.util.Date;
import java.util.List;

class ServerDbUpdateHelper {

    /**
     * 返回是否本地比服务器日志新，以本地新为主
     * @param logModule
     * @return
     */
    static boolean isLocalNewer(Context context, LogModule logModule) {
        if (!(AuthMgr.getInstance().isLogin()))
            return true;

        UtLogDao utLogDao = new UtLogDao(context);

        Date local = utLogDao.getLog(logModule).getUpdateTime();
        try {
            Date server = LogUtil.getOneLog(logModule).getUpdateTime();

            Log.e("", "isLocalNewer: " + local + server + ", after " + local.after(server) + ", before" + local.before(server));
            return local.after(server);
        }
        catch (ServerErrorException ex) {
            ex.printStackTrace();
            return true;
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return true;
        }
    }

    /**
     * 返回是否本地比服务器日志旧
     * @param logModule
     * @return
     */
    static boolean isLocalOlder(Context context, LogModule logModule) {
        if (!(AuthMgr.getInstance().isLogin()))
            return false;

        UtLogDao utLogDao = new UtLogDao(context);

        Date local = utLogDao.getLog(logModule).getUpdateTime();
        try {
            Date server = LogUtil.getOneLog(logModule).getUpdateTime();
            return local.before(server);
        }
        catch (ServerErrorException ex) {
            ex.printStackTrace();
            return false;
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return false;
        }
    }


    /**
     * 从服务器更新数据
     * @param context 数据库上下文
     * @param logModule
     */
    static void pullData(Context context, LogModule logModule) {
        if (!(AuthMgr.getInstance().isLogin()))
            return;

        Log.e("", "pullData: " + logModule.toString());

        // 更新数据
        switch (logModule) {
            case Mod_Note: {
                NoteDao noteDao = new NoteDao(context);
                List<Note> allNotes = noteDao.queryAllNotesFromGroupId(-1, false);
                for (Note note : allNotes)
                    noteDao.deleteNote(note.getId(), false);

                try {
                    Note[] notes = NoteUtil.getAllNotes();
                    GroupDao groupDao = new GroupDao(context);
                    if (notes != null)
                        for (Note note : notes) {
                            note.setGroupLabel(groupDao.queryGroupById(note.getGroupLabel().getId(), false), false);
                            noteDao.insertNote(note, note.getId()); // not check log
                            pullLog(context, logModule);
                        }
                }
                catch (ServerErrorException ex) {
                    ex.printStackTrace();
                }
            }
            break;
            case Mod_Group: {
                // 先Group后Note
                GroupDao groupDao = new GroupDao(context);
                List<Group> groups = groupDao.queryGroupAll(false);
                for (Group group : groups)
                    try {
                        // 不会抛出
                        groupDao.deleteGroup(group.getId(), false);
                    }
                    catch (EditDefaultGroupException ex) {
                        ex.printStackTrace();
                    }

                try {
                    Group[] groups1 = GroupUtil.getAllGroups();
                    if (groups1 != null)
                        for (Group group : groups1) {
                            groupDao.insertGroup(group, group.getId()); // not check log
                            pullLog(context, logModule);
                        }
                }
                catch (ServerErrorException ex) {
                    ex.printStackTrace();
                }
            }
            break;
            case Mod_Star: {
                SearchItemDao searchItemDao = new SearchItemDao(context);
                searchItemDao.deleteStarSearchItems(searchItemDao.queryAllStarSearchItems(false), false);
                try {
                    SearchItem[] searchItems = StarUtil.getAllStars();
                    for (SearchItem searchItem : searchItems) {
                        Log.e("", "pullData: " + searchItem.getUrl() );
                        searchItemDao.insertStarSearchItem(searchItem, false);

                        pullLog(context, logModule);
                    }
                } catch (ServerErrorException ex) {
                    ex.printStackTrace();
                }
            }
            break;
            case Mod_File: { }
            break;
            case Mod_Schedule: { }
            break;

        }
    }

    static void pullLog(Context context, LogModule logModule) {
        try {
            UtLog utLog = LogUtil.getOneLog(logModule);
            UtLogDao utLogDao = new UtLogDao(context);
            utLogDao.updateLog(utLog); // 更新本地为服务器日志
        }
        catch (ServerErrorException ex) {
            ex.printStackTrace();
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 更新服务器数据，异步
     * @param context 数据库上下文
     * @param logModule
     */
    static void pushData(Context context, LogModule logModule) {
        if (!(AuthMgr.getInstance().isLogin()))
            return;

        Log.e("", "pushData: " + logModule.toString());

        switch (logModule) {
            case Mod_Note: {

                NoteDao noteDao = new NoteDao(context);
                List<Note> notes = noteDao.queryAllNotesFromGroupId(-1, false);
                try {
                    NoteUtil.pushNotes(notes.toArray(new Note[0]));
                    pushLog(context, logModule);
                }
                catch (ServerErrorException ex) {
                    ex.printStackTrace();
                }
            }
            break;
            case Mod_Group: {
                GroupDao groupDao = new GroupDao(context);
                List<Group> groups = groupDao.queryGroupAll(false);
                try {
                    GroupUtil.pushGroups(groups.toArray(new Group[0]));
                    pushLog(context, logModule);
                }
                catch (ServerErrorException ex) {
                    ex.printStackTrace();
                }
            }
            break;
            case Mod_Star: {

                SearchItemDao searchItemDao = new SearchItemDao(context);
                List<SearchItem> searchItems = searchItemDao.queryAllStarSearchItems(false);
                try {
                    StarUtil.pushStar(searchItems.toArray(new SearchItem[0]));
                    pushLog(context, logModule);
                }
                catch (ServerErrorException ex) {
                    ex.printStackTrace();
                }
            }
            break;
            case Mod_File: { }
            break;
            case Mod_Schedule: { }
            break;
        }
    }

    static void pushLog(Context context, LogModule logModule) {
        try {
            UtLogDao utLogDao = new UtLogDao(context);

            Log.e("", "pushLog: " + utLogDao.getLog(logModule).getModule());

            LogUtil.updateModuleLog(utLogDao.getLog(logModule)); // 更新服务器为本地日志
        }
        catch (ServerErrorException ex) {
            ex.printStackTrace();
        }
    }
}
