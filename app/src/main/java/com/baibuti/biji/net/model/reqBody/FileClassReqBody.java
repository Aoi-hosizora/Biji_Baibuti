package com.baibuti.biji.net.model.reqBody;

import com.baibuti.biji.data.po.FileClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class FileClassReqBody implements Serializable {

    private int id;
    private String name;

    public FileClassReqBody(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // region FileClass <-> ReqBody

    /**
     * FileClassReqBody -> FileClass
     * @return
     */
    public FileClass toFileClass() {
        return new FileClass(name, 0);
    }

    /**
     * FileClass -> FileClassReqBody
     * @param fileClass
     * @return
     */
    public static FileClassReqBody toFileClassReqBody(FileClass fileClass) {
        return new FileClassReqBody(fileClass.getId(), fileClass.getFileClassName());
    }

    /**
     * FileClass[] -> FileClassReqBody[]
     * @param fileClasses
     * @return
     */
    public static FileClassReqBody[] toFileClassReqBodies(FileClass[] fileClasses) {
        if (fileClasses == null)
            return null;
        FileClassReqBody[] rets = new FileClassReqBody[fileClasses.length];
        for (int i = 0; i < fileClasses.length; i++)
            rets[i] = toFileClassReqBody(fileClasses[i]);
        return rets;
    }

    /**
     * FileClassReqBody[] -> FileClass[]
     * @return
     */
    public static FileClass[] toFileClasses(FileClassReqBody[] fileClassReqBodies) {
        if (fileClassReqBodies == null)
            return null;
        FileClass[] rets = new FileClass[fileClassReqBodies.length];
        for (int i = 0; i < fileClassReqBodies.length; i++)
            rets[i] = fileClassReqBodies[i].toFileClass();
        return rets;
    }

    // endregion FileClass <-> ReqBody

    // region FileClassReqBody <-> Json

    /**
     * FileClassReqBody -> Json str
     * @return
     */
    public String toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
            obj.put("name", name);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return "";
        }
        return obj.toString();
    }

    /**
     * Json str -> FileClassReqBody
     * @param json
     * @return
     */
    public static FileClassReqBody getFileClassRespFromJson(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            return getFileClassRespFromJson(obj);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Json obj -> FileClassReqBody
     * @param obj
     * @return
     */
    public static FileClassReqBody getFileClassRespFromJson(JSONObject obj) {
        try {
            return new FileClassReqBody(
                obj.getInt("id"),
                obj.getString("name")
            );
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Json str -> FileClassReqBody[]
     * @param json
     * @return
     */
    public static FileClassReqBody[] getFileClassRespsFromJson(String json) {
        try {
            JSONArray obj = new JSONArray(json);
            return getFileClassRespsFromJson(obj);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Json obj -> FileClassReqBody[]
     * @param objs
     * @return
     */
    public static FileClassReqBody[] getFileClassRespsFromJson(JSONArray objs) {
        try {
            FileClassReqBody[] ret = new FileClassReqBody[objs.length()];
            for (int i = 0; i < objs.length(); i++)
                ret[i] = getFileClassRespFromJson(objs.getJSONObject(i));
            return ret;
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * FileClassReqBody[] -> Json str
     * @return
     */
    public static String getJsonFromFileClassReqRodies(FileClassReqBody[] fileClassReqBodies) {
        JSONArray obj = new JSONArray();
        for (FileClassReqBody fileClassReqBody : fileClassReqBodies)
            obj.put(fileClassReqBody.toJson());
        return obj.toString();
    }

    // endregion FileClassReqBody <-> Json
}
