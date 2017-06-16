package com.phonemap.phonemap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.phonemap.phonemap.objects.Task;

import static com.phonemap.phonemap.constants.Other.TASK;

public class TaskDescription extends AppCompatActivity {
    private Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_description);

        // ToDo: Move activity into a fragment to reduce duplication
        setupUI();

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                // ToDo: Report that no data about task have been passed
                finish();
            } else {
                task = (Task) extras.getSerializable(TASK);
            }
        } else {
            task= (Task) savedInstanceState.getSerializable(TASK);
        }

        loadUIWithTask(task);
    }

    private void loadUIWithTask(Task task) {
        ((TextView) findViewById(R.id.task_name)).append(task.getName());
        ((TextView) findViewById(R.id.author_name)).append(task.getOwnerFullname());
        ((TextView) findViewById(R.id.organization)).append(task.getOwnerOrg());
        ((TextView) findViewById(R.id.submitted)).append(task.getTimeSubmitted());
        ((TextView) findViewById(R.id.progress)).append(task.getCompletedPercentage());
        ((TextView) findViewById(R.id.description)).append(task.getDescription());
    }

    private void setupUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
    }
}
