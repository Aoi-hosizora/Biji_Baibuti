package com.baibuti.biji.Data.DB;

import android.content.Context;
import android.util.Log;

import com.baibuti.biji.Data.Models.LogModule;
import com.baibuti.biji.Data.Models.Note;
import com.baibuti.biji.Data.Models.SearchItem;
import com.baibuti.biji.Data.Models.UtLog;
import com.baibuti.biji.Net.Models.RespObj.ServerErrorException;
import com.baibuti.biji.Net.Modules.Auth.AuthMgr;
import com.baibuti.biji.Net.Modules.Log.LogUtil;
import com.baibuti.biji.Net.Modules.Note.NoteUtil;
import com.baibuti.biji.Net.Modules.Star.StarUtil;

import java.util.Date;
import java.util.List;

public class ServerDbUpdateHelper {

    /**
     * 返回是否本地比服务器日志新，以本地新为主
     * @param logModule
     * @return
     */
    public static boolean isLocalNewer(Context context, LogModule logModule) {
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
    public static boolean isLocalOlder(Context context, LogModule logModule) {
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
    public static void pullData(Context context, LogModule logModule) {
        if (!(AuthMgr.getInstance().isLogin()))
            return;

        Log.e("", "pullData: " + logModule.toString());

        // 更新数据
        switch (logModule) {
            case Mod_Note: {
                // NoteDao noteDao = new NoteDao(context);
                // noteDao.deleteNote(noteDao.queryNotesAll());
                // try {
                //     Note[] notes = NoteUtil.getAllNotes();
                //     for (Note note : notes) {
                //         noteDao.insertNote(note, note.getId());
                //     }
                // } catch (ServerErrorException ex) {
                //     ex.printStackTrace();
                // }
            }
            break;
            case Mod_Group: { }
            break;
            case Mod_Star: {
                SearchItemDao searchItemDao = new SearchItemDao(context);
                searchItemDao.deleteStarSearchItems(searchItemDao.queryAllStarSearchItems(false), false);
                try {
                    SearchItem[] searchItems = StarUtil.getAllStars();
                    for (SearchItem searchItem : searchItems) {
                        Log.e("", "pullData: " + searchItem.getUrl() );
                        searchItemDao.insertStarSearchItem(searchItem, false);
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

        // 更新 Log
        pullLog(context, logModule);
    }

    public static void pullLog(Context context, LogModule logModule) {
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
    public static void pushData(Context context, LogModule logModule) {
        if (!(AuthMgr.getInstance().isLogin()))
            return;

        Log.e("", "pushData: " + logModule.toString());

        switch (logModule) {
            case Mod_Note: {
                // NoteDao noteDao = new NoteDao(context);
//
                // List<Note> notes = noteDao.queryNotesAll();
                // // TODO
//
                // try {
                //     Note[] notes1 = NoteUtil.getAllNotes();
                //     for (Note note1 : notes1)
                //         try {
                //             NoteUtil.deleteNote(note1);
                //         }
                //         catch (ServerErrorException ex) {
                //             ex.printStackTrace();
                //         }
                // }
                // catch (ServerErrorException ex) {
                //     ex.printStackTrace();
                // }
                // for (Note note : notes)
                //     try {
                //         NoteUtil.insertNote(note);
                //     }
                //     catch (ServerErrorException ex) {
                //         ex.printStackTrace();
                //     }
            }
            break;
            case Mod_Group: { }
            break;
            case Mod_Star: {

                SearchItemDao searchItemDao = new SearchItemDao(context);

                List<SearchItem> searchItems = searchItemDao.queryAllStarSearchItems(false);
                // TODO

                try {
                    SearchItem[] searchItems1 = StarUtil.getAllStars();
                    for (SearchItem searchItem : searchItems1) {
                        Log.e("", "pushData: " + searchItem.getTitle() );
                        try {
                            StarUtil.deleteStar(searchItem);
                        } catch (ServerErrorException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                catch (ServerErrorException ex) {
                    Log.e("", "pushData: ServerErrorException getAllStars error"  );
                    ex.printStackTrace();
                }
                catch (NullPointerException ex) {
                    Log.e("", "pushData: NullPointerException getAllStars error"  );
                    ex.printStackTrace();
                }

                for (SearchItem searchItem : searchItems)
                    try {
                        StarUtil.insertStar(searchItem);
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

        pushLog(context, logModule);
    }

    public static void pushLog(Context context, LogModule logModule) {
        try {
            UtLogDao utLogDao = new UtLogDao(context);
            LogUtil.updateModuleLog(utLogDao.getLog(logModule)); // 更新服务器为本地日志
        }
        catch (ServerErrorException ex) {
            ex.printStackTrace();
        }
    }
}
