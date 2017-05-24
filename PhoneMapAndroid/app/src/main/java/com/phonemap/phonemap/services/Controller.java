package com.phonemap.phonemap.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class Controller extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startService(new Intent(getApplicationContext(), JSRunner.class));
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
