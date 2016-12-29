package com.masa34.nk225analyzer.UI;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.masa34.nk225analyzer.R;

public class SettingsActivity extends PreferenceActivity {

    //private final String TAG = "SettingsActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment {

        private final String TAG = "SettingsFragment";

        private Context context;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            PackageManager pm = context.getPackageManager();
            try {
                PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
                PreferenceScreen pref = (PreferenceScreen)getPreferenceScreen().findPreference("version");
                pref.setSummary(packageInfo.versionName);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, e.toString());
            }
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            Log.d(TAG, "onAttach");

            this.context = context;
        }

        @Override
        public void onDetach() {
            super.onDetach();
            Log.d(TAG, "onDetach");
        }
    }
}
