package com.phonemap.phonemap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.phonemap.phonemap.objects.Task;
import com.phonemap.phonemap.requests.GetProfilePicture;

import static com.phonemap.phonemap.constants.Intents.UPDATED_PREFERRED_TASK;
import static com.phonemap.phonemap.constants.Other.TASK;
import static com.phonemap.phonemap.constants.Preferences.CURRENT_TASK;
import static com.phonemap.phonemap.constants.Preferences.INVALID_TASK_ID;
import static com.phonemap.phonemap.constants.Preferences.PREFERENCES;
import static com.phonemap.phonemap.services.Utils.startJSRunner;

public class TaskDescription extends AppCompatActivity {
    public static final String LOG_TAG = "TaskDescription";

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

    private void loadUIWithTask(final Task task) {
        ((TextView) findViewById(R.id.author_name)).setText(task.getOwnerFullname());
        ((TextView) findViewById(R.id.organization)).setText(task.getOwnerOrg());
        ((TextView) findViewById(R.id.submitted)).setText(task.getTimeSubmitted());
        ((TextView) findViewById(R.id.completion)).setText(task.getExpectedCompletionTime());
        ((TextView) findViewById(R.id.progress)).setText(task.getCompletedPercentage());
        ((TextView) findViewById(R.id.description)).setText(task.getDescription());

        ImageView view = (ImageView) findViewById(R.id.imageView);
        new GetProfilePicture(view, task.getOwnerID());

        SharedPreferences preferences = getApplicationContext()
                .getSharedPreferences(PREFERENCES, MODE_PRIVATE);

        final Button button = ((Button) findViewById(R.id.preferredTask));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences =
                        getApplication().getSharedPreferences(PREFERENCES, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();

                if (preferences.getInt(CURRENT_TASK, INVALID_TASK_ID) == task.getTaskID()) {
                    button.setText(getString(R.string.select_task));
                    editor.putInt(CURRENT_TASK, INVALID_TASK_ID);
                    editor.apply();
                } else {
                    button.setText(getString(R.string.preferred_task));
                    editor.putInt(CURRENT_TASK, task.getTaskID());
                    editor.apply();

                    startJSRunner(getApplication());
                }

                Intent intent = new Intent(UPDATED_PREFERRED_TASK);
                LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(intent);
            }
        });

        if (preferences.getInt(CURRENT_TASK, INVALID_TASK_ID) == task.getTaskID()) {
            button.setText(getString(R.string.preferred_task));
        }
    }

    private void setupActionBar(Task task) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();

        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowHomeEnabled(true);
            bar.setTitle(task.getName());
        } else {
            Log.e(LOG_TAG, "Could not find action bar");
        }
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
