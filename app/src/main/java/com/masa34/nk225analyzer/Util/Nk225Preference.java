package com.masa34.nk225analyzer.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Nk225Preference {

    private static Nk225Preference instance;

    private SharedPreferences preference;

    // コンストラクタ
    private Nk225Preference() {
    }

    public static Nk225Preference getInstance(Context context) {
        if (instance == null) {
            instance = new Nk225Preference();
        }
        instance.preference = context.getApplicationContext().getSharedPreferences("nk225_prefs", Context.MODE_PRIVATE);
        return instance;
    }

    // 設定値の管理方法変更に伴うデータ移行
    public void upgradePreferences(Context context) {
        SharedPreferences oldPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences newPrefs = preference;

        // display_period の移行
        if (!newPrefs.contains("display_period")) {
            int oldValue = oldPrefs.getInt("display_period", 0);
            newPrefs.edit().putString("display_period", String.valueOf(oldValue)).apply();
        }

        // auto_download の移行
        if (!newPrefs.contains("auto_download")) {
            boolean oldAuto = oldPrefs.getBoolean("auto_download", false);
            newPrefs.edit().putBoolean("auto_download", oldAuto).apply();
        }
    }

    // 設定値更新
    private void UpdatePreference(String key, String value) {
        SharedPreferences.Editor editor = preference.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public boolean getDownloaded() {
        return preference.getBoolean("downloaded", false);
    }

    public void setDownloaded(boolean downloaded) {
        SharedPreferences.Editor editor = preference.edit();
        editor.putBoolean("downloaded", downloaded);
        editor.apply();
    }

    // 自動ダウンロード
    public boolean isAutoDownload() {
        return preference.getBoolean("auto_download", false);
    }

    public void setAutoDownload(boolean enabled) {
        SharedPreferences.Editor editor = preference.edit();
        editor.putBoolean("auto_download", enabled);
        editor.apply();
    }

    // 表示期間
    public int getDisplayPeriod() {
        return Integer.parseInt(preference.getString("display_period", "0"));
    }

    public void setDisplayPeriod(int period) {
        UpdatePreference("display_period", String.valueOf(period));
    }

    // 「レビュー」クリック日付
    public String getReviewDate() {
        return preference.getString("review_date", "");
    }

    public void setReviewDate(String date) {
        UpdatePreference("review_date", date);
    }

    // 「あとで」クリック日付
    public String getLaterDate() {
        return preference.getString("later_date", "");
    }

    public void setLaterDate(String date) {
        UpdatePreference("later_date", date);
    }

    // DBスキーマバージョン
    public int getSchemaVersion() {
        return Integer.parseInt(preference.getString("schema_version", "0"));
    }

    public void setSchemaVersion(int version) {
        UpdatePreference("schema_version", String.valueOf(version));
    }
}
