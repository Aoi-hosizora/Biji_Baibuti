package com.baibuti.biji.common.interact.server;

import com.baibuti.biji.common.interact.contract.INoteInteract;
import com.baibuti.biji.model.dto.OneFieldDTO;
import com.baibuti.biji.model.dto.ResponseDTO;
import com.baibuti.biji.model.po.Note;
import com.baibuti.biji.common.auth.AuthManager;
import com.baibuti.biji.common.retrofit.RetrofitFactory;
import com.baibuti.biji.model.dto.NoteDTO;
import com.baibuti.biji.model.vo.MessageVO;
import com.baibuti.biji.util.imgTextUtil.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class NoteNetInteract implements INoteInteract {

    @Override
    public Observable<MessageVO<List<Note>>> queryAllNotes() {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getAllNotes()
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<List<Note>>(false, responseDTO.getMessage());
                List<Note> fromNotes = new ArrayList<>();
                Collections.addAll(fromNotes, NoteDTO.toNotes(responseDTO.getData()));
                return new MessageVO<>(fromNotes);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<List<Note>>> queryNotesByGroupId(int id) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getNotesByGroupId(id)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<List<Note>>(false, responseDTO.getMessage());
                List<Note> fromNotes = new ArrayList<>();
                Collections.addAll(fromNotes, NoteDTO.toNotes(responseDTO.getData()));
                return new MessageVO<>(fromNotes);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Note>> queryNoteById(int id) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getNoteById(id)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Note>(false, responseDTO.getMessage());
                return new MessageVO<>(responseDTO.getData().toNote());
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @return SUCCESS | FAILED | UPLOAD_FAILED
     */
    @Override
    public Observable<MessageVO<Boolean>> insertNote(Note note) {
        Note newNote = uploadImage(note);
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .insertNote(newNote.getTitle(), newNote.getContent(), newNote.getGroup().getId(), note.getCreateTime_FullString(), note.getUpdateTime_FullString())
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Boolean>(false, responseDTO.getMessage());
                return new MessageVO<>(true);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @return SUCCESS | FAILED | UPLOAD_FAILED
     */
    @Override
    public Observable<MessageVO<Boolean>> updateNote(Note note) {
        Note newNote = uploadImage(note);
       return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .updateNote(newNote.getId(), newNote.getTitle(), newNote.getContent(), newNote.getGroup().getId())
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Boolean>(false, responseDTO.getMessage());
                return new MessageVO<>(true);
            })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * SUCCESS | FAILED
     */
    @Override
    public Observable<MessageVO<Boolean>> deleteNote(int id) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteNote(id)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 20)
                    return new MessageVO<Boolean>(false, responseDTO.getMessage());
                return new MessageVO<>(true);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Integer>> deleteNotes(int[] ids) {
        Integer[] newIds = new Integer[ids.length];
        for (int i = 0; i < ids.length; i++)
            newIds[i] = ids[i];
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteNotes(newIds)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Integer>(false,  responseDTO.getMessage());
                return new MessageVO<>(responseDTO.getData().getCount());
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 上传所有笔本地图片
     */
    private synchronized Note uploadImage(Note note) {
        List<String> textList = StringUtil.cutStringByImgTag(note.getContent()); // 所有
        Set<String> uploadUrl = new TreeSet<>(); // 所有本地图片

        for (String url : textList) {
            if (url.contains("<img") && url.contains("src=")) {
                File file = new File(url);
                if (file.exists()) {
                    uploadUrl.add(url);
                }
            }
        }

        for (String url : uploadUrl.toArray(new String[0])) {
            ResponseDTO<OneFieldDTO.FilenameDTO> responseDTO = RetrofitFactory.getInstance()
                .createRequest(AuthManager.getInstance().getAuthorizationHead())
                .uploadImage(new File(url), OneFieldDTO.RawImageType_Note)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .blockingFirst();
            note.setContent(note.getContent().replace(url, responseDTO.getData().getFilename()));
        }
        return note;
    }
}
