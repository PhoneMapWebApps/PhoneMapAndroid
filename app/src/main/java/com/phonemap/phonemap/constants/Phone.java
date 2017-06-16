package com.phonemap.phonemap.constants;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import static android.content.Context.POWER_SERVICE;

public class Phone {
    private Context inContext;

    public Phone(Context inContext) {
        this.inContext = inContext;
    }

    public String id() {
        return Settings.Secure.getString(inContext.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public boolean isScreenOn() {
        PowerManager powerManager = (PowerManager)
                inContext.getSystemService(POWER_SERVICE);
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH
                && powerManager.isInteractive()
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH
                && powerManager.isScreenOn();
    }

    public boolean isCharging() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = inContext.registerReceiver(null, filter);
        if (batteryStatus == null) {
            // Todo: Investigate the possibility if further necessary handling.
            // If we don't know the battery status, ASSUME that it isn't charging
            return false;
        }
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
    }

    public boolean isConnectedViaWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                inContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }
}
