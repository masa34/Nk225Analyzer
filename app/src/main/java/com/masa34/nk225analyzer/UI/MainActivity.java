package com.masa34.nk225analyzer.UI;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.masa34.nk225analyzer.R;
import com.masa34.nk225analyzer.Stock.Candlestick;
import com.masa34.nk225analyzer.Stock.Nk225Entity;
import com.masa34.nk225analyzer.Stock.StockUtils;
import com.masa34.nk225analyzer.Task.AbstractNk225DownloadProcess;
import com.masa34.nk225analyzer.Task.Nk225ListReader;
import com.masa34.nk225analyzer.Util.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmResults;
import io.realm.RealmSchema;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<List<Nk225Entity>> {

    private final String TAG = "MainActivity";

    private SwipeRefreshLayout swipeRefreshLayout;
    private Nk225PagerAdapter pagerAdapter;

    private AdView adView;

    private boolean isStartup = false;
    private boolean isVisible = false;
    private boolean needReflesh = false;

    private static final int REQUEST_CODE = 1;

    private boolean isAutoDownload() {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);

        return preference.getBoolean("auto_download", false);
    }

    private int getDisplayPeriod() {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);

        return Integer.parseInt(preference.getString("display_period", "0"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        // Realm初期化
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder(this)
                .schemaVersion(2)
                .migration(new RealmMigration() {
                    @Override
                    public void migrate(final DynamicRealm realm, long oldVersion, long newVersion) {
                        RealmSchema schema = realm.getSchema();

                        // Version 0 to 1
                        if (oldVersion == 0) {
                            // CandlestickテーブルにmarketClosingカラムを追加
                            schema.get("Candlestick")
                                    .addField("marketClosing", boolean.class, FieldAttribute.REQUIRED)
                                    .transform(new RealmObjectSchema.Function() {
                                        @Override
                                        public void apply(DynamicRealmObject obj) {
                                            obj.setBoolean("marketClosing", true);
                                        }
                                    });

                            // Nk225EntityテーブルにmarketClosingカラムを追加
                            schema.get("Nk225Entity")
                                    .addField("marketClosing", boolean.class, FieldAttribute.REQUIRED)
                                    .transform(new RealmObjectSchema.Function() {
                                        @Override
                                        public void apply(DynamicRealmObject obj) {
                                            obj.setBoolean("marketClosing", true);
                                        }
                                    });

                            oldVersion++;
                        }

                        // Version 1 to 2
                        if (oldVersion == 1) {
                            // Nk225EntityテーブルにpriceRange,priceRangeAverage20カラムを追加
                            schema.get("Nk225Entity")
                                    .addField("priceRange", double.class, FieldAttribute.REQUIRED)
                                    .addField("priceRangeAverage20", double.class, FieldAttribute.REQUIRED)
                                    .transform(new RealmObjectSchema.Function() {
                                        @Override
                                        public void apply(DynamicRealmObject obj) {
                                            obj.setDouble("priceRange", 0.0);
                                            obj.setDouble("priceRangeAverage20", 0.0);
                                        }
                                    });

                            oldVersion++;
                        }
                    }
                })
                .build());

        // データ移行
        if (!migareteDb()) {
            new AlertDialog.Builder(this)
                .setTitle("お知らせ")
                .setMessage("データベースの移行に失敗しました。\r\n再度データをダウンロードしてください")
                .show();

            //// DB削除
            //RealmConfiguration config = new RealmConfiguration.Builder(this).build();
            //Realm.deleteRealm(config);
            //Realm.setDefaultConfiguration(config);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_widget);
        swipeRefreshLayout.setColorSchemeResources(R.color.red, R.color.green, R.color.blue, R.color.orange);
        swipeRefreshLayout.setOnRefreshListener(this);

        PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.tab_strip);
        pagerTabStrip.setVisibility(View.INVISIBLE);

        // 広告初期化
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-0365443143303373~5579598474");
        adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        isStartup = true;
        needReflesh = false;

        //new AlertDialog.Builder(this)
        //        .setTitle("お知らせ")
        //        .setMessage("ただいまサービス停止中につきデータの取得ができません。\r\n復旧までいましばらくお待ちください")
        //        .show();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");

        needReflesh = false;

        adView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG,"onRestoreInstanceState");

        isStartup = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        isVisible = true;

        adView.resume();

        if (downloader != null && downloader.isInProcess()) {
            showProgressDialog(this);
            return;
        }

        if (isStartup) {
            if (isAutoDownload()) {
                // 自動ダウンロード
                downloader = new AutoNk225DownloadProcess();
                downloader.execute();
            } else {
                getSupportLoaderManager().initLoader(0, null, this);
            }
        }

        if (needReflesh) {
            needReflesh = false;

            onRefresh();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");

        isStartup = false;

        if (downloader != null && downloader.isInProcess()) {
            // アクティビティが消える前にダイアログを終了させる
            dismissProgressDialog();
        }

        isVisible = false;

        adView.pause();
        super.onPause();
    }

    // BACKボタンが押された時の処理
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown");

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 評価の訴求
            SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
            String review = preference.getString("review_date", "");
            if (review.isEmpty()) {
                boolean dispAler = false;
                String later = preference.getString("later_date", "");
                if (later.isEmpty()) {
                    dispAler = true;
                }
                else {
                    try {
                        // 前回「あとで」を選択してから3日以上経過していたら再度表示
                        Date nowDate = DateUtils.getNow();
                        Date laterDate = DateUtils.convertToDate(later, "yyyy/MM/dd");
                        int diffDay = DateUtils.DifferenceDays(nowDate, laterDate);
                        if (diffDay > 3) {
                            dispAler = true;
                        }
                    }
                    catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }

                if (dispAler) {
                    new AlertDialog.Builder(this)
                        .setTitle("評価のお願い")
                        .setMessage("ご利用ありがとうございます\n開発の励みになるので、良ければ★5のレビューをお願いします")
                        .setPositiveButton("評価する", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd");
                                SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                SharedPreferences.Editor editor = preference.edit();
                                editor.putString("review_date", fmt.format(DateUtils.getNow()));
                                editor.commit();

                                // レビュー画面を表示
                                Intent intent = new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=com.masa34.nk225analyzer"));
                                startActivity(intent);

                                MainActivity.this.finish();
                            }
                        })
                        .setNegativeButton("あとで", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd");
                                SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                SharedPreferences.Editor editor = preference.edit();
                                editor.putString("later_date", fmt.format(DateUtils.getNow()));
                                editor.commit();

                                MainActivity.this.finish();
                            }
                        })
                        .show();

                    return true;
                }
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");

        switch (item.getItemId()) {
            case R.id.action_download:
                downloader = new ManualNk225DownloadProcess();
                downloader.execute();
                return true;

            case R.id.action_settings:
                Intent intent = new android.content.Intent(this, SettingsActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                return true;

            case R.id.action_db_init:
                RealmConfiguration realmConfig = new RealmConfiguration.Builder(this).build();
                Realm.deleteRealm(realmConfig);
                Realm.setDefaultConfiguration(realmConfig);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        Log.d(TAG, "onRefresh");

        // 非同期処理
        getSupportLoaderManager().restartLoader(0, null, this);

        // 更新が終了したらインジケータ非表示
        swipeRefreshLayout.setRefreshing(false);
    }

    // 以下、非同期データ読み込み処理のコールバック

    @Override
    public Loader<List<Nk225Entity>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");

        Nk225ListReader loader = new Nk225ListReader(getApplication());
        loader.forceLoad();

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<Nk225Entity>> loader, List<Nk225Entity> data) {
        Log.d(TAG, "onLoadFinished");

        // 指定日数以前のデータは捨てる
        int displayPeriod = getDisplayPeriod();
        if (displayPeriod > 0) {
            int from = 0;
            int to = data.size() - displayPeriod;
            if (to < 0) {
                to = 0;
            }
            data.subList(from, to).clear();
        }

        pagerAdapter = new Nk225PagerAdapter(getSupportFragmentManager());
        pagerAdapter.setNk225Entitiy(data);
        pagerAdapter.notifyDataSetChanged();

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                    }

                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {
                        if (state == ViewPager.SCROLL_STATE_IDLE) {
                            swipeRefreshLayout.setEnabled(true);
                        } else {
                            swipeRefreshLayout.setEnabled(false);
                        }
                    }
                });

        // 最終ページ（最新日付）を表示
        viewPager.setCurrentItem(pagerAdapter.getCount() - 1);

        PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.tab_strip);
        if (data.isEmpty()) {
            pagerTabStrip.setVisibility(View.INVISIBLE);
        } else {
            pagerTabStrip.setDrawFullUnderline(true);
            pagerTabStrip.setTabIndicatorColor(Color.DKGRAY);
            pagerTabStrip.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Nk225Entity>> loader) {
        Log.d(TAG, "onLoaderReset");

        // 今回は無視する
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            //SecondActivityから戻ってきた場合
            case (REQUEST_CODE):
                if (resultCode == RESULT_OK) {
                    //OKボタンを押して戻ってきたときの処理
                    if (data.getBooleanExtra("displayPeriodChanged", false)) {
                        // 表示期間が変更された
                        needReflesh = true;
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    //キャンセルボタンを押して戻ってきたときの処理
                } else {
                    //その他
                }
                break;

            default:
                break;
        }
    }

    // 以下ダウンロード関連処理

    private SimpleProgressDialog progressDialog;

    private void showProgressDialog(Context context) {
        Log.d(TAG, "showProgressDialog");

        dismissProgressDialog();
        progressDialog = new SimpleProgressDialog(context);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        Log.d(TAG, "dismissProgressDialog");

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = null;
    }

    private static AbstractNk225DownloadProcess downloader = null;

    private class ManualNk225DownloadProcess extends AbstractNk225DownloadProcess {

        @Override
        protected void onPreDownloadProcess() {
            showProgressDialog(MainActivity.this);
        }

        @Override
        protected void onPostDownloadProcess(boolean result) {
            dismissProgressDialog();

            if (result) {
                Toast.makeText(MainActivity.this, "画面を下に引っ張り、表示を更新して下さい", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "株価データの取得に失敗しました。\nしばらく時間をおいてから再度お試しください", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class AutoNk225DownloadProcess extends AbstractNk225DownloadProcess {

        @Override
        protected void onPreDownloadProcess() {
            showProgressDialog(MainActivity.this);
        }

        @Override
        protected void onPostDownloadProcess(boolean result) {
            dismissProgressDialog();

            if (isVisible) {
                onRefresh();
            } else {
                needReflesh = true;
            }

            if (!result) {
                Toast.makeText(MainActivity.this, "株価データの取得に失敗しました。\nしばらく時間をおいてから再度お試しください", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean migareteDb() {

        Realm realm = Realm.getDefaultInstance();

        if (realm == null) {
            return false;
        }

        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        int schemaVersion = Integer.parseInt(preference.getString("schema_version", "0"));

        if (schemaVersion < 1) {
            if (!migrateDb0To1(realm)) {
                return false;
            }

            SharedPreferences.Editor editor = preference.edit();
            editor.putString("schema_version", "1");
            editor.commit();
        }

        if (schemaVersion < 2) {
            if (!migrateDb1To2(realm)) {
                return false;
            }

            SharedPreferences.Editor editor = preference.edit();
            editor.putString("schema_version", "2");
            editor.commit();
        }

        return true;
    }

    private boolean migrateDb0To1(Realm realm) {

        Log.d(TAG, "migrateDb0To1");

        return true;
    }

    private boolean migrateDb1To2(Realm realm) {

        Log.d(TAG, "migrateDb1To2");

        try {
            realm.beginTransaction();

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd");

            // 2016/1/1以降を再計算
            Date fromDate = DateUtils.convertToDate("2016/01/01", "yyyy/MM/dd");
            RealmResults<Candlestick> results = realm.where(Candlestick.class)
                .greaterThan("date", fromDate)
                .findAllSorted("date", Sort.ASCENDING);

            for (int i = 0; i < results.size(); ++i) {

                Date date = results.get(i).getDate();

                RealmResults<Nk225Entity> nk225Entities = realm.where(Nk225Entity.class)
                    .equalTo("date", date)
                    .findAll();

                for (int j = 0; j < nk225Entities.size(); ++j) {

                    Nk225Entity nk225 = nk225Entities.get(j);

                    double range = StockUtils.priceRange(date);
                    nk225.setPriceRange(range);
                    Log.d(TAG, fmt.format(date) + ":当日値幅 " + String.valueOf(range));

                    double rangeAvg20 = StockUtils.priceRangeAverage(date, 20);
                    nk225.setPriceRangeAverage20(rangeAvg20);
                    Log.d(TAG, fmt.format(date) + ":20日平均値幅 " + String.valueOf(rangeAvg20));
                }
            }

            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();

            Log.e(TAG, e.toString());

            return false;
        } finally {
            realm.close();
        }

        return true;
    }
}
