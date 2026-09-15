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

    private int safeGetInt(String key, int defaultValue) {
        Object value = preference.getAll().get(key);

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof String) {
            try {
                int v = Integer.parseInt((String) value);
                preference.edit().putInt(key, v).commit();
                return v;
            } catch (Exception e) {
                preference.edit().remove(key).commit();
                return defaultValue;
            }
        }

        return defaultValue;
    }

    private boolean safeGetBoolean(String key, boolean defaultValue) {
        Object value = preference.getAll().get(key);

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof String) {
            boolean b = Boolean.parseBoolean((String) value);
            preference.edit().putBoolean(key, b).commit();
            return b;
        }

        return defaultValue;
    }

    // 設定値の管理方法変更に伴うデータ移行
    public void upgradePreferences(Context context) {
        SharedPreferences oldPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences newPrefs = preference;

        // display_period の移行
        if (!newPrefs.contains("display_period")) {
            String raw = oldPrefs.getString("display_period", "0");
            int oldValue;
            try {
                oldValue = Integer.parseInt(raw);
            } catch (Exception e) {
                oldValue = 0;
            }
            newPrefs.edit().putInt("display_period", oldValue).commit();
        }

        // auto_download の移行
        if (!newPrefs.contains("auto_download")) {
            Object raw = oldPrefs.getAll().get("auto_download");
            boolean oldAuto = false;

            if (raw instanceof Boolean) {
                oldAuto = (Boolean) raw;
            } else if (raw instanceof String) {
                oldAuto = Boolean.parseBoolean((String) raw);
            }

            newPrefs.edit().putBoolean("auto_download", oldAuto).commit();
        }

        // 旧設定値ファイルを削除
        String fileName = PreferenceManager.getDefaultSharedPreferencesName(context);
        SharedPreferences prefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    // 初回ダウンロード済みフラグ
    public boolean getDownloaded() {
        return safeGetBoolean("downloaded", false);
    }

    public void setDownloaded(boolean downloaded) {
        preference.edit().putBoolean("downloaded", downloaded).apply();
    }

    // 自動ダウンロード
    public boolean isAutoDownload() {
        return safeGetBoolean("auto_download", false);
    }

    public void setAutoDownload(boolean enabled) {
        preference.edit().putBoolean("auto_download", enabled).apply();
    }

    // 表示期間
    public int getDisplayPeriod() {
        return safeGetInt("display_period", 0);
    }

    public void setDisplayPeriod(int period) {
        preference.edit().putInt("display_period", period).apply();
    }

    // 「レビュー」クリック日付
    public String getReviewDate() {
        return preference.getString("review_date", "");
    }

    public void setReviewDate(String date) {
        preference.edit().putString("review_date", date).apply();
    }

    // 「あとで」クリック日付
    public String getLaterDate() {
        return preference.getString("later_date", "");
    }

    public void setLaterDate(String date) {
        preference.edit().putString("later_date", date).apply();
    }

    // DBスキーマバージョン
    public int getSchemaVersion() {
        return safeGetInt("schema_version", 0);
    }

    public void setSchemaVersion(int version) {
        preference.edit().putInt("schema_version", version).apply();
    }
}
