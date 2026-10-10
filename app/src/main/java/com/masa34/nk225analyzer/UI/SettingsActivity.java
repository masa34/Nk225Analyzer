package com.masa34.nk225analyzer.UI;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.CheckBoxPreference;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.util.Log;
import android.view.KeyEvent;

import com.masa34.nk225analyzer.R;
import com.masa34.nk225analyzer.Util.Nk225Preference;

public class SettingsActivity extends AppCompatActivity {

    private final String TAG = "SettingsActivity";

    private int displayPeriod;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_settings);

        EdgeToEdge.enable(this);

        View containerView = findViewById(R.id.linear_layout);
        if (containerView != null) {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> {
                Intent intent = new Intent();
                intent.putExtra(
                "displayPeriodChanged",
                displayPeriod != Nk225Preference.getInstance(SettingsActivity.this).getDisplayPeriod()
                );
                setResult(RESULT_OK, intent);
                finish();
            });

            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();

            if (getSupportActionBar() != null) {
                // 戻るボタン（←）だけを表示する
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            ViewCompat.setOnApplyWindowInsetsListener(containerView, (v, windowInsets) -> {
                Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

                View appBarLayout = findViewById(R.id.app_bar_layout);

                // ツールバーの上部に、ステータスバーの高さ分のパディングを設定する
                appBarLayout.setPadding(
                    appBarLayout.getPaddingLeft(),
                    systemBars.top,
                    appBarLayout.getPaddingRight(),
                    appBarLayout.getPaddingBottom()
                );

                return windowInsets;
            });
        }
        //getWindow().setStatusBarColor(Color.WHITE);


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
            Log.d(TAG, "onCreatePreferences");

            setPreferencesFromResource(R.xml.settings, rootKey);

            // --- auto_download の保存処理
            CheckBoxPreference autoDownloadPref = findPreference("auto_download");
            autoDownloadPref.setOnPreferenceChangeListener((pref, newValue) -> {
                boolean enabled = (Boolean) newValue;
                Nk225Preference.getInstance(context).setAutoDownload(enabled);
                return true;
            });

            // --- display_period の保存処理
            ListPreference displayPeriodPref = findPreference("display_period");
            int current = Nk225Preference.getInstance(context).getDisplayPeriod();
            displayPeriodPref.setValue(String.valueOf(current));
            displayPeriodPref.setSummaryProvider(pref -> {
                ListPreference lp = (ListPreference) pref;
                return "指定した期間の日経平均データを表示します\n設定値：" + lp.getEntry();
            });

            displayPeriodPref.setOnPreferenceChangeListener((pref, newValue) -> {
                int period = Integer.parseInt((String) newValue);
                Nk225Preference.getInstance(context).setDisplayPeriod(period);
                return true;
            });

            // プライバシーポリシー
            Preference privacy = findPreference("privacy_policy");
            privacy.setOnPreferenceClickListener(this);

            // バージョン番号表示
            Preference version = findPreference("version");
            try {
                PackageInfo info = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
                version.setSummary(info.versionName);
            } catch (Exception ignored) {}
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = super.onCreateView(inflater, container, savedInstanceState);
            Log.d(TAG, "onCreateView");
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
                    Uri uri = Uri.parse("https://masa34.github.io/Nk225Analyzer/privacy-policy.html");
                    Intent i = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(i);
                    break;
            }
            return false;
        }
    }
}
