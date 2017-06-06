package com.phonemap.phonemap.objects;

import static com.phonemap.phonemap.constants.Preferences.INVALID_TASK_ID;

public class Task {
    public final static Task NULL_TASK = new Task("No task selected", "", INVALID_TASK_ID);
    private final String name;
    private final String description;
    private final int id;

    public Task(String name, String description, int id) {
        this.name = name;
        this.description = description;
        this.id = id;
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
}
