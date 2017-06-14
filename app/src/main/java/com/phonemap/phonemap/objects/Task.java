package com.phonemap.phonemap.objects;

import java.io.Serializable;

public class Task implements Serializable {
    private final String name;
    private final String description;
    private final int id;
    private final int totalSubtasks;
    private final int completedSubtasks;
    private final String ownerFullname;
    private final String ownerOrg;
    private final String timeSubmitted;

    public Task(String name, String description, int id, int totalSubtasks, int completedSubtasks, String ownerFullname, String ownerOrg, String timeSubmitted) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.totalSubtasks = totalSubtasks;
        this.completedSubtasks = completedSubtasks;
        this.ownerFullname = ownerFullname;
        this.ownerOrg = ownerOrg;
        this.timeSubmitted = timeSubmitted;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public int getTotalSubtasks() {
        return totalSubtasks;
    }

    public int getCompletedSubtasks() {
        return completedSubtasks;
    }

    public String getOwnerFullname() {
        return ownerFullname;
    }

    public String getOwnerOrg() {
        return ownerOrg;
    }

    public String getTimeSubmitted() {
        return timeSubmitted;
    }
}
