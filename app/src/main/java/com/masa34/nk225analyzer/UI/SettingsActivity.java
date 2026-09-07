package com.masa34.nk225analyzer.UI;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.ListPreference;
import androidx.preference.CheckBoxPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.util.Log;
import android.view.KeyEvent;

import com.masa34.nk225analyzer.R;
import com.masa34.nk225analyzer.Util.Nk225Preference;

public class SettingsActivity extends AppCompatActivity {

    int displayPeriod;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        displayPeriod = Nk225Preference.getInstance(this).getDisplayPeriod();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (e.getAction() == KeyEvent.ACTION_DOWN) {
                // 戻るボタンが押された場合

                // 結果を設定
                Intent intent = new Intent();
                intent.putExtra("displayPeriodChanged", displayPeriod != Nk225Preference.getInstance(this).getDisplayPeriod());
                setResult(RESULT_OK, intent);
                finish();
                return true;
            }
        }
        return super.dispatchKeyEvent(e);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {

        private final String TAG = "SettingsFragment";

        private Context context;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings, rootKey);

            // --- display_period の保存処理
            ListPreference displayPeriodPref = findPreference("display_period");
            int current = Nk225Preference.getInstance(context).getDisplayPeriod();
            displayPeriodPref.setValue(String.valueOf(current));

            displayPeriodPref.setOnPreferenceChangeListener((pref, newValue) -> {
                int period = Integer.parseInt((String) newValue);
                Nk225Preference.getInstance(context).setDisplayPeriod(period);
                return true;
            });

            // --- auto_download の保存処理
            CheckBoxPreference autoDownloadPref = findPreference("auto_download");
            autoDownloadPref.setOnPreferenceChangeListener((pref, newValue) -> {
                boolean enabled = (Boolean) newValue;
                Nk225Preference.getInstance(context).setAutoDownload(enabled);
                return true;
            });

            // バージョン番号表示
            PreferenceScreen version = findPreference("version");
            try {
                PackageInfo info = context.getPackageManager()
                        .getPackageInfo(context.getPackageName(), 0);
                version.setSummary(info.versionName);
            } catch (Exception ignored) {}

            // プライバシーポリシー
            PreferenceScreen privacy = findPreference("privacy_policy");
            privacy.setOnPreferenceClickListener(this);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = super.onCreateView(inflater, container, savedInstanceState);
            v.setFitsSystemWindows(true);
            return v;
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

        @Override
        public boolean onPreferenceClick(Preference preference) {

            switch (preference.getKey()) {
                case "privacy_policy":
                    Uri uri = Uri.parse("http://masapu.cocolog-nifty.com/kabu/2018/09/post-80a7.html");
                    Intent i = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(i);
                    break;
            }

            return false;
        }
    }
}
