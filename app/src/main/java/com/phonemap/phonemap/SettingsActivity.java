package com.phonemap.phonemap;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.phonemap.phonemap.services.JSRunner;

import static com.phonemap.phonemap.constants.Intents.JSRUNNER_STOP_INTENT;
import static com.phonemap.phonemap.constants.Intents.PREFERENCES_CHANGED;
import static com.phonemap.phonemap.services.JSRunner.broadcastState;
import static com.phonemap.phonemap.services.Utils.isServiceRunning;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            Preference button = findPreference(getString(R.string.stop));
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (isServiceRunning(getActivity(), JSRunner.class)) {
                        getActivity().stopService(new Intent(getActivity(), JSRunner.class));
                        Toast.makeText(getActivity(),
                                "All background processes have been destroyed",
                                Toast.LENGTH_SHORT).show();
                        broadcastState(getActivity(), new Intent(JSRUNNER_STOP_INTENT));
                    } else {
                        Toast.makeText(getActivity(),
                                "No background process is running",
                                Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {
            Intent intent = new Intent(PREFERENCES_CHANGED);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        }
    }
}
