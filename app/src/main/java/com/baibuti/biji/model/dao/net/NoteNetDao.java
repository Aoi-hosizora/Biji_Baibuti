package com.baibuti.biji.model.dao.net;

import com.baibuti.biji.model.dao.daoInterface.INoteDao;
import com.baibuti.biji.model.dto.OneFieldDTO;
import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.model.dto.ServerException;
import com.baibuti.biji.model.po.Note;
import com.baibuti.biji.service.auth.AuthManager;
import com.baibuti.biji.service.retrofit.RetrofitFactory;
import com.baibuti.biji.model.dto.NoteDTO;
import com.baibuti.biji.service.retrofit.ServerErrorHandle;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class NoteNetDao implements INoteDao {

    @Override
    public List<Note> queryAllNotes() throws ServerException {
        Observable<ResponseDTO<NoteDTO[]>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getAllNotes()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<NoteDTO[]> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return Arrays.asList(NoteDTO.toNotes(response.getData()));
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    @Override
    public List<Note> queryNotesByGroupId(int id) throws ServerException {
        Observable<ResponseDTO<NoteDTO[]>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getNotesByGroupId(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<NoteDTO[]> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return Arrays.asList(NoteDTO.toNotes(response.getData()));
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    @Override
    public Note queryNoteById(int id) throws ServerException {
        Observable<ResponseDTO<NoteDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getNoteById(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<NoteDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return response.getData().toNote();
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    @Override
    public long insertNote(Note note) throws ServerException {

        // TODO 同时上传图片

        // TODO
        // /**
        //  * 将笔记的图片上传并修改
        //  * @param note 新笔记 用来上传图片
        //  * @param motoNote 旧笔记 用来删除笔记
        //  * @return
        //  */
        // @WorkerThread
        // private String handleSaveImgToServer(String note, String motoNote) {
        //
        //     String ret = note;
        //
        //     // 切割块
        //     List<String> motoTextList = StringUtil.cutStringByImgTag(motoNote); // 旧 删除用
        //     List<String> textList = StringUtil.cutStringByImgTag(note); // 新 上传用
        //
        //     // 获取 服务器上原有的图片
        //     ArrayList<String> NewUrls = new ArrayList<>();
        //     for (String blocks : textList) {
        //         if (blocks.contains("<img") && blocks.contains("src=")) {
        //             String imagePath = StringUtil.getImgSrc(blocks);
        //             if (imagePath.startsWith(ImgUtil.GetImgUrlHead)) {
        //                 NewUrls.add(imagePath);
        //             }
        //         }
        //     }
        //
        //     ArrayList<String> DelUrls = new ArrayList<>();
        //     for (String blocks : motoTextList) {
        //         if (blocks.contains("<img") && blocks.contains("src=")) {
        //             String imagePath = StringUtil.getImgSrc(blocks);
        //             if (imagePath.startsWith(ImgUtil.GetImgUrlHead)) {
        //                 if (NewUrls.indexOf(imagePath) == -1) // 不存在新内，删除
        //                     DelUrls.add(imagePath);
        //             }
        //         }
        //     }
        //
        //     // 异步删除原有的图片
        //     if (DelUrls.size() > 0)
        //         ImgUtil.DeleteImgsAsync(DelUrls.toArray(new String[0]));
        //
        //
        //     // 遍历本地图片
        //     for (String blocks : textList) {
        //         // 图片块
        //         if (blocks.contains("<img") && blocks.contains("src=")) {
        //             // 图片路径
        //             String imagePath = StringUtil.getImgSrc(blocks);
        //             // 本地路径，网络路径忽略
        //             if (imagePath.startsWith(AppPathUtil.SDCardRoot)) { // /storage/emulated/0/
        //                 try {
        //                     UploadStatus uploadStatus = ImgUtil.uploadImg(imagePath);
        //                     if (uploadStatus != null) {
        //                         String newFileName = uploadStatus.getNewFileName();
        //                         Log.e("", "handleSaveImgToServer: " + imagePath + " -> " + newFileName);
        //                         ret = ret.replaceAll(imagePath, newFileName);
        //                     }
        //                 }
        //                 catch (ServerException ex) {
        //                     ex.printStackTrace();
        //                 }
        //             }
        //         }
        //     }
        //     return ret;
        // }

        Observable<ResponseDTO<NoteDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .insertNote(note.getTitle(), note.getContent(), note.getGroup().getId())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<NoteDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return response.getData().getId();
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    @Override
    public boolean updateNote(Note note) throws ServerException {

        // TODO 同时上传图片

        Observable<ResponseDTO<NoteDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .updateNote(note.getId(), note.getTitle(), note.getContent(), note.getGroup().getId())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<NoteDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return true;
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    @Override
    public boolean deleteNote(int id) throws ServerException {

        // TODO 同时判断，删除图片

        Observable<ResponseDTO<NoteDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteNote(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<NoteDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return true;
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }

    @Override
    public int deleteNotes(int[] ids) throws ServerException {

        // TODO 同时判断，删除图片

        Observable<ResponseDTO<OneFieldDTO.CountDTO>> observable = RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteNotes(ids)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

        try {
            ResponseDTO<OneFieldDTO.CountDTO> response = observable.toFuture().get();
            if (response.getCode() != ServerErrorHandle.SUCCESS)
                throw ServerErrorHandle.parseErrorMessage(response);

            return response.getData().getCount();
        }
        catch (ServerException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            throw ServerErrorHandle.getClientError(ex);
        }
    }
}
