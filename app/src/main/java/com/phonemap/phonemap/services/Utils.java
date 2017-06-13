package com.phonemap.phonemap.services;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

public class Utils {
    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void startJSRunner(Context context) {
        if (!isServiceRunning(context, JSRunner.class)) {
            context.startService(new Intent(context, JSRunner.class));
        }
    }
}
