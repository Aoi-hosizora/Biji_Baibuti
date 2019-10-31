package com.baibuti.biji.data.dto;

import com.baibuti.biji.data.model.Group;
import com.baibuti.biji.data.model.Note;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class NoteDTO implements Serializable {

    private int id;
    private String title;
    private String content;
    private int group_id;
    private Date create_time;
    private Date update_time;

    private NoteDTO(int id, String title, String content, int group_id, Date create_time, Date update_time) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.group_id = group_id;
        this.create_time = create_time;
        this.update_time = update_time;
    }

    /**
      * NoteDTO -> Note, unset-group !!!!!!
      */
     public Note toNote() {
         // TODO 临时 Group
         return new Note(id, title, content, new Group(group_id), create_time, update_time);
     }

    /**
     * Note -> NoteDTO
     */
    public static NoteDTO toNoteDTO(Note note) {
        if (note == null) return null;
        return
            new NoteDTO(note.getId(), note.getTitle(), note.getContent(), note.getGroup().getId(), note.getCreateTime(), note.getUpdateTime());
    }

    /**
     * NoteDTO[] -> Note[]
     */
    public static Note[] toNotes(NoteDTO[] notesDTO) {
        // TODO 临时 Group
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
