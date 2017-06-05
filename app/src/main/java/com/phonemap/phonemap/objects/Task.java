package com.phonemap.phonemap.objects;

public class Task {
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
