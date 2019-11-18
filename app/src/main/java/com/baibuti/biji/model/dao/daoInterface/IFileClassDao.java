package com.baibuti.biji.model.dao.daoInterface;

import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.DocClass;

import java.util.List;

public interface IFileClassDao {

    // 查
    List<DocClass> queryAllFileClasses() throws ServerException;
    DocClass queryFileClassById(int id) throws ServerException;
    DocClass queryDefaultFileClass() throws ServerException;

    // 增删改
    long insertFileClass(DocClass docClass) throws ServerException;
    boolean updateFileClass(DocClass docClass) throws ServerException;
    boolean deleteFileClass(int id) throws ServerException;
}
