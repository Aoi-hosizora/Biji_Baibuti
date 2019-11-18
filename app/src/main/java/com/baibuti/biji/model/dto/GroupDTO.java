package com.baibuti.biji.model.dto;

import com.baibuti.biji.model.po.Group;

import java.io.Serializable;

import lombok.Data;

@Data
public class GroupDTO implements Serializable {

    private int id;
    private String name;
    private int order;
    private String color;

    private GroupDTO(int id, String name, int order, String color) {
        this.id = id;
        this.name = name;
        this.order = order;
        this.color = color;
    }

    /**
     * GroupDTO -> Group
     */
    public Group toGroup() {
        return new Group(id, name, order, color);
    }

    /**
     * Group -> GroupDTO
     */
    public static GroupDTO toGroupDTO(Group group) {
        return new GroupDTO(group.getId(), group.getName(), group.getOrder(), group.getColor());
    }

    /**
     * GroupDTO[] -> Group[]
     */
    public static Group[] toGroups(GroupDTO[] groupsDTO) {
        if (groupsDTO == null)
            return null;
        Group[] groups = new Group[groupsDTO.length];
        for (int i = 0; i < groupsDTO.length; i++)
            groups[i] = groupsDTO[i].toGroup();
        return groups;
    }
}
