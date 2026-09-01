package com.masa34.nk225analyzer.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Nk225Preference {

    private static Nk225Preference instance;

    private static SharedPreferences preference;

    // コンストラクタ
    private Nk225Preference() {
    }

    public static Nk225Preference getInstance(Context context) {
        if (instance == null) {
            instance = new Nk225Preference();
        }
        preference = context.getSharedPreferences("nk225_prefs", Context.MODE_PRIVATE);
        return instance;
    }

    // 設定値更新
    private void UpdatePreference(String key, String value) {
        SharedPreferences.Editor editor = preference.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public boolean getDownloaded() {
        return Boolean.parseBoolean(preference.getString("downloaded", "false"));
    }

    public void setDownloaded(boolean downloaded) {
        UpdatePreference("downloaded", String.valueOf(downloaded));
    }

    // 自動ダウンロード
    public boolean isAutoDownload() {
        return preference.getBoolean("auto_download", false);
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
