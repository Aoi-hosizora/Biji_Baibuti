package com.baibuti.biji.model.dao.daoInterface;

import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.FileClass;

import java.util.List;

public interface IFileClassDao {

    // 查
    List<FileClass> queryAllFileClasses() throws ServerException;
    FileClass queryFileClassById(int id) throws ServerException;
    // FileClass queryDefaultFileClass() throws ServerException;

    // 增删改
    long insertFileClass(FileClass fileClass) throws ServerException;
    boolean updateFileClass(FileClass fileClass) throws ServerException;
    boolean deleteFileClass(int id) throws ServerException;
}
