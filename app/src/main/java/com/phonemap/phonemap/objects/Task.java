package com.phonemap.phonemap.objects;

import java.io.Serializable;

import static com.phonemap.phonemap.constants.Preferences.INVALID_TASK_ID;

public class Task implements Serializable {
    private final String name;
    private final String description;
    private final int id;
    private final int total_subtasks;
    private final int completed_subtasks;
    private final String owner_fullname;
    private final String owner_org;
    private final String time_submitted;

    public Task(String name, String description, int id, int total_subtasks, int completed_subtasks, String owner_fullname, String owner_org, String time_submitted) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.total_subtasks = total_subtasks;
        this.completed_subtasks = completed_subtasks;
        this.owner_fullname = owner_fullname;
        this.owner_org = owner_org;
        this.time_submitted = time_submitted;
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

    public int getTotal_subtasks() {
        return total_subtasks;
    }

    public int getCompleted_subtasks() {
        return completed_subtasks;
    }

    public String getOwner_fullname() {
        return owner_fullname;
    }

    public String getOwner_org() {
        return owner_org;
    }

    public String getTime_submitted() {
        return time_submitted;
    }
}
