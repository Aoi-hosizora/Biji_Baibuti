package com.baibuti.biji.data.dao.net;

import com.baibuti.biji.data.dao.daoInterface.INoteDao;
import com.baibuti.biji.data.model.Note;

import java.util.List;

public class NoteNetDao implements INoteDao {

    @Override
    public List<Note> queryAllNotes() {
        return null;
    }

    @Override
    public List<Note> queryNotesByGroupId(int id) {
        return null;
    }

    @Override
    public Note queryNoteById(int id) {
        return null;
    }

    @Override
    public long insertNote(Note note) {
        return 0;
    }

    @Override
    public boolean updateNote(Note note) {
        return false;
    }

    @Override
    public boolean deleteNote(int id) {
        return false;
    }
}
