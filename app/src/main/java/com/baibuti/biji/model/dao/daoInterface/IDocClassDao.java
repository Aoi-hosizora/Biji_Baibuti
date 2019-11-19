package com.baibuti.biji.model.dao.daoInterface;

import com.baibuti.biji.model.dao.DbStatusType;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.DocClass;

import java.util.List;

public interface IDocClassDao {

    // 查
    List<DocClass> queryAllDocClasses() throws ServerException;
    DocClass queryDocClassById(int id) throws ServerException;
    DocClass queryDocClassByName(String name) throws ServerException;
    DocClass queryDefaultDocClass() throws ServerException;

    // 增删改
    DbStatusType insertDocClass(DocClass docClass) throws ServerException;
    DbStatusType updateDocClass(DocClass docClass) throws ServerException;
    DbStatusType deleteDocClass(int id) throws ServerException;
}
