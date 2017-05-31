package com.phonemap.phonemap;

import android.app.ActivityManager;
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

import static com.phonemap.phonemap.constants.Intents.JSRUNNER_STOP_INTENT;
import static com.phonemap.phonemap.constants.Preferences.PREFERENCES;
import static com.phonemap.phonemap.constants.Preferences.RUN_AUTOMATICALLY;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction(JSRUNNER_STOP_INTENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, filter);

        settings = getSharedPreferences(PREFERENCES, 0);
        boolean automatically = settings.getBoolean(RUN_AUTOMATICALLY, false);
        CheckBox checkbox = (CheckBox) findViewById(R.id.checkBox);
        checkbox.setChecked(automatically);

        changeServiceState(automatically);
    }

    public void onStartClick(View v) {
        CheckBox checkbox = (CheckBox) findViewById(R.id.checkBox);
        Boolean checked = checkbox.isChecked();

        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(RUN_AUTOMATICALLY, checked);
        editor.apply();

        changeServiceState(checked);
    }

    private void changeServiceState(boolean start) {
        if (start) {
            if (!isServiceRunning(JSRunner.class)) {
                startService(new Intent(this, JSRunner.class));
                logStatus("Running");
            }
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(JSRUNNER_STOP_INTENT)) {
                logStatus("Stopped");
            }
        }
    };

    private void logStatus(String status) {
        final TextView textView = (TextView) findViewById(R.id.textView);

        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
        String log = currentDateTimeString + ": " + status + "\n" + textView.getText();

        textView.setText(log);
    }
}
