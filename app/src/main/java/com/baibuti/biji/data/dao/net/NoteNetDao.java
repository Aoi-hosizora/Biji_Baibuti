package com.baibuti.biji.data.dao.net;

import com.baibuti.biji.data.dao.daoInterface.INoteDao;
import com.baibuti.biji.data.model.Note;

import java.util.List;

public class NoteNetDao implements INoteDao {

    @Override
    public List<Note> queryAllNotes() throws Exception {
        return null;
    }

    @Override
    public List<Note> queryNotesByGroupId(int id) throws Exception {
        return null;
    }

    @Override
    public Note queryNoteById(int id) throws Exception {
        return null;
    }

    @Override
    public long insertNote(Note note) throws Exception {
        return 0;
    }

    @Override
    public boolean updateNote(Note note) throws Exception {
        return false;
    }

    @Override
    public boolean deleteNote(int id) throws Exception {
        return false;
    }
}
