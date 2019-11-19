package com.baibuti.biji.model.dao.daoInterface;

import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.Note;

import java.util.List;

public interface INoteDao {

    // 查
    List<Note> queryAllNotes() throws ServerException;
    List<Note> queryNotesByGroupId(int id) throws ServerException;
    Note queryNoteById(int id) throws ServerException;

    // 增删改
    DbStatusType insertNote(Note note) throws ServerException;
    DbStatusType updateNote(Note note) throws ServerException;
    DbStatusType deleteNote(int id) throws ServerException;
    int deleteNotes(int[] id) throws ServerException;
}
