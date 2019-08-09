package com.baibuti.biji.Net.Models.ReqBody;

import com.baibuti.biji.Data.Models.Group;
import com.baibuti.biji.Data.Models.Note;
import com.baibuti.biji.Utils.OtherUtils.DateColorUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

public class NoteReqBody implements Serializable {

    private int id;
    private String title;
    private String content;
    private int group_id;
    private Date create_time;
    private Date update_time;

    public NoteReqBody(int id, String title, String content, int group_id, Date create_time, Date update_time) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.group_id = group_id;
        this.create_time = create_time;
        this.update_time = update_time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getGroup_id() {
        return group_id;
    }

    public void setGroup_id(int group_id) {
        this.group_id = group_id;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    public Date getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(Date update_time) {
        this.update_time = update_time;
    }

    // region Note <-> ReqBody

     /**
      * NoteReqBody -> Note, unset-group !!!!!!
      * @return
      */
     public Note toNote() {
         // TODO
         return new Note(id, title, content, Group.getTmpGroup(group_id), create_time, update_time);
     }

    /**
     * Note -> NoteReqBody
     * @param note
     * @return
     */
    public static NoteReqBody toNoteReqBody(Note note) {
        if (note == null) return null;
        return new NoteReqBody(note.getId(), note.getTitle(), note.getContent(), note.getGroupLabel().getId(), note.getCreateTime(), note.getUpdateTime());
    }

    /**
     * NoteReqBody[] -> Note[]
     * @return
     */
    public static Note[] toNotes(NoteReqBody[] noteReqBodies) {
        if (noteReqBodies == null)
            return null;
        Note[] rets = new Note[noteReqBodies.length];
        for (int i = 0; i < noteReqBodies.length; i++)
            rets[i] = noteReqBodies[i].toNote();
        return rets;
    }

    /**
     * Note[] -> NoteReqBody[]
     * @return
     */
    public static NoteReqBody[] toNoteReqBodies(Note[] notes) {
        if (notes == null)
            return null;
        NoteReqBody[] rets = new NoteReqBody[notes.length];
        for (int i = 0; i < notes.length; i++)
            rets[i] = toNoteReqBody(notes[i]);
        return rets;
    }

    // endregion Note <-> ReqBody

    // region NoteReqBody <-> Json

    /**
     * NoteReqBody -> Json str
     * @return
     */
    public String toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
            obj.put("title", title);
            obj.put("content", content);
            obj.put("group_id", group_id);
            obj.put("create_time", DateColorUtil.Date2Str(create_time));
            obj.put("update_time", DateColorUtil.Date2Str(update_time));
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return "";
        }
        return obj.toString();
    }

    /**
     * NoteReqBody[] -> Json str
     * @return
     */
    public static String getJsonFromNoteRodies(NoteReqBody[] noteReqBodies) {
        JSONArray obj = new JSONArray();
        for (NoteReqBody noteReqBody : noteReqBodies)
            obj.put(noteReqBody.toJson());
        return obj.toString();
    }

    /**
     * Json str -> NoteReqBody
     * @param json
     * @return
     */
    public static NoteReqBody getNoteRespFromJson(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            return getNoteRespFromJson(obj);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Json obj -> NoteReqBody
     * @param obj
     * @return
     */
    public static NoteReqBody getNoteRespFromJson(JSONObject obj) {
        try {
            Date ct = DateColorUtil.Str2Date(obj.getString("create_time"));
            Date ut = DateColorUtil.Str2Date(obj.getString("update_time"));
            return new NoteReqBody(
                obj.getInt("id"),
                obj.getString("title"),
                obj.getString("content"),
                obj.getInt("group_id"),
                ct,
                ut
            );
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Json str -> NoteReqBody[]
     * @param json
     * @return
     */
    public static NoteReqBody[] getNoteRespsFromJson(String json) {
        try {
            JSONArray obj = new JSONArray(json);
            return getNoteRespsFromJson(obj);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Json obj -> NoteReqBody[]
     * @param objs
     * @return
     */
    public static NoteReqBody[] getNoteRespsFromJson(JSONArray objs) {
        try {
            NoteReqBody[] ret = new NoteReqBody[objs.length()];
            for (int i = 0; i < objs.length(); i++)
                ret[i] = getNoteRespFromJson(objs.getJSONObject(i));
            return ret;
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // endregion NoteReqBody <-> Json
}
