package com.phonemap.phonemap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.phonemap.phonemap.adapters.TaskListAdapter;
import com.phonemap.phonemap.objects.Task;
import com.phonemap.phonemap.requests.RequestAPI;
import com.phonemap.phonemap.requests.ServerListener;
import com.phonemap.phonemap.services.JSRunner;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.util.List;

import static com.phonemap.phonemap.constants.Intents.JSRUNNER_STARTED_INTENT;
import static com.phonemap.phonemap.constants.Intents.JSRUNNER_STOP_INTENT;
import static com.phonemap.phonemap.constants.Preferences.CURRENT_TASK;
import static com.phonemap.phonemap.constants.Preferences.INVALID_TASK_ID;
import static com.phonemap.phonemap.constants.Preferences.PREFERENCES;

public class MainActivity extends AppCompatActivity implements ServerListener {

    private final RequestAPI requestAPI = new RequestAPI(this);
    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(JSRUNNER_STOP_INTENT)) {
                // JSRunenr stopped
            } else if (intent.getAction().equals(JSRUNNER_STARTED_INTENT)) {
                // JSRunner started executing new task
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerIntentFilter();
        setupUI();
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
    }

    private void setupUI() {
        requestAPI.getTasks();
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
    }

    private void registerIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(JSRUNNER_STOP_INTENT);
        filter.addAction(JSRUNNER_STARTED_INTENT);
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
    public void gotTasks(List<Task> tasks) {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES, 0);
        int task_id = preferences.getInt(CURRENT_TASK, INVALID_TASK_ID);

        if (task_id == INVALID_TASK_ID) {
            setCurrentTask(Task.NULL_TASK);
        } else {
            for (Task task : tasks) {
                if (task.getId() == task_id) {
                    setCurrentTask(task);
                }
            }
        }

        final ListView listView = (ListView) findViewById(R.id.taskListView);
        listView.setAdapter(new TaskListAdapter(this, tasks));
    }

    private void setCurrentTask(Task task) {
        TextView task_name = (TextView) findViewById(R.id.currentTaskName);
        task_name.setText(task.getName());

        TextView task_description = (TextView) findViewById(R.id.currentTaskDescription);
        task_description.setText(task.getDescription());
    }

    public void selectTaskHandler(View v) {
        RelativeLayout row = (RelativeLayout)v.getParent();
        Button button = (Button) row.findViewById(R.id.select_task);
        button.setText("Preferred Task");

        //ToDo: Do something useful
    }
}
