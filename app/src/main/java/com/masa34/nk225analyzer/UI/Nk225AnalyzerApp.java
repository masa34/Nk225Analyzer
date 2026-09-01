package com.masa34.nk225analyzer.UI;

import android.app.Application;
import android.content.Context;
import androidx.room.Room;

import com.masa34.nk225analyzer.Db.Nk225AnalyzerDatabase;

public class Nk225AnalyzerApp extends Application {

    private static Nk225AnalyzerDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();

        db = Room.databaseBuilder(
            this,
            Nk225AnalyzerDatabase.class,
            "nk225.db"
        ).build();
    }

    public static Nk225AnalyzerDatabase getDatabase() {
        return db;
    }

    public static synchronized void resetDatabase(Context context) {
        if (db != null) {
            db.close();
        }

        context.deleteDatabase("nk225.db");

        db = Room.databaseBuilder(
            context,
            Nk225AnalyzerDatabase.class,
            "nk225.db"
        ).build();
    }
}
