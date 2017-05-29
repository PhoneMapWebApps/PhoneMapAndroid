package com.phonemap.phonemap.utils;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

public class Utils {
    private static final String LOG_TAG = "Utils";

    public static JSONObject bundleToJSON(Bundle bundle) {
        JSONObject json = new JSONObject();

        if (bundle == null) {
            return json;
        }

        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            try {
                json.put(key, bundle.get(key));
            } catch(JSONException e) {
                Log.e(LOG_TAG, "Cannot create JSONObject out of provided bundle");
            }
        }

        return json;
    }
}
