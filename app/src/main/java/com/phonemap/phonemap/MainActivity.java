package com.phonemap.phonemap;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.phonemap.phonemap.services.JSRunner;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onStartClick(View v) {
        if (!isServiceRunning(JSRunner.class)) {
            startService(new Intent(this, JSRunner.class));
            setStatus("Running");
            setButton("Stop");
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

    private void setStatus(String status) {
        final TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText("Status: " + status);
    }

    private void setButton(String text) {
        final Button button = (Button) findViewById(R.id.button);
        button.setText(text);
    }
}
