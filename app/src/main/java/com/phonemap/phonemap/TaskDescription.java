package com.phonemap.phonemap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.phonemap.phonemap.objects.Task;

import static com.phonemap.phonemap.constants.Other.TASK;

public class TaskDescription extends AppCompatActivity {

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
                loadUIWithTask((Task) extras.getSerializable(TASK));
            }
        } else {
            loadUIWithTask((Task) savedInstanceState.getSerializable(TASK));
        }
    }

    private void loadUIWithTask(Task task) {

    }

    private void setupUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
    }
}
