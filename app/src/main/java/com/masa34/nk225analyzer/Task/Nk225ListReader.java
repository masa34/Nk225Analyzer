package com.masa34.nk225analyzer.Task;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.masa34.nk225analyzer.Stock.Nk225Entity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class Nk225ListReader extends AsyncTaskLoader<List<Nk225Entity>> {

    private final String TAG = "Nk225ListReader";

    public Nk225ListReader(Context context) {
        super(context);
        Log.d(TAG, "Nk225ListReader");
    }

    @Override
    public List<Nk225Entity> loadInBackground() {

        Log.d(TAG, "loadInBackground");

        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            // ※日付は要調整
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd");
            Date date = fmt.parse("2016/01/01");

            RealmResults<Nk225Entity> nk225Entities = realm.where(Nk225Entity.class)
                    .greaterThan("date", date)
                    .findAllSorted("date", Sort.ASCENDING);

            return realm.copyFromRealm(nk225Entities);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        return null;
    }
}
