package com.phonemap.phonemap.constants;


import android.content.Context;
import android.provider.Settings;

public class Phone {
    private Context inContext;

    public Phone(Context inContext) {
        this.inContext = inContext;
    }

    public String id() {
        return Settings.Secure.getString(inContext.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
