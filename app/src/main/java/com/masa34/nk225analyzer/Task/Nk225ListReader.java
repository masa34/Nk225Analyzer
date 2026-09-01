package com.masa34.nk225analyzer.Task;

import android.content.Context;
import androidx.loader.content.AsyncTaskLoader;
import android.util.Log;

import com.masa34.nk225analyzer.UI.Nk225AnalyzerApp;
import com.masa34.nk225analyzer.Db.Dao.Nk225EntityDao;
import com.masa34.nk225analyzer.Db.Entity.Nk225Entity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Nk225ListReader extends AsyncTaskLoader<List<Nk225Entity>> {

    private final String TAG = "Nk225ListReader";

    public Nk225ListReader(Context context) {
        super(context);
        Log.d(TAG, "Nk225ListReader");
    }

    @Override
    public List<Nk225Entity> loadInBackground() {

        Log.d(TAG, "loadInBackground");

        try {
            Nk225EntityDao dao = Nk225AnalyzerApp.getDatabase().nk225EntityDao();

            // ※日付は要調整
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd");
            Date date = fmt.parse("2016/01/01");

            return dao.findAfter(date);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}
