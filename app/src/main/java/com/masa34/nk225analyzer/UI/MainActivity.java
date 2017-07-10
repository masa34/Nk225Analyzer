package com.masa34.nk225analyzer.UI;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.masa34.nk225analyzer.R;
import com.masa34.nk225analyzer.Stock.Nk225Entity;
import com.masa34.nk225analyzer.Task.AbstractNk225DownloadProcess;
import com.masa34.nk225analyzer.Task.Nk225ListReader;

import java.util.List;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

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
                .schemaVersion(1)
                .migration(new RealmMigration() {
                    @Override
                    public void migrate(final DynamicRealm realm, long oldVersion, long newVersion) {
                        RealmSchema schema = realm.getSchema();

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
                    }
                })
                .build());

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
        protected void onPostDownloadProcess() {
            dismissProgressDialog();

            Toast.makeText(MainActivity.this, "画面を下に引っ張り、表示を更新して下さい", Toast.LENGTH_LONG).show();
        }
    }

    private class AutoNk225DownloadProcess extends AbstractNk225DownloadProcess {

        @Override
        protected void onPreDownloadProcess() {
            showProgressDialog(MainActivity.this);
        }

        @Override
        protected void onPostDownloadProcess() {
            dismissProgressDialog();

            if (isVisible) {
                onRefresh();
            } else {
                needReflesh = true;
            }
        }
    }
}
