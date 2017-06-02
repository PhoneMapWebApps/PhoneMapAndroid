package com.phonemap.phonemap;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.phonemap.phonemap.services.JSRunner;

import java.text.DateFormat;
import java.util.Date;

import static com.phonemap.phonemap.constants.Intents.JSRUNNER_STARTED_INTENT;
import static com.phonemap.phonemap.constants.Intents.JSRUNNER_STOP_INTENT;
import static com.phonemap.phonemap.constants.Preferences.PREFERENCES;
import static com.phonemap.phonemap.constants.Preferences.RUN_AUTOMATICALLY;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerIntentFilter();

        settings = getSharedPreferences(PREFERENCES, 0);
        boolean automatically = settings.getBoolean(RUN_AUTOMATICALLY, false);
        CheckBox checkbox = (CheckBox) findViewById(R.id.checkBox);
        checkbox.setChecked(automatically);

        if (automatically) {
            startService(new Intent(this, JSRunner.class));
        }

        checkForUpdates();
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

    public void onStartClick(View v) {
        CheckBox checkbox = (CheckBox) findViewById(R.id.checkBox);
        Boolean checked = checkbox.isChecked();

        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(RUN_AUTOMATICALLY, checked);
        editor.apply();

        if (checked) {
            startService(new Intent(this, JSRunner.class));
        }
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
                logStatus("Stopped");
            } else if (intent.getAction().equals(JSRUNNER_STARTED_INTENT)) {
                logStatus("Started");
            }
        }
    };

    private void logStatus(String status) {
        final TextView textView = (TextView) findViewById(R.id.textView);

        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
        String log = currentDateTimeString + ": " + status + "\n" + textView.getText();

        textView.setText(log);
    }


    private void checkForCrashes() {
        //CrashManager.register(this);
    }

    private void checkForUpdates() {
        // Remove this for store builds!
        //UpdateManager.register(this);
    }

    private void unregisterManagers() {
        //UpdateManager.unregister();
    }
}
