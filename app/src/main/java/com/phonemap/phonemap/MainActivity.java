package com.phonemap.phonemap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.phonemap.phonemap.adapters.TaskListAdapter;
import com.phonemap.phonemap.constants.Preferences;
import com.phonemap.phonemap.objects.Task;
import com.phonemap.phonemap.requests.ServerAPI;
import com.phonemap.phonemap.requests.ServerListener;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.util.List;

import static com.phonemap.phonemap.constants.Intents.JSRUNNER_FAILED_EXECUTION;
import static com.phonemap.phonemap.constants.Intents.JSRUNNER_STARTED_INTENT;
import static com.phonemap.phonemap.constants.Intents.JSRUNNER_STOP_INTENT;
import static com.phonemap.phonemap.constants.Intents.UPDATED_PREFERRED_TASK;
import static com.phonemap.phonemap.constants.Requests.TASK_NAME;
import static com.phonemap.phonemap.constants.Sockets.NO_TASKS;
import static com.phonemap.phonemap.services.Utils.startJSRunner;

public class MainActivity extends AppCompatActivity implements ServerListener {

    private final ServerAPI serverAPI = new ServerAPI(this);
    private final String LOG_TAG = "MainActivity";

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(JSRUNNER_STARTED_INTENT)) {
                setCurrentStatus("Executing task.");
                setCurrentName(intent.getStringExtra(TASK_NAME));
                setLoading(true);
            } else if (intent.getAction().equals(JSRUNNER_STOP_INTENT)) {
                setCurrentName("Finished executing task.");
                setCurrentStatus("");
                setLoading(false);
            } else if (intent.getAction().equals(JSRUNNER_FAILED_EXECUTION)) {
                setCurrentStatus("Error occurred when executing task! Retrying...");
                setLoading(false);
                serverAPI.getTasks();
            } else if (intent.getAction().equals(NO_TASKS)) {
                setCurrentName("No tasks available.");
                setCurrentStatus("");
                serverAPI.getTasks();
                setLoading(false);
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

        serverAPI.getTasks();

        startJSRunner(this);
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
            case R.id.action_refresh:
                serverAPI.getTasks();
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForCrashes();

        Preferences preferences = new Preferences(getApplicationContext());
        Intent intent = preferences.getLastIntent();
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        serverAPI.getTasks();
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
        // ToDo: Remove this for store builds!
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
        task_name.setText(name);
    }

    private void setCurrentStatus(String status) {
        TextView currentStatus = (TextView) findViewById(R.id.currentTaskStatus);
        currentStatus.setText(status);
    }

    private void setLoading(boolean loading) {
        ProgressBar spinner = (ProgressBar) findViewById(R.id.loading);

        if (loading) {
            spinner.setVisibility(View.VISIBLE);
        } else {
            spinner.setVisibility(View.GONE);
        }
    }
}
