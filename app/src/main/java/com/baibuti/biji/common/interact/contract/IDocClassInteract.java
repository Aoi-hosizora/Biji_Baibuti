package com.baibuti.biji.common.interact.contract;

import com.baibuti.biji.model.po.DocClass;
import com.baibuti.biji.model.vo.MessageVO;

import java.util.List;

import io.reactivex.Observable;

public interface IDocClassInteract {

    // 查
    Observable<MessageVO<List<DocClass>>> queryAllDocClasses();
    Observable<MessageVO<DocClass>> queryDocClassById(int id);
    Observable<MessageVO<DocClass>> queryDocClassByName(String name);
    Observable<MessageVO<DocClass>> queryDefaultDocClass();

    // 增删改
    Observable<MessageVO<Boolean>> insertDocClass(DocClass docClass);
    Observable<MessageVO<Boolean>> updateDocClass(DocClass docClass);
    Observable<MessageVO<Boolean>> deleteDocClass(int id, boolean isToDefault);
}
