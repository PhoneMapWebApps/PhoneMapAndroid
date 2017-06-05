package com.phonemap.phonemap;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.ListView;

import com.phonemap.phonemap.adapters.TaskListAdapter;
import com.phonemap.phonemap.objects.Task;
import com.phonemap.phonemap.services.JSRunner;

import java.util.ArrayList;

import static com.phonemap.phonemap.constants.Intents.JSRUNNER_STARTED_INTENT;
import static com.phonemap.phonemap.constants.Intents.JSRUNNER_STOP_INTENT;

public class MainActivity extends AppCompatActivity {

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
        ArrayList<Task> searchResults = getTasks();

        final ListView listView = (ListView) findViewById(R.id.taskListView);
        listView.setAdapter(new TaskListAdapter(this, searchResults));

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
    }


    private void registerIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(JSRUNNER_STOP_INTENT);
        filter.addAction(JSRUNNER_STARTED_INTENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, filter);
    }

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

    private ArrayList<Task> getTasks(){
        // ToDo: Replace by actual values not placeholders

        ArrayList<Task> arrayList = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            String name = "Task " + String.valueOf(i);
            String description = "Description " + String.valueOf(i) + "\nEven more description.";
            arrayList.add(new Task(name, description, i));
        }

        return arrayList;
    }
}
