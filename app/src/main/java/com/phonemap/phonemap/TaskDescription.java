package com.phonemap.phonemap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.phonemap.phonemap.objects.Task;

import static com.phonemap.phonemap.constants.Other.TASK;

public class TaskDescription extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_description);
    }
}
