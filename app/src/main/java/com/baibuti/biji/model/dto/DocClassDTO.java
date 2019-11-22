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
    public DocClass toDocClass() {
        return new DocClass(id, name);
    }


    /**
     * DocClassDTO[] -> DocClass[]
     */
    public static DocClass[] toDocClasses(DocClassDTO[] docClassesDTO) {
        if (docClassesDTO == null)
            return null;
        DocClass[] docClasses = new DocClass[docClassesDTO.length];
        for (int i = 0; i < docClassesDTO.length; i++)
            docClasses[i] = docClassesDTO[i].toDocClass();
        return docClasses;
    }
}
