package com.phonemap.phonemap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.phonemap.phonemap.adapters.TaskListAdapter;
import com.phonemap.phonemap.objects.Task;
import com.phonemap.phonemap.requests.RequestAPI;
import com.phonemap.phonemap.requests.ServerListener;
import com.phonemap.phonemap.services.JSRunner;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.util.List;

import static com.phonemap.phonemap.constants.Intents.JSRUNNER_FAILED_EXECUTION;
import static com.phonemap.phonemap.constants.Intents.JSRUNNER_STARTED_INTENT;
import static com.phonemap.phonemap.constants.Intents.JSRUNNER_STOP_INTENT;
import static com.phonemap.phonemap.constants.Intents.UPDATED_PREFERRED_TASK;
import static com.phonemap.phonemap.constants.Requests.TASK_NAME;
import static com.phonemap.phonemap.constants.Sockets.NO_TASKS;

public class MainActivity extends AppCompatActivity implements ServerListener {

    private final RequestAPI requestAPI = new RequestAPI(this);
    private final String LOG_TAG = "MainActivity";

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(JSRUNNER_STARTED_INTENT)) {
                setCurrentStatus("Executing task.");
                setCurrentName(intent.getStringExtra(TASK_NAME));
            } else if (intent.getAction().equals(JSRUNNER_STOP_INTENT)) {
                setCurrentStatus("Finished executing task.");
            } else if (intent.getAction().equals(JSRUNNER_FAILED_EXECUTION)) {
                setCurrentStatus("Error occurred when executing task! Retrying...");
            } else if (intent.getAction().equals(NO_TASKS)) {
                setCurrentStatus("No tasks available.");
                setCurrentName("");
            } else if (intent.getAction().equals(UPDATED_PREFERRED_TASK)) {
                // ToDo: Have some visual indication that preference has changed
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerIntentFilter();
        setupUI();

        requestAPI.getTasks();

        startService(new Intent(this, JSRunner.class));
        checkForUpdates();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForCrashes();
        requestAPI.getTasks();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterManagers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterManagers();
        unregisterIntentFilter();

    }

    private void setupUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
    }

    private void unregisterIntentFilter() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
    }

    private void registerIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(JSRUNNER_STOP_INTENT);
        filter.addAction(JSRUNNER_STARTED_INTENT);
        filter.addAction(JSRUNNER_FAILED_EXECUTION);
        filter.addAction(NO_TASKS);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, filter);
    }

    private void checkForCrashes() {
        CrashManager.register(this);
    }

    private void checkForUpdates() {
        // Remove this for store builds!
        UpdateManager.register(this);
    }

    private void unregisterManagers() {
        UpdateManager.unregister();
    }

    @Override
    public void gotTasks(final List<Task> tasks) {
        final ListView listView = (ListView) findViewById(R.id.taskListView);
        listView.setAdapter(new TaskListAdapter(this, tasks));
    }

    private void setCurrentName(String name) {
        TextView task_name = (TextView) findViewById(R.id.currentTaskName);

        if (!name.equals(task_name.getText())) {
            task_name.setText(name);
            requestAPI.getTasks();
        }
    }

    private void setCurrentStatus(String status) {
        TextView currentStatus = (TextView) findViewById(R.id.currentTaskStatus);
        currentStatus.setText(status);
    }
}
