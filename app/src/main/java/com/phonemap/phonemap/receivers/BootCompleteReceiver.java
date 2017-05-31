package com.phonemap.phonemap.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.phonemap.phonemap.services.JSRunner;

import static com.phonemap.phonemap.constants.Preferences.PREFERENCES;
import static com.phonemap.phonemap.constants.Preferences.RUN_AUTOMATICALLY;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCES, 0);
        boolean automatically = settings.getBoolean(RUN_AUTOMATICALLY, false);

        if (automatically) {
            context.startService(new Intent(context, JSRunner.class));
        }
    }
}