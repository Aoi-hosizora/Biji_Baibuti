package com.baibuti.biji.data.dao.daoInterface;

import com.baibuti.biji.data.model.Note;

import java.util.List;

public interface INoteDao {

    // 查
    default List<Note> queryAllNotes() {
        return queryNotesByGroupId(-1);
    }

    List<Note> queryNotesByGroupId(int id);
    Note queryNoteById(int id);

    // 增删改
    long insertNote(Note note);
    boolean updateNote(Note note);
    boolean deleteNote(int id);
}
