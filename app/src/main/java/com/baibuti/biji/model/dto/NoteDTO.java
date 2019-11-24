package com.baibuti.biji.model.dto;

import com.baibuti.biji.model.po.Note;
import com.baibuti.biji.util.otherUtil.DateColorUtil;

import java.io.Serializable;

import lombok.Data;

@Data
public class NoteDTO implements Serializable {

    private int id;
    private String title;
    private String content;
    private GroupDTO group; // <<<
    private String create_time; // 不能用 Date，GSON 无法解析
    private String update_time;

    private NoteDTO(int id, String title, String content, GroupDTO group, String create_time, String update_time) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.group = group;
        this.create_time = create_time;
        this.update_time = update_time;
    }

    /**
      * NoteDTO -> Note
      */
     public Note toNote() {
         return new Note(id, title, content, group.toGroup(), DateColorUtil.Str2Date(create_time), DateColorUtil.Str2Date(update_time));
     }

    // /**
    //  * Note -> NoteDTO
    //  */
    // public static NoteDTO toNoteDTO(Note note) {
    //     if (note == null) return null;
    //     return
    //         new NoteDTO(note.getId(), note.getTitle(), note.getContent(), GroupDTO.toGroupDTO(note.getGroup()), note.getCreateTime(), note.getUpdateTime());
    // }

    /**
     * NoteDTO[] -> Note[]
     */
    public static Note[] toNotes(NoteDTO[] notesDTO) {
        if (notesDTO == null)
            return new Note[0];
        Note[] notes = new Note[notesDTO.length];
        for (int i = 0; i < notesDTO.length; i++)
            notes[i] = notesDTO[i].toNote();
        return notes;
    }

    // /**
    //  * Note[] -> NoteDTO[]
    //  */
    // public static NoteDTO[] toNotesDTO(Note[] notes) {
    //     if (notes == null)
    //         return null;
    //     NoteDTO[] notesDTO = new NoteDTO[notes.length];
    //     for (int i = 0; i < notes.length; i++)
    //         notesDTO[i] = toNoteDTO(notes[i]);
    //     return notesDTO;
    // }
}
