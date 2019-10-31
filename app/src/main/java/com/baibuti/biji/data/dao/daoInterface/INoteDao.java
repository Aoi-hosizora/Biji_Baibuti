package com.baibuti.biji.data.dao.daoInterface;

import com.baibuti.biji.data.po.Note;

import java.util.List;

public interface INoteDao {

    // 查
    default List<Note> queryAllNotes() throws Exception {
        return queryNotesByGroupId(-1);
    }

    List<Note> queryNotesByGroupId(int id) throws Exception;
    Note queryNoteById(int id) throws Exception;

    // 增删改
    long insertNote(Note note) throws Exception;
    boolean updateNote(Note note) throws Exception;
    boolean deleteNote(int id) throws Exception;
}
