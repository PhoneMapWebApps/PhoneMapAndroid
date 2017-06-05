package com.phonemap.phonemap.services;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.liquidplayer.service.MicroService;

import static com.phonemap.phonemap.constants.API.ON_DESTROY;

public class ShutdownReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "ShutdownReceiver";
    private MicroService service;

    public ShutdownReceiver(MicroService service) {
        this.service = service;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String intendedAction = intent.getAction();
        if  (service == null) {
            Log.i(LOG_TAG, "Wanted to send onDestroy but service isn't running");
        } else if (intendedAction.equals(Intent.ACTION_SHUTDOWN)) {
            service.emit(ON_DESTROY, true);
        } else if (intendedAction.equals(Intent.ACTION_SCREEN_ON) || intendedAction.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            service.emit(ON_DESTROY, false);
        }
    }
}
