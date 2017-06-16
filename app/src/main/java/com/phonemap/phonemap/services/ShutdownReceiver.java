package com.phonemap.phonemap.services;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import org.liquidplayer.service.MicroService;

import static com.phonemap.phonemap.constants.API.ON_DESTROY;

public class ShutdownReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "ShutdownReceiver";
    private MicroService service;
    private JSRunner runner;

    public ShutdownReceiver(MicroService service, JSRunner runner) {
        this.service = service;
        this.runner = runner;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String intendedAction = intent.getAction();

        if (service != null) {
            if (intendedAction.equals(Intent.ACTION_SHUTDOWN)) {
                service.emit(ON_DESTROY, true);
            } else if (intendedAction.equals(Intent.ACTION_SCREEN_ON) ||
                    intendedAction.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                service.emit(ON_DESTROY, false);
            } else if (intendedAction.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION) &&
                    intendedAction.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION) &&
                    !intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
                service.emit(ON_DESTROY, false);
            }
        }

        if (intendedAction.equals(Intent.ACTION_POWER_CONNECTED) ||
                intendedAction.equals(Intent.ACTION_SCREEN_OFF) ||
                intendedAction.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION) &&
                        intendedAction.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION) &&
                        !intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
            runner.requestNewSubtask();
        }
    }
}
