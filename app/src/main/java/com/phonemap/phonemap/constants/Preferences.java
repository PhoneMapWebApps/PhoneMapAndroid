package com.phonemap.phonemap.constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static android.content.Context.MODE_PRIVATE;

public class Preferences {
    public static final String PREFERENCES = "PhoneMapPreferences";

    public static final String CURRENT_TASK = "current_task";
    public static final String SCREEN_ON = "screen_on";
    public static final int INVALID_TASK_ID = -1;
    private static final String AUTOSTART = "autostart";
    public static final String ALLOW_MOBILE = "allow_mobile";
    public static final String ONLY_CONNECTED = "only_connected";
    private Context inContext;

    public Preferences(Context inContext) {
        this.inContext = inContext;
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
