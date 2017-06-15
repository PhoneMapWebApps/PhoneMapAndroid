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
    public static final String SCREEN_ON = "screen_on";
    public static final int INVALID_TASK_ID = -1;
    private static final String AUTOSTART = "autostart";
    public static final String ALLOW_MOBILE = "allow_mobile";
    public static final String ONLY_CONNECTED = "only_connected";
    public static final String LAST_INTENT = "last_intent";
    private Context inContext;

    public Preferences(Context inContext) {
        this.inContext = inContext;
    }

    public SharedPreferences getSharedPref() {
        return inContext.getSharedPreferences(PREFERENCES, MODE_PRIVATE);
    }

    public void saveIntent(Intent intent) {
        getSharedPref().edit().putString(LAST_INTENT, intent.toURI()).commit();
    }

    public Intent getLastIntent() {
        String uri = getSharedPref().getString(LAST_INTENT, "");

        try {
            return Intent.parseUri(uri, 0);
        } catch (URISyntaxException e) {
            return new Intent();
        }
    }

    public int preferredTask() {
        return inContext.getSharedPreferences(PREFERENCES, MODE_PRIVATE).getInt(CURRENT_TASK, INVALID_TASK_ID);
    }

    public SharedPreferences getDefaultPreference() {
        return PreferenceManager.getDefaultSharedPreferences(inContext);
    }

    public boolean autostartEnabled() {
        return !getDefaultPreference().getBoolean(AUTOSTART, true);
    }

    public boolean enableWhenScreenOn() {
        return getDefaultPreference().getBoolean(SCREEN_ON, true);
    }

    public boolean enableOnMobile() {
        return getDefaultPreference().getBoolean(ALLOW_MOBILE, false);
    }

    public boolean enableWhenNoPower() {
        return !getDefaultPreference().getBoolean(ONLY_CONNECTED, true);
    }
}
