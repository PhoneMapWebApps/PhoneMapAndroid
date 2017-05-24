package com.phonemap.phonemap.wrappers;

import android.content.IntentFilter;

public class IntentFilterBuilder extends IntentFilter {
    private IntentFilter intentFilter;

    public IntentFilterBuilder() {
        intentFilter = new IntentFilter();
    }

    public IntentFilterBuilder withAction(String action) {
        intentFilter.addAction(action);
        return this;
    }

    public IntentFilter build() {
        return intentFilter;
    }
}
