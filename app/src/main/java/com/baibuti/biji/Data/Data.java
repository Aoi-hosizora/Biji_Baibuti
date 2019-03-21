package com.baibuti.biji.Data;

import com.baibuti.biji.Activity.MainActivity;
import com.baibuti.biji.db.GroupDao;
import com.baibuti.biji.db.NoteDao;

import java.util.ArrayList;
import java.util.List;

public class Data {
    private Data() {}

    private static Data DataInstance;
    private static NoteDao noteDao;
    private static GroupDao groupDao;
    public static Data getData() {
        if (DataInstance == null) {
            initNote();
//            noteDao = new NoteDao(null);
//            groupDao = new GroupDao(null);
            DataInstance = new Data();
        }

        return DataInstance;
    }

    static List<Note> notelist;

    private static void initNote() {
        if (noteDao == null)
            noteDao = new NoteDao(null);
        notelist = noteDao.queryNotesAll(0);
    }

    public List<Note> getNote() {
        return notelist;
    }

    public void setNoteItem(int index, Note note) {
        notelist.set(index, note);
    }
}
