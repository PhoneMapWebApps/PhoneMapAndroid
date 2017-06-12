package com.phonemap.phonemap.constants;

import android.content.Context;
import android.preference.PreferenceManager;

import static android.content.Context.MODE_PRIVATE;

public class Preferences {
    public static final String PREFERENCES = "PhoneMapPreferences";

    public static final String CURRENT_TASK = "current_task";
    public static final int INVALID_TASK_ID = -1;
    private static final String AUTOSTART = "autostart";
    private Context inContext;

    public Preferences(Context inContext) {
        this.inContext = inContext;
    }

    public int preferredTask() {
        return inContext.getSharedPreferences(PREFERENCES, MODE_PRIVATE).getInt(CURRENT_TASK, INVALID_TASK_ID);
    }

    public boolean autostartEnabled() {
        return !PreferenceManager.getDefaultSharedPreferences(inContext).getBoolean(AUTOSTART, true);
    }
}
