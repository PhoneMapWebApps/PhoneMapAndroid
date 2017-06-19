package com.phonemap.phonemap.requests;

import android.graphics.Bitmap;

import com.phonemap.phonemap.objects.Task;

import java.util.List;

public interface ServerListener {
    void gotTasks(List<Task> tasks);
    void gotProfilePicture(Bitmap picture, int task_id);
    void gotTaskPicture(Bitmap picture, int task_id);
}