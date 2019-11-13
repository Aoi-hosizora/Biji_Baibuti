package com.baibuti.biji.model.dto;

import com.baibuti.biji.model.po.Note;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class NoteDTO implements Serializable {

    private int id;
    private String title;
    private String content;
    private GroupDTO group; // <<<
    private Date create_time;
    private Date update_time;

    private NoteDTO(int id, String title, String content, GroupDTO group, Date create_time, Date update_time) {
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
         return new Note(id, title, content, group.toGroup(), create_time, update_time);
     }

    /**
     * Note -> NoteDTO
     */
    public static NoteDTO toNoteDTO(Note note) {
        if (note == null) return null;
        return
            new NoteDTO(note.getId(), note.getTitle(), note.getContent(), GroupDTO.toGroupDTO(note.getGroup()), note.getCreateTime(), note.getUpdateTime());
    }

    /**
     * NoteDTO[] -> Note[]
     */
    public static Note[] toNotes(NoteDTO[] notesDTO) {
        if (notesDTO == null) return null;
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
