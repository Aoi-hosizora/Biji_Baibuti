package com.baibuti.biji.net.model.reqBody;

import android.util.Log;

import com.baibuti.biji.model.po.Document;
import com.baibuti.biji.net.module.file.DocumentUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class DocumentReqBody implements Serializable {

    private int id;
    private String foldername;
    private String filename;

    public DocumentReqBody(int id, String foldername, String filename) {
        this.id = id;
        this.foldername = foldername;
        this.filename = filename;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFoldername() {
        return foldername;
    }

    public void setFoldername(String foldername) {
        this.foldername = foldername;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    // region Document <-> DocumentReqBody

    /**
     * SearchItemNetDao -> SearchItem
     * @return
     */
    public Document toDocument() {
        // TODO
        return new Document(id, DocumentUtil.getFileType(filename), filename, null, foldername);
    }

    /**
     * Document -> DocumentReqBody
     * @param document
     * @return
     */
    public static DocumentReqBody toFileReqBody(Document document) {
        return new DocumentReqBody(document.getId(), document.getDocumentClassName(), document.getDocumentName());
    }

    /**
     * DocumentReqBody[] -> Document[]
     * @return
     */
    public static Document[] toDocuments(DocumentReqBody[] fileReqBodies) {
        if (fileReqBodies == null)
            return null;
        Document[] rets = new Document[fileReqBodies.length];
        for (int i = 0; i < fileReqBodies.length; i++)
            rets[i] = fileReqBodies[i].toDocument();
        return rets;
    }

    /**
     * Document[] -> DocumentReqBody[]
     * @return
     */
    public static DocumentReqBody[] toFileReqBodies(Document[] documents) {
        if (documents == null)
            return null;
        DocumentReqBody[] rets = new DocumentReqBody[documents.length];
        for (int i = 0; i < documents.length; i++)
            rets[i] = toFileReqBody(documents[i]);
        return rets;
    }

    /**
     * DocumentReqBody[] -> Json str
     * @return
     */
    public static String toFileReqBodiesJson(DocumentReqBody[] fileReqBodies) {
        if (fileReqBodies == null)
            return "";
        JSONArray jsonArray = new JSONArray();
        for (DocumentReqBody documentReqBody : fileReqBodies) {
            jsonArray.put(documentReqBody.toJson());
        }
        return jsonArray.toString();
    }

    // endregion Document <-> DocumentReqBody

    // region DocumentReqBody <-> Json

    public String toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
            obj.put("foldername", foldername);
            obj.put("filename", filename);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return "";
        }
        return obj.toString();
    }

    /**
     * DocumentReqBody[] -> Json str
     * @return
     */
    public static String getJsonFromDocumentBodies(DocumentReqBody[] documentReqBodies) {
        JSONArray obj = new JSONArray();
        for (DocumentReqBody documentReqBody : documentReqBodies)
            obj.put(documentReqBody.toJson());
        return obj.toString();
    }

    /**
     * Json str -> DocumentReqBody
     * @param json
     * @return
     */
    public static DocumentReqBody getFileRespFromJson(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            return getFileRespFromJson(obj);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Json obj -> DocumentReqBody
     * @param obj
     * @return
     */
    public static DocumentReqBody getFileRespFromJson(JSONObject obj) {
        try {
            Log.e("", "getFileRespFromJson: " + obj.toString() );
            return new DocumentReqBody(
                    obj.getInt("id"),
                    obj.getString("foldername"),
                    obj.getString("filename")
            );
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Json str -> DocumentReqBody[]
     * @param json
     * @return
     */
    public static DocumentReqBody[] getFileRespsFromJson(String json) {
        try {
            JSONArray objs = new JSONArray(json);
            return getFileRespsFromJson(objs);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Json obj -> DocumentReqBody[]
     * @param objs
     * @return
     */
    public static DocumentReqBody[] getFileRespsFromJson(JSONArray objs) {
        try {
            DocumentReqBody[] ret = new DocumentReqBody[objs.length()];
            for (int i = 0; i < objs.length(); i++) {
                ret[i] = getFileRespFromJson(objs.getJSONObject(i));
            }
            return ret;
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    // endregion DocumentReqBody <-> Json
}
