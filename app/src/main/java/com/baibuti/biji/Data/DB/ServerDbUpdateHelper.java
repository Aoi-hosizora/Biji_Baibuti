package com.baibuti.biji.Data.DB;

import android.content.Context;

import com.baibuti.biji.Data.Models.LogModule;
import com.baibuti.biji.Data.Models.Note;
import com.baibuti.biji.Net.Models.RespObj.ServerErrorException;
import com.baibuti.biji.Net.Modules.Log.LogUtil;
import com.baibuti.biji.Net.Modules.Note.NoteUtil;

import java.util.Date;
import java.util.List;

public class ServerDbUpdateHelper {

    /**
     * 返回是否本地比服务器日志新
     * @param logModule
     * @return
     */
    public static boolean isLocalNewer(Context context, LogModule logModule) {
        UtLogDao utLogDao = new UtLogDao(context);

        Date local = utLogDao.getLog(logModule).getUpdateTime();
        Date server;
        try {
            server = LogUtil.getOneLog(logModule).getUpdateTime();
        }
        catch (ServerErrorException ex) {
            ex.printStackTrace();
            return true;
        }
        return local.after(server);
    }


    /**
     * 从服务器更新数据
     */
    void updateNoteFromServer(Context context, LogModule logModule) {
        switch (logModule) {
            case Mod_Note: {

                NoteDao noteDao = new NoteDao(context);
                noteDao.deleteNote(noteDao.queryNotesAll());

                try {
                    Note[] notes = NoteUtil.getAllNotes();

                    for (Note note : notes) {
                        noteDao.insertNote(note, note.getId());
                    }

                    noteDao.updateLog();
                }
                catch (ServerErrorException ex) {
                    ex.printStackTrace();
                }
                catch (NullPointerException ex) {
                    ex.printStackTrace();
                }
            }
            break;
        }

    }

    /**
     * 更新服务器数据
     */
    void updateNoteToServer(Context context, LogModule logModule) {
        NoteDao noteDao = new NoteDao(context);
        try {
            List<Note> notes = noteDao.queryNotesAll();
            NoteUtil.updateNotes(notes.toArray(new Note[0]));
        }
        catch (ServerErrorException ex) {
            ex.printStackTrace();
        }
    }

}
