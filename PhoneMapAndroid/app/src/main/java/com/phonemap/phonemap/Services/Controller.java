package com.phonemap.phonemap.services;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class Controller extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(receiver, filter);

        return Service.START_NOT_STICKY;
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                Log.i("LOG", "Screen On");
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Log.i("LOG", "Screen Off");
            } else if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
                Log.i("LOG", "Power Connected");
            } else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
                Log.i("LOG", "Power Disconnected");
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
