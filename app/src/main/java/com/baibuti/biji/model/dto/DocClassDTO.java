package com.baibuti.biji.model.dto;

import com.baibuti.biji.model.po.DocClass;

import java.io.Serializable;

import lombok.Data;

@Data
public class DocClassDTO implements Serializable {

    private int id;
    private String name;

    private DocClassDTO(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * DocClassDTO -> DocClass
     */
    public DocClass toFileClass() {
        return new DocClass(id, name);
    }


    /**
     * DocClassDTO[] -> DocClass[]
     */
    public static DocClass[] toFileClasses(DocClassDTO[] fileClassesDTO) {
        if (fileClassesDTO == null)
            return null;
        DocClass[] docClasses = new DocClass[fileClassesDTO.length];
        for (int i = 0; i < fileClassesDTO.length; i++)
            docClasses[i] = fileClassesDTO[i].toFileClass();
        return docClasses;
    }
}
