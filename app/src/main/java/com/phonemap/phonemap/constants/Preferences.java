package com.phonemap.phonemap.constants;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.net.URISyntaxException;

import static android.content.Context.MODE_PRIVATE;

public class Preferences {
    public static final String PREFERENCES = "PhoneMapPreferences";

    public static final String CURRENT_TASK = "current_task";
    public static final int INVALID_TASK_ID = -1;
    private static final String SCREEN_ON = "screen_on";
    private static final String ALLOW_MOBILE = "allow_mobile";
    private static final String RUN_DISCONNECTED = "run_disconnected";
    private static final String LAST_INTENT = "last_intent";
    private static final String AUTOSTART = "autostart";
    private Context inContext;

    public Preferences(Context inContext) {
        this.inContext = inContext;
    }

    private SharedPreferences getSharedPreferences() {
        return inContext.getSharedPreferences(PREFERENCES, MODE_PRIVATE);
    }

    public void saveIntent(Intent intent) {
        getSharedPreferences().edit().putString(LAST_INTENT, intent.toURI()).commit();
    }

    public Intent getLastIntent() {
        String uri = getSharedPreferences().getString(LAST_INTENT, "");

        try {
            return Intent.parseUri(uri, 0);
        } catch (URISyntaxException e) {
            return new Intent();
        }
    }

    public int preferredTask() {
        return inContext.getSharedPreferences(PREFERENCES, MODE_PRIVATE).getInt(CURRENT_TASK, INVALID_TASK_ID);
    }

    private SharedPreferences getDefaultPreference() {
        return PreferenceManager.getDefaultSharedPreferences(inContext);
    }

    public boolean autostartEnabled() {
        return !getDefaultPreference().getBoolean(AUTOSTART, true);
    }

    public boolean satisfied(Phone phone) {
        return phone.isScreenOn() && !getDefaultPreference().getBoolean(SCREEN_ON, false) ||
                !phone.isCharging() && !getDefaultPreference().getBoolean(RUN_DISCONNECTED, false) ||
                !phone.isConnectedViaWifi() && !getDefaultPreference().getBoolean(ALLOW_MOBILE, false);
    }
}
