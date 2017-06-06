package com.phonemap.phonemap.requests;

import com.phonemap.phonemap.objects.Task;

import java.util.List;

public interface ServerListener {
    void gotTasks(List<Task> tasks);
}