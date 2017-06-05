package com.phonemap.phonemap.services;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.liquidplayer.service.MicroService;

import static com.phonemap.phonemap.constants.API.ON_DESTROY;

public class ShutdownReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "ShutdownReceiver";
    private JSRunner runner;

    public ShutdownReceiver(JSRunner runner) {
        this.runner = runner;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        MicroService service = runner.getService();
        if  (service == null) {
            Log.i(LOG_TAG, "Wanted to send onDestroy but service isn't running");
            return;
        }

        String intendedAction = intent.getAction();
        if (intendedAction.equals(Intent.ACTION_SHUTDOWN)) {
            service.emit(ON_DESTROY, true);
        } else if (intendedAction.equals(Intent.ACTION_SCREEN_ON) || intendedAction.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            service.emit(ON_DESTROY, false);
        }
    }
}
