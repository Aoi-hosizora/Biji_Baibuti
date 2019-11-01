package com.baibuti.biji.model.dao.net;

import com.baibuti.biji.model.dao.daoInterface.IFileClassDao;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.FileClass;
import java.util.List;

public class FileClassNetDao implements IFileClassDao {

    @Override
    public List<FileClass> queryAllFileClasses() throws ServerException {
        return null;
    }

    @Override
    public FileClass queryFileClassById(int id) throws ServerException {
        return null;
    }

    @Override
    public FileClass queryFileClassByName(String name) throws ServerException {
        return null;
    }

    @Override
    public long insertFileClass(FileClass fileClass) throws ServerException {
        return 0;
    }

    @Override
    public boolean updateFileClass(FileClass fileClass) throws ServerException {
        return false;
    }

    @Override
    public boolean deleteFileClass(int id) throws ServerException {
        return false;
    }
}
