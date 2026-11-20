package com.example.buddybook;

public class UserGroup {
    private String groupId;
    private String groupName;

    public UserGroup(String groupId, String groupName) {
        this.groupId = groupId;
        this.groupName = groupName;
    }

    public String getGroupId() { return groupId; }
    public String getGroupName() { return groupName; }
}