package com.phonemap.phonemap;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import com.phonemap.phonemap.services.JSRunner;

import static com.phonemap.phonemap.services.Utils.isServiceRunning;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment
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
                    } else {
                        Toast.makeText(getActivity(),
                                "No background process is running",
                                Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
        }
    }
}
