package com.phonemap.phonemap;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.phonemap.phonemap.objects.Task;
import com.phonemap.phonemap.requests.GetProfilePicture;

import static com.phonemap.phonemap.constants.Other.TASK;

public class TaskDescription extends AppCompatActivity {
    private Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_description);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                // ToDo: Report that no data about task have been passed
                finish();
            } else {
                task = (Task) extras.getSerializable(TASK);
            }
        } else {
            task = (Task) savedInstanceState.getSerializable(TASK);
        }

        // ToDo: Move activity into a fragment to reduce duplication
        setupActionBar(task);
        loadUIWithTask(task);
    }

    private void loadUIWithTask(Task task) {
        ((TextView) findViewById(R.id.author_name)).setText(task.getOwnerFullname());
        ((TextView) findViewById(R.id.organization)).setText(task.getOwnerOrg());
        ((TextView) findViewById(R.id.submitted)).setText(task.getTimeSubmitted());
        ((TextView) findViewById(R.id.completion)).setText(task.getExpectedCompletionTime());
        ((TextView) findViewById(R.id.progress)).setText(task.getCompletedPercentage());
        ((TextView) findViewById(R.id.description)).setText(task.getDescription());

        ImageView view = (ImageView) findViewById(R.id.imageView);
        new GetProfilePicture(view, task.getOwnerID());
    }

    private void setupActionBar(Task task) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowHomeEnabled(true);
        bar.setTitle(task.getName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);

    }
}
