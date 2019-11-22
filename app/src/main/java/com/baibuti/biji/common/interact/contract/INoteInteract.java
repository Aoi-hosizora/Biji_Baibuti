package com.baibuti.biji.common.interact.contract;

import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.po.Note;
import com.baibuti.biji.model.vo.MessageVO;

import java.util.List;

import io.reactivex.Observable;

public interface INoteInteract {

    // 查
    Observable<MessageVO<List<Note>>> queryAllNotes();
    Observable<MessageVO<List<Note>>> queryNotesByGroupId(int id);
    Observable<MessageVO<Note>> queryNoteById(int id);

    // 增删改
    Observable<MessageVO<Boolean>> insertNote(Note note) ;
    Observable<MessageVO<Boolean>> updateNote(Note note);
    Observable<MessageVO<Boolean>> deleteNote(int id);
    Observable<MessageVO<Integer>> deleteNotes(int[] id);
}
