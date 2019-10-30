package com.baibuti.biji.data.dao.db;

import android.content.Context;
import android.util.Log;

import com.baibuti.biji.data.model.Document;
import com.baibuti.biji.data.model.FileClass;
import com.baibuti.biji.data.model.Group;
import com.baibuti.biji.data.model.LogModule;
import com.baibuti.biji.data.model.Note;
import com.baibuti.biji.data.model.SearchItem;
import com.baibuti.biji.data.model.UtLog;
import com.baibuti.biji.iGlobal.IPushCallBack;
import com.baibuti.biji.net.model.respObj.ServerErrorException;
import com.baibuti.biji.net.module.auth.AuthMgr;
import com.baibuti.biji.net.module.file.DocumentUtil;
import com.baibuti.biji.net.module.file.FileClassUtil;
import com.baibuti.biji.net.module.log.LogUtil;
import com.baibuti.biji.net.module.note.GroupUtil;
import com.baibuti.biji.net.module.note.NoteUtil;
import com.baibuti.biji.net.module.schedule.ScheduleUtil;
import com.baibuti.biji.net.module.star.StarUtil;

import java.util.Date;
import java.util.List;

@Deprecated
class ServerDbUpdateHelper {

    /**
     * 返回是否本地比服务器日志新，以本地新为主
     * @param logModule
     * @return
     */
    @Deprecated
    static boolean isLocalNewer(Context context, LogModule logModule) {
        if (!(AuthMgr.getInstance().isLogin()))
            return true;

        UtLogDao utLogDao = new UtLogDao(context);

        Date local = utLogDao.getLog(logModule).getUpdateTime();
        try {
            Date server = LogUtil.getOneLog(logModule).getUpdateTime();

            Log.e("测试", "isLocalNewer: " + local + server + ", after " + local.after(server) + ", before" + local.before(server));
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
    @Deprecated
    static boolean isLocalOlder(Context context, LogModule logModule) {
        if (!(AuthMgr.getInstance().isLogin()))
            return false;

        UtLogDao utLogDao = new UtLogDao(context);

        Date local = utLogDao.getLog(logModule).getUpdateTime();
        try {
            Date server = LogUtil.getOneLog(logModule).getUpdateTime();
            Log.e("测试", "isLocalOlder: " + local + server + ", after " + local.after(server) + ", before" + local.before(server));
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
     * 同步
     * @param context 数据库上下文
     * @param logModule
     */
    @Deprecated
    static void pullData(Context context, LogModule logModule) {
        if (!(AuthMgr.getInstance().isLogin()))
            return;

        Log.e("测试", "pullData: " + logModule.toString());

        // 更新数据
        switch (logModule) {
            case Mod_Note: {
                NoteDao noteDao = new NoteDao(context);
                List<Note> allNotes = noteDao.queryNotesByGroupId(-1, false);
                for (Note note : allNotes)
                    noteDao.deleteNote(note.getId(), false);

                try {
                    Note[] notes = NoteUtil.getAllNotes();
                    GroupDao groupDao = new GroupDao(context);
                    if (notes != null)
                        for (Note note : notes) {
                            note.setGroup(groupDao.queryGroupById(note.getGroup().getId(), false), false);
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
            case Mod_FileClass: {
                // 先FileClass后Document
                FileClassDao fileClassDao = new FileClassDao(context);
                List<FileClass> fileClasses = fileClassDao.queryFileClassAll(false);
                for (FileClass fileClass : fileClasses)
                    try {
                        // 不会抛出
                        fileClassDao.deleteFileClass(fileClass.getId(), false);
                    }
                    catch (EditDefaultFileClassException ex) {
                        ex.printStackTrace();
                    }

                try {
                    FileClass[] fileClasses1 = FileClassUtil.getAllFileClasses();
                    if (fileClasses1 != null) {
                        for (FileClass fileClass : fileClasses1) {
                            fileClassDao.insertFileClass(fileClass, fileClass.getId()); // not check log
                        }
                        pullLog(context, logModule);
                    }
                }
                catch (ServerErrorException ex) {
                    ex.printStackTrace();
                }
            }
            break;
            case Mod_Document: {
                DocumentDao documentDao = new DocumentDao(context);
                documentDao.deleteDocument();

                try {
                    Document[] documents = DocumentUtil.getAllFiles("");
                    for(Document document: documents){
                        Log.e("测试", "pull: " + document.getId() + ' ' +
                                document.getDocumentName() + ' ' +
                                document.getDocumentClassName() + '\n');
                        documentDao.insertDocument(document, document.getId());
                    }
                    pullLog(context, logModule);
                }
                catch (ServerErrorException ex) {
                    ex.printStackTrace();
                }
            }
            break;
            case Mod_Schedule: {
                ScheduleDao scheduleDao = new ScheduleDao(context);
                scheduleDao.deleteScheduleJson(false);
                try {
                    String scheduleJson = ScheduleUtil.getSchedule();
                    scheduleDao.insertScheduleJson(scheduleJson, false);
                    pullLog(context, logModule);
                } catch (ServerErrorException ex) {
                    ex.printStackTrace();
                }
            }
            break;

        }
    }

    /**
     * 从服务器获取日志
     * 同步
     * @param context
     * @param logModule
     */
    @Deprecated
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
     * 更新服务器数据
     * 异步
     * @param context 数据库上下文
     * @param logModule
     */
    @Deprecated
    static void pushData(Context context, LogModule logModule) {
        if (!(AuthMgr.getInstance().isLogin()))
            return;

        Log.e("测试", "pushData: " + logModule.toString());

        switch (logModule) {
            case Mod_Note: {
                NoteDao noteDao = new NoteDao(context);
                List<Note> notes = noteDao.queryNotesByGroupId(-1, false);
                try {
                    NoteUtil.pushNotesAsync(notes.toArray(new Note[0]), new IPushCallBack() {
                        @Override
                        public void onCallBack() {
                            pushLog(context, logModule);
                        }
                    });
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
                    GroupUtil.pushGroupsAsync(groups.toArray(new Group[0]), new IPushCallBack() {
                        @Override
                        public void onCallBack() {
                            pushLog(context, logModule);
                        }
                    });
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
                    StarUtil.pushStarAsync(searchItems.toArray(new SearchItem[0]), new IPushCallBack() {
                        @Override
                        public void onCallBack() {
                            pushLog(context, logModule);
                        }
                    });
                }
                catch (ServerErrorException ex) {
                    ex.printStackTrace();
                }
            }
            break;
            case Mod_FileClass: {
                FileClassDao fileClassDao = new FileClassDao(context);
                List<FileClass> fileClasses = fileClassDao.queryFileClassAll(false);
                try {
                    FileClassUtil.pushFileClassAsync(fileClasses.toArray(new FileClass[0]), new IPushCallBack() {
                        @Override
                        public void onCallBack() {
                            pushLog(context, logModule);
                        }
                    });
                }
                catch (ServerErrorException ex) {
                    ex.printStackTrace();
                }
            }
            break;
            case Mod_Document: {
                DocumentDao documentDao = new DocumentDao(context);
                List<Document> documents = documentDao.queryDocumentAll();
                try {
                    DocumentUtil.pushDocumentsAsync(documents.toArray(new Document[0]), new IPushCallBack() {
                        @Override
                        public void onCallBack() {
                            pushLog(context, logModule);
                        }
                    });
                }
                catch (ServerErrorException ex) {
                    ex.printStackTrace();
                }
            }
            case Mod_Schedule: {
                ScheduleDao scheduleDao = new ScheduleDao(context);
                String scheduleJson = scheduleDao.queryScheduleJson(false);
                try {
                    ScheduleUtil.pushSchedule(scheduleJson, new IPushCallBack() {
                        @Override
                        public void onCallBack() {
                            pushLog(context, logModule);
                        }
                    });
                }
                catch (ServerErrorException ex) {
                    ex.printStackTrace();
                }
            }
            break;
        }
    }

    /**
     * 更新服务器日志
     * 异步
     * @param context
     * @param logModule
     */
    @Deprecated
    static void pushLog(Context context, LogModule logModule) {
        try {
            UtLogDao utLogDao = new UtLogDao(context);

            Log.e("", "pushLog: " + utLogDao.getLog(logModule).getModule());

            LogUtil.updateModuleLogAsync(utLogDao.getLog(logModule), new IPushCallBack() {
                @Override
                public void onCallBack() { }
            });
            // 更新服务器为本地日志
        }
        catch (ServerErrorException ex) {
            ex.printStackTrace();
        }
    }
}
