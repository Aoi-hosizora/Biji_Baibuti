package com.baibuti.biji.Net.Models.ReqBody;

import com.baibuti.biji.Data.Models.Group;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class GroupReqBody implements Serializable {

    private int id;
    private String name;
    private int order;
    private String color;

    public GroupReqBody(int id, String name, int order, String color) {
        this.id = id;
        this.name = name;
        this.order = order;
        this.color = color;
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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    // region Group <-> ReqBody

    /**
     * GroupReqBody -> Group
     * @return
     */
    public Group toGroup() {
        return new Group(id, name, order, color);
    }

    /**
     * Group -> GroupReqBody
     * @param group
     * @return
     */
    public static GroupReqBody toGroupReqBody(Group group) {
        return new GroupReqBody(group.getId(), group.getName(), group.getOrder(), group.getColor());
    }

    /**
     * GroupReqBody[] -> Group[]
     * @return
     */
    public static Group[] toGroups(GroupReqBody[] groupReqBodies) {
        if (groupReqBodies == null)
            return null;
        Group[] rets = new Group[groupReqBodies.length];
        for (int i = 0; i < groupReqBodies.length; i++)
            rets[i] = groupReqBodies[i].toGroup();
        return rets;
    }

    // endregion Group <-> ReqBody

    // region GroupReqBody <-> Json

    /**
     * GroupReqBody -> Json str
     * @return
     */
    public String toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
            obj.put("name", name);
            obj.put("order", order);
            obj.put("color", color);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return "";
        }
        return obj.toString();
    }

    /**
     * Json str -> GroupReqBody
     * @param json
     * @return
     */
    public static GroupReqBody getGroupRespFromJson(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            return getGroupRespFromJson(obj);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Json obj -> GroupReqBody
     * @param obj
     * @return
     */
    public static GroupReqBody getGroupRespFromJson(JSONObject obj) {
        try {
            return new GroupReqBody(
                obj.getInt("id"),
                obj.getString("name"),
                obj.getInt("order"),
                obj.getString("color")
            );
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Json str -> GroupReqBody[]
     * @param json
     * @return
     */
    public static GroupReqBody[] getGroupRespsFromJson(String json) {
        try {
            JSONArray obj = new JSONArray(json);
            return geGroupRespsFromJson(obj);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Json obj -> GroupReqBody[]
     * @param objs
     * @return
     */
    public static GroupReqBody[] geGroupRespsFromJson(JSONArray objs) {
        try {
            GroupReqBody[] ret = new GroupReqBody[objs.length()];
            for (int i = 0; i < objs.length(); i++)
                ret[i] = getGroupRespFromJson(objs.getJSONObject(i));
            return ret;
        }
        catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // endregion GroupReqBody <-> Json
}
