package com.baibuti.biji.model.dto;

import com.baibuti.biji.model.po.FileClass;

import java.io.Serializable;

import lombok.Data;

@Data
public class FileClassDTO implements Serializable {

    private int id;
    private String name;
    private int order;

    private FileClassDTO(int id, String name, int order) {
        this.id = id;
        this.name = name;
        this.order = order;
    }

    /**
     * FileClassDTO -> FileClass
     */
    public FileClass toFileClass() {
        return new FileClass(id, name, order);
    }

    /**
     * FileClass -> FileClassDTO
     */
    public static FileClassDTO toFileClassDTO(FileClass fileClass) {
        return new FileClassDTO(fileClass.getId(), fileClass.getName(), fileClass.getOrder());
    }

    /**
     * FileClass[] -> FileClassDTO[]
     */
    public static FileClassDTO[] toFileClassesDTO(FileClass[] fileClasses) {
        if (fileClasses == null)
            return null;
        FileClassDTO[] fileClassesDTO = new FileClassDTO[fileClasses.length];
        for (int i = 0; i < fileClasses.length; i++)
            fileClassesDTO[i] = toFileClassDTO(fileClasses[i]);
        return fileClassesDTO;
    }

    // /**
    //  * FileClassDTO[] -> FileClass[]
    //  */
    // public static FileClass[] toFileClasses(FileClassDTO[] fileClassesDTO) {
    //     if (fileClassesDTO == null)
    //         return null;
    //     FileClass[] fileClasses = new FileClass[fileClassesDTO.length];
    //     for (int i = 0; i < fileClassesDTO.length; i++)
    //         fileClasses[i] = fileClassesDTO[i].toFileClass();
    //     return fileClasses;
    // }
}
