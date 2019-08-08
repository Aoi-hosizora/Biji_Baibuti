package com.baibuti.biji.Net.Modules.Note;

import com.baibuti.biji.Data.Models.Note;
import com.baibuti.biji.Net.Models.ReqBody.NoteReqBody;
import com.baibuti.biji.Net.Models.RespBody.MessageResp;
import com.baibuti.biji.Net.Models.RespObj.ServerErrorException;
import com.baibuti.biji.Net.Models.RespType;
import com.baibuti.biji.Net.Modules.Auth.AuthMgr;
import com.baibuti.biji.Net.NetUtil;
import com.baibuti.biji.Net.Urls;

import java.util.Locale;

public class NoteUtil {

    private static final String AllNoteUrl = Urls.NoteUrl + "/all";
    private static final String OneNoteUrl = Urls.NoteUrl + "/one?id=%d";
    private static final String UpdateNoteUrl = Urls.NoteUrl + "/update";
    private static final String InsertNoteUrl = Urls.NoteUrl + "/insert";
    private static final String DeleteNoteUrl = Urls.NoteUrl + "/delete";
    private static final String PushNoteUrl = Urls.NoteUrl + "/push";

    public static Note[] getAllNotes() throws ServerErrorException {
        RespType resp = NetUtil.httpGetSync(AllNoteUrl, NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken()));
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                NoteReqBody[] rets = NoteReqBody.getNoteRespsFromJson(resp.getBody());
                return NoteReqBody.toNotes(rets);
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerErrorException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Note getOneNote(int id) throws ServerErrorException {
        String url = String.format(Locale.CHINA, OneNoteUrl, id);
        RespType resp = NetUtil.httpGetSync(url, NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken()));
        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                NoteReqBody ret = NoteReqBody.getNoteRespFromJson(resp.getBody());
                return ret.toNote();
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerErrorException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Note updateNote(Note Note) throws ServerErrorException {
        RespType resp = NetUtil.httpPostSync(
                UpdateNoteUrl,
                NoteReqBody.toNoteReqBody(Note).toJson(),
                NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken())
        );

        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                NoteReqBody ret = NoteReqBody.getNoteRespFromJson(resp.getBody());
                return ret.toNote();
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerErrorException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Note insertNote(Note Note) throws ServerErrorException {
        RespType resp = NetUtil.httpPostPutDeleteSync(
                InsertNoteUrl, NetUtil.PUT,
                NoteReqBody.toNoteReqBody(Note).toJson(),
                NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken())
        );

        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                NoteReqBody ret = NoteReqBody.getNoteRespFromJson(resp.getBody());
                return ret.toNote();
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerErrorException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Note deleteNote(Note note) throws ServerErrorException {
        RespType resp = NetUtil.httpPostPutDeleteSync(
                DeleteNoteUrl, NetUtil.DELETE,
                NoteReqBody.toNoteReqBody(note).toJson(),
                NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken())
        );

        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                NoteReqBody ret = NoteReqBody.getNoteRespFromJson(resp.getBody());
                return ret.toNote();
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerErrorException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static boolean pushNotes(Note[] notes) throws ServerErrorException {
        RespType resp = NetUtil.httpPostPutDeleteSync(
                PushNoteUrl, NetUtil.POST,
                NoteReqBody.getJsonFromNoteRodies(NoteReqBody.toNoteReqBodies(notes)),
                NetUtil.getOneHeader("Authorization", AuthMgr.getInstance().getToken())
        );

        try {
            int code = resp.getCode();
            if (code == 200) {
                String newToken = resp.getHeaders().get("Authorization");
                if (newToken != null && !(newToken.isEmpty()))
                    AuthMgr.getInstance().setToken(newToken);

                return true;
            }
            else {
                MessageResp msg = MessageResp.getMsgRespFromJson(resp.getBody());
                throw new ServerErrorException(msg.getMessage(), msg.getDetail(), code);
            }
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
