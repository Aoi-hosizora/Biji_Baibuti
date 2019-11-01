package com.baibuti.biji.model.dao.daoInterface;

import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.Note;

import java.util.List;

public interface INoteDao {

    // 查
    default List<Note> queryAllNotes() throws ServerException {
        return queryNotesByGroupId(-1);
    }

    List<Note> queryNotesByGroupId(int id) throws ServerException;
    Note queryNoteById(int id) throws ServerException;

    // 增删改
    long insertNote(Note note) throws ServerException;
    boolean updateNote(Note note) throws ServerException;
    boolean deleteNote(int id) throws ServerException;
}
