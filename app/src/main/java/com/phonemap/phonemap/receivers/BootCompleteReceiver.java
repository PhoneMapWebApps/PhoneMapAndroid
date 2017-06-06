package com.phonemap.phonemap.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.phonemap.phonemap.services.JSRunner;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, JSRunner.class));
    }
}